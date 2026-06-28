#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
batch_oss_seed.py
从 OSS 批量获取文件 URL → 自动生成种子 SQL 脚本

与前端完全对齐：
  - 栏目 module：subjectConfig.columnConfig（如 同步备课）
  - 资源类型 type：syncPrepColumnFilters.DEFAULT_RESOURCE_TYPE_TABS（如 音频/朗读）
  - 子类型 sub_type：仅「精彩片段」「微课视频」「课文图片」等需要 subType 筛选时使用
  - 目录节点：
      · 同步备课：primary_chinese_curriculum + 34_seed_catalog_primary_2023_full.sql（y1s1_语文_u01_l03）
      · 期末复习：35_seed_qicai_primary_baseline.sql（y1s1_pack_final_review_*）

用法：
    python batch_oss_seed.py

前置条件：
    1. ossutil 已安装并配置（ossutil config）
    2. 修改下方配置区域
    3. 确认要扫描的 OSS 目录前缀

生成的 SQL 与 sql/tools/seed_oss_y1s1_*.sql 格式一致
"""

from __future__ import annotations

import io
import os
import re
import subprocess
import sys
from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional
from urllib.parse import quote

from generate_qicai_primary_baseline import knowledge_points_for_volume, slug
from primary_chinese_curriculum import CHINESE_UNITS

# 与 generate_qicai_primary_baseline.PACK_ROOTS 中 pack_final_review 一致
PACK_FINAL_REVIEW = "pack_final_review"

# ============================================================
# 配置区域 — 请根据实际情况修改
# ============================================================

OSSUTIL_PATH = r"F:\ossutil-2.3.0-windows-amd64\ossutil.exe"

OSS_BUCKET = "xinketangdu"
OSS_ENDPOINT = "oss-cn-shenzhen.aliyuncs.com"

# 七彩 OSS 目录前缀（示例：我爱学语文课文下的所有文件）
OSS_PREFIX = "0.我上学了/3 我爱学语文/"

OUTPUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "mysql")

DB_NAME = "xinketang"

# 册别 key（与 edu_catalog_node.meta.volumeKey、前端 volumeDataMap 一致）
VOLUME_KEY = "y1s1"

# 是否从 OSS 路径自动解析 unit / lesson / catalog_node_code（推荐 True）
AUTO_RESOLVE_CATALOG = True

# 手动覆盖（AUTO_RESOLVE_CATALOG=False 或解析失败时使用）
CATALOG_NODE_CODE = "y1s1_语文_u01_l03"

# 资源维度（catalog 解析成功时会自动填充 unit_name / lesson_name / catalog_path）
DIMENSIONS = {
    "stage": "小学",
    "subject": "语文",
    "module": "同步备课",
    "grade_name": "一年级上册",
    "edition": "统编版(2024)",
    "brand_code": "qicai",
    "catalog_path": "",       # 自动：OSS 前缀去掉末尾 /
    "unit_name": "",          # 自动：我上学了
    "lesson_name": "",        # 自动：我爱学语文
}

# ============================================================
# 前端对齐常量（勿随意改名，须与 k12-edu-platform 保持一致）
# ============================================================

# syncPrepColumnFilters.ts — 同步备课 Tab 直接作为 DB.type 入库
FRONTEND_TYPE_TABS = [
    "课件",
    "教案",
    "练习",
    "试卷",
    "学案",
    "电子课本",
    "教学反思",
    "音频/朗读",
    "视频",
    "知识点",
]

# 同步备课额外 Tab → (type, sub_type)
SYNC_PREP_EXTRA_TYPES: dict[str, tuple[str, str]] = {
    "精彩片段": ("视频", "精彩片段"),
    "课文相关图片": ("图片素材", "课文图片"),
}

# 视频类 sub_type（筛选 Tab 为「视频」时只匹配 type=视频，sub_type 用于细分）
VIDEO_SUB_TYPES = {
    "微课": "微课视频",
    "微课视频": "微课视频",
    "课堂实录": "课堂实录",
    "示范课": "示范课",
}

# 七彩文件夹名 → 统编 canonical 单元名（generate_qicai_primary_baseline.py）
QICAI_UNIT_ALIASES: dict[str, dict[str, str]] = {
    "y1s1": {
        "第一单元·识字": "第一单元：识字",
        "第一单元": "第一单元：识字",
        "第二单元·拼音": "汉语拼音",
        "第二单元": "汉语拼音",
        "第三单元·课文": "第四单元：课文",
        "第四单元·识字": "第五单元：识字",
        "第五单元·课文": "第六单元：课文",
        "第六单元·识字": "第七单元：课文",
        "第七单元·课文": "第八单元：课文",
    },
}

# 栏目：OSS 路径关键词 → module（subjectConfig.columnConfig.primary）
MODULE_FROM_PATH: list[tuple[re.Pattern[str], str]] = [
    (re.compile(r"国学阅读|国学"), "国学阅读"),
    (re.compile(r"教师工作包|班主任"), "教师工作包"),
    (re.compile(r"期中复习"), "期中复习"),
    (re.compile(r"期末复习"), "期末复习"),
    (re.compile(r"开学专区|开学"), "开学专区"),
    (re.compile(r"月考"), "月考"),
    (re.compile(r"期中(?!复习)"), "期中"),
    (re.compile(r"期末(?!复习)"), "期末"),
    (re.compile(r"小升初"), "小升初真题"),
    (re.compile(r"专题复习"), "专题复习"),
    (re.compile(r"真题汇编"), "真题汇编"),
    (re.compile(r"暑假"), "暑假"),
    (re.compile(r"寒假"), "寒假"),
    (re.compile(r"作文"), "作文"),
    (re.compile(r"阅读"), "阅读"),
    (re.compile(r"竞赛"), "竞赛"),
    (re.compile(r"纯素材"), "纯素材"),
]

# ============================================================
# 类型推断：扩展名 + 文件名 + 文件夹路径
# ============================================================

# 扩展名兜底（type, sub_type）
EXT_TYPE_MAP: dict[str, tuple[str, Optional[str]]] = {
    ".doc": ("教案", None),
    ".docx": ("教案", None),
    ".ppt": ("课件", None),
    ".pptx": ("课件", None),
    ".pdf": ("讲义", None),
    ".mp3": ("音频/朗读", None),
    ".wav": ("音频/朗读", None),
    ".m4a": ("音频/朗读", None),
    ".aac": ("音频/朗读", None),
    ".mp4": ("视频", "微课视频"),
    ".mov": ("视频", "微课视频"),
    ".avi": ("视频", "微课视频"),
    ".jpg": ("图片素材", "课文图片"),
    ".jpeg": ("图片素材", "课文图片"),
    ".png": ("图片素材", "课文图片"),
    ".gif": ("图片素材", "课文图片"),
    ".webp": ("图片素材", "课文图片"),
    ".xls": ("练习", None),
    ".xlsx": ("练习", None),
    ".zip": ("其他", None),
    ".rar": ("其他", None),
}

# 文件名 / 路径关键词（优先级高于扩展名）
# 每条：(pattern, type, sub_type)
NAME_TYPE_RULES: list[tuple[re.Pattern[str], str, Optional[str]]] = [
    # 期末复习（优先于通用规则；「专项复习题」须先于「专项复习」避免误匹配）
    (re.compile(r"期末检测卷|期末试卷|期末检测"), "试卷", "期末检测"),
    (re.compile(r"专项复习题"), "练习", "专项复习"),
    (re.compile(r"复习课件"), "课件", "复习课件"),
    (re.compile(r"【课件】|课件\+教案|/课件/"), "课件", None),
    (re.compile(r"【教案】|优质教案|教学设计"), "教案", None),
    (re.compile(r"【学案】"), "学案", None),
    (re.compile(r"【课时练习】|课时练习|【练习】"), "练习", None),
    (re.compile(r"【试卷】|检测卷|模拟卷|真题"), "试卷", None),
    (re.compile(r"教学反思"), "教学反思", None),
    (re.compile(r"电子课本|电子书"), "电子课本", None),
    (re.compile(r"知识点|考点手册"), "知识点", None),
    (re.compile(r"精彩片段"), "视频", "精彩片段"),
    (re.compile(r"课文相关图片|课文图片|相关图片"), "图片素材", "课文图片"),
    (re.compile(r"【朗读】|课文朗读|相关音频|/音频/|朗读"), "音频/朗读", None),
    (re.compile(r"【微课】|微课视频|教学视频"), "视频", "微课视频"),
    (re.compile(r"课堂实录"), "视频", "课堂实录"),
]


@dataclass
class CatalogContext:
    volume_key: str
    unit_name: str
    lesson_name: str
    catalog_node_code: str
    catalog_path: str
    resolved: bool = True  # False 表示自动解析失败、使用了手动兜底


@dataclass
class ResourceRow:
    key: str
    size: int
    filename: str
    file_ext: str
    title: str
    resource_type: str
    sub_type: Optional[str]
    module: str
    catalog: CatalogContext


# ============================================================
# OSS 工具
# ============================================================

def run_ossutil(args: list[str]) -> str:
    cmd = [OSSUTIL_PATH] + args
    try:
        result = subprocess.run(
            cmd, capture_output=True, text=True, check=False, encoding="utf-8",
        )
        if result.returncode != 0:
            print(f"[警告] ossutil 返回非零退出码 {result.returncode}", file=sys.stderr)
            if result.stderr:
                print(f"  stderr: {result.stderr[:500]}", file=sys.stderr)
        return result.stdout
    except FileNotFoundError:
        print(f"[错误] 找不到 ossutil: {OSSUTIL_PATH}", file=sys.stderr)
        sys.exit(1)


def list_oss_files(prefix: str) -> list[dict]:
    bucket_url = f"oss://{OSS_BUCKET}/{prefix}"
    raw = run_ossutil(["ls", bucket_url, "-r"])
    files: list[dict] = []
    pattern = re.compile(
        r"^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\s+\+0800 CST\s+(\d+)\s+Standard\s+\S+\s+oss://[^/]+/(.+)$"
    )
    for line in raw.split("\n"):
        m = pattern.match(line)
        if m:
            files.append({"Key": m.group(2), "Size": int(m.group(1))})
    return files


def oss_key_to_url(object_key: str) -> str:
    encoded = "/".join(quote(part, safe="") for part in object_key.split("/"))
    return f"https://{OSS_BUCKET}.{OSS_ENDPOINT}/{encoded}"


# ============================================================
# 目录解析（OSS 路径 → 统编 catalog_node_code）
# ============================================================

def normalize_folder_name(name: str) -> str:
    """去掉七彩文件夹前缀编号：'0.我上学了' → '我上学了'，'3 我爱学语文' → '我爱学语文'"""
    s = name.strip().strip("/")
    s = re.sub(r"^\d+[\.\、]?\s*", "", s)
    return s.strip()


def canonical_unit_name(volume_key: str, folder_unit: str) -> str:
    aliases = QICAI_UNIT_ALIASES.get(volume_key, {})
    return aliases.get(folder_unit, folder_unit)


def fuzzy_lesson_match(catalog_lesson: str, folder_lesson: str) -> bool:
    a = re.sub(r"^\d+\s*", "", catalog_lesson).strip()
    b = re.sub(r"^\d+\s*", "", folder_lesson).strip()
    if catalog_lesson == folder_lesson or a == b:
        return True
    if b in catalog_lesson or catalog_lesson in b:
        return True
    if len(a) >= 2 and len(b) >= 2 and (a in b or b in a):
        return True
    return False


def is_final_review_path(oss_key: str) -> bool:
    return "期末复习" in oss_key


def pack_final_review_code(volume_key: str, suffix: str) -> str:
    """生成 pack 期末复习节点 code，suffix 不含 volume 前缀。"""
    return f"{volume_key}_{PACK_FINAL_REVIEW}_{suffix}"


def find_folder_index(folder_parts: list[str], marker: str) -> Optional[int]:
    for i, part in enumerate(folder_parts):
        if part == marker or marker in part:
            return i
    return None


def fuzzy_match_unit(volume_key: str, folder_unit: str) -> tuple[Optional[int], str]:
    """七彩/OSS 文件夹名 → (单元序号, canonical 单元名)。跳过「附录」。"""
    raw = normalize_folder_name(folder_unit)
    canonical = canonical_unit_name(volume_key, raw)
    units = CHINESE_UNITS.get(volume_key, [])
    for i, (uname, _) in enumerate(units, 1):
        if uname == "附录":
            continue
        candidates = (raw, canonical, folder_unit)
        for c in candidates:
            if not c:
                continue
            if c == uname or c in uname or uname in c:
                return i, uname
    return None, ""


def fuzzy_match_knowledge_point(volume_key: str, folder_name: str) -> Optional[str]:
    norm = normalize_folder_name(folder_name)
    if not norm:
        return None
    for kp in knowledge_points_for_volume(volume_key):
        if kp == norm or norm in kp or kp in norm:
            return kp
    return None


def infer_unit_from_review_filename(filename: str, volume_key: str) -> tuple[Optional[int], str]:
    """从「第一单元复习课件.pptx」等文件名推断统编单元（无单元子文件夹时使用）。"""
    stem = os.path.splitext(filename)[0]
    stem = re.sub(r"^\d+[\.\、]?\s*", "", stem)
    units = CHINESE_UNITS.get(volume_key, [])
    if not units:
        return None, ""

    if "我上学了" in stem:
        for i, (uname, _) in enumerate(units, 1):
            if uname == "我上学了":
                return i, uname

    m = re.search(r"第([一二三四五六七八])[、,，]([一二三四五六七八])单元", stem)
    if m and volume_key == "y1s1" and m.group(1) == "二" and m.group(2) == "三":
        for i, (uname, _) in enumerate(units, 1):
            if uname == "汉语拼音":
                return i, uname

    m = re.search(r"第([一二三四五六七八])单元", stem)
    if m:
        cn = m.group(1)
        for i, (uname, _) in enumerate(units, 1):
            if uname.startswith(f"第{cn}单元") or f"第{cn}单元" in uname:
                return i, uname

    return None, ""


def infer_knowledge_point_from_special_filename(filename: str, volume_key: str) -> Optional[str]:
    """从「专项1：汉语拼音专项复习课件.ppt」等文件名推断知识点。"""
    stem = os.path.splitext(filename)[0]
    stem = re.sub(r"^\d+[\.\、]?\s*", "", stem)
    rules: list[tuple[re.Pattern[str], str]] = [
        (re.compile(r"汉语拼音|拼音专项|拼音和笔画", re.I), "拼音与识字"),
        (re.compile(r"生字", re.I), "拼音与识字"),
        (re.compile(r"词语", re.I), "词语与句子"),
        (re.compile(r"句子|标点", re.I), "词语与句子"),
        (re.compile(r"阅读", re.I), "阅读与鉴赏"),
        (re.compile(r"口语交际", re.I), "口语交际"),
        (re.compile(r"写话|看图", re.I), "习作"),
        (re.compile(r"积累|背诵|古诗", re.I), "古诗与积累"),
    ]
    for pattern, kp in rules:
        if pattern.search(stem):
            if kp in knowledge_points_for_volume(volume_key):
                return kp
    return None


def resolve_final_review_catalog_from_oss_key(
    oss_key: str, volume_key: str,
) -> Optional[CatalogContext]:
    """
    从 OSS key 解析期末复习 pack 目录节点。

    示例:
      10.期末复习/复习课件/单元复习/第一单元·识字/xxx.ppt
        → y1s1_pack_final_review_复习课件_单元_02
      10.期末复习/复习课件/专项复习/拼音与识字/xxx.ppt
        → y1s1_pack_final_review_复习课件_专项_拼音与识字
      10.期末复习/专项复习题/词语与句子/xxx.doc
        → y1s1_pack_final_review_专项复习题_词语与句子
      10.期末复习/期末检测卷/xxx.pdf
        → y1s1_pack_final_review_期末检测卷
    """
    parts = [p for p in oss_key.split("/") if p]
    if len(parts) < 2:
        return None

    folder_parts = [normalize_folder_name(p) for p in parts[:-1]]
    if not any("期末复习" in p for p in folder_parts):
        return None

    catalog_path = "/".join(parts[:-1])

    # 期末检测卷（文件可在该目录任意子层）
    if find_folder_index(folder_parts, "期末检测卷") is not None:
        return CatalogContext(
            volume_key=volume_key,
            unit_name="期末复习",
            lesson_name="期末检测卷",
            catalog_node_code=pack_final_review_code(volume_key, "期末检测卷"),
            catalog_path=catalog_path,
            resolved=True,
        )

    # 专项复习题 / {知识点}
    ex_idx = find_folder_index(folder_parts, "专项复习题")
    if ex_idx is not None:
        kp: Optional[str] = None
        if ex_idx + 1 < len(folder_parts):
            kp = fuzzy_match_knowledge_point(volume_key, folder_parts[ex_idx + 1])
        if kp:
            return CatalogContext(
                volume_key=volume_key,
                unit_name=kp,
                lesson_name="",
                catalog_node_code=pack_final_review_code(volume_key, f"专项复习题_{slug(kp)}"),
                catalog_path=catalog_path,
                resolved=True,
            )
        return None

    # 复习课件 / 单元复习|专项复习 / {单元|知识点}
    cw_idx = find_folder_index(folder_parts, "复习课件")
    if cw_idx is not None:
        unit_scope_idx = find_folder_index(folder_parts, "单元复习")
        if unit_scope_idx is not None and unit_scope_idx > cw_idx:
            filename = os.path.basename(oss_key)
            if unit_scope_idx + 1 < len(folder_parts):
                uidx, uname = fuzzy_match_unit(volume_key, folder_parts[unit_scope_idx + 1])
            else:
                uidx, uname = infer_unit_from_review_filename(filename, volume_key)
            if uidx is not None and uname:
                return CatalogContext(
                    volume_key=volume_key,
                    unit_name=uname,
                    lesson_name="",
                    catalog_node_code=pack_final_review_code(
                        volume_key, f"复习课件_单元_{uidx:02d}",
                    ),
                    catalog_path=catalog_path,
                    resolved=True,
                )

        special_scope_idx = find_folder_index(folder_parts, "专项复习")
        if special_scope_idx is not None and special_scope_idx > cw_idx:
            filename = os.path.basename(oss_key)
            if special_scope_idx + 1 < len(folder_parts):
                kp = fuzzy_match_knowledge_point(volume_key, folder_parts[special_scope_idx + 1])
            else:
                kp = infer_knowledge_point_from_special_filename(filename, volume_key)
            if kp:
                return CatalogContext(
                    volume_key=volume_key,
                    unit_name=kp,
                    lesson_name="",
                    catalog_node_code=pack_final_review_code(
                        volume_key, f"复习课件_专项_{slug(kp)}",
                    ),
                    catalog_path=catalog_path,
                    resolved=True,
                )

    return None


def resolve_sync_prep_catalog_from_oss_key(oss_key: str, volume_key: str) -> Optional[CatalogContext]:
    """
    从 OSS key 解析同步备课单元/课文/catalog_node_code。
    示例 key: 0.我上学了/3 我爱学语文/音频/猪八戒吃西瓜.mp3
    """
    parts = [p for p in oss_key.split("/") if p]
    if len(parts) < 2:
        return None

    folder_parts = [normalize_folder_name(p) for p in parts[:-1]]
    if not folder_parts:
        return None

    units = CHINESE_UNITS.get(volume_key)
    if not units:
        return None

    # 第一级文件夹通常是单元（如 我上学了 / 第一单元：识字）
    raw_unit = folder_parts[0]
    unit_name = canonical_unit_name(volume_key, raw_unit)

    unit_idx = None
    lessons: list[str] = []
    for i, (uname, llist) in enumerate(units, 1):
        if uname == unit_name or raw_unit == uname or unit_name in uname or uname in unit_name:
            unit_idx = i
            unit_name = uname
            lessons = llist
            break
    if unit_idx is None:
        return None

    # 第二级通常是课文（如 我爱学语文 / 1 天地人）
    lesson_name = ""
    lesson_idx = None
    if len(folder_parts) >= 2:
        raw_lesson = folder_parts[1]
        for j, lname in enumerate(lessons, 1):
            if fuzzy_lesson_match(lname, raw_lesson):
                lesson_name = lname
                lesson_idx = j
                break
        if lesson_idx is None and len(lessons) == 1:
            lesson_name = lessons[0]
            lesson_idx = 1

    if lesson_idx is None:
        return None

    code = f"{volume_key}_语文_u{unit_idx:02d}_l{lesson_idx:02d}"
    catalog_path = "/".join(parts[:-1])
    return CatalogContext(
        volume_key=volume_key,
        unit_name=unit_name,
        lesson_name=lesson_name,
        catalog_node_code=code,
        catalog_path=catalog_path,
        resolved=True,
    )


def resolve_catalog_from_oss_key(oss_key: str, volume_key: str) -> Optional[CatalogContext]:
    """按路径类型分发：期末复习 pack → 同步备课课文树。"""
    if is_final_review_path(oss_key):
        return resolve_final_review_catalog_from_oss_key(oss_key, volume_key)
    return resolve_sync_prep_catalog_from_oss_key(oss_key, volume_key)


def resolve_module_from_path(path_text: str) -> str:
    for pattern, module in MODULE_FROM_PATH:
        if pattern.search(path_text):
            return module
    return DIMENSIONS.get("module", "同步备课")


def infer_resource_type(oss_key: str, filename: str) -> tuple[str, Optional[str]]:
    """返回 (type, sub_type)，type 必须与前端 Tab 文案一致"""
    path_lower = oss_key.lower()
    name_lower = filename.lower()
    combined = f"{oss_key}/{filename}"

    for pattern, rtype, sub in NAME_TYPE_RULES:
        if pattern.search(combined) or pattern.search(name_lower):
            return rtype, sub

    ext = os.path.splitext(filename)[1].lower()
    if ext in EXT_TYPE_MAP:
        rtype, sub = EXT_TYPE_MAP[ext]
        # mp4 等：若路径含「微课」则保持微课视频
        if rtype == "视频" and sub == "微课视频":
            for kw, st in VIDEO_SUB_TYPES.items():
                if kw in combined:
                    return "视频", st
        return rtype, sub

    return "其他", None


def build_resource_row(oss_key: str, size: int) -> ResourceRow:
    filename = os.path.basename(oss_key)
    file_ext = os.path.splitext(filename)[1].lstrip(".").lower()
    title = os.path.splitext(filename)[0]

    if AUTO_RESOLVE_CATALOG:
        catalog = resolve_catalog_from_oss_key(oss_key, VOLUME_KEY)
    else:
        catalog = None

    if catalog is None:
        catalog = CatalogContext(
            volume_key=VOLUME_KEY,
            unit_name=DIMENSIONS.get("unit_name", ""),
            lesson_name=DIMENSIONS.get("lesson_name", ""),
            catalog_node_code=CATALOG_NODE_CODE,
            catalog_path=DIMENSIONS.get("catalog_path", OSS_PREFIX.rstrip("/")),
            resolved=False,
        )

    module = resolve_module_from_path(oss_key)
    rtype, sub_type = infer_resource_type(oss_key, filename)

    return ResourceRow(
        key=oss_key,
        size=size,
        filename=filename,
        file_ext=file_ext,
        title=title,
        resource_type=rtype,
        sub_type=sub_type,
        module=module,
        catalog=catalog,
    )


# ============================================================
# SQL 生成
# ============================================================

def escape_sql(val: str) -> str:
    return val.replace("'", "''")


def catalog_node_id_sql(code: str) -> str:
    return (
        f"(SELECT `id` FROM `edu_catalog_node` "
        f"WHERE `code` = '{escape_sql(code)}' AND `status` = 1 LIMIT 1)"
    )


def generate_insert_sql(row: ResourceRow, sort_index: int) -> str:
    d = DIMENSIONS
    cat = row.catalog
    node_sql = catalog_node_id_sql(cat.catalog_node_code)
    sort = sort_index * 10
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    oss_url = oss_key_to_url(row.key)
    file_size_kb = max(1, row.size // 1024)
    sub_type_sql = f"'{escape_sql(row.sub_type)}'" if row.sub_type else "NULL"

    remark = (
        f"{d['stage']}{d['grade_name']}{d['subject']} · "
        f"{cat.unit_name} · {cat.lesson_name} · {row.module}{row.resource_type}"
    )
    if row.sub_type:
        remark += f"({row.sub_type})"

    catalog_path = cat.catalog_path or OSS_PREFIX.rstrip("/")

    return f"""-- {row.filename}  →  type={row.resource_type}  sub_type={row.sub_type or 'NULL'}
INSERT INTO `oss_primary_chinese_resource` (
  `stage`, `subject`, `module`, `type`, `sub_type`,
  `grade_name`, `edition`, `brand_code`,
  `catalog_node_id`, `catalog_path`,
  `unit_name`, `lesson_name`,
  `title`, `original_filename`, `file_ext`,
  `oss_bucket`, `oss_object_key`, `oss_url`,
  `file_size_kb`, `download_count`, `view_count`,
  `status`, `allow_preview`, `sort`, `remark`,
  `upload_time`, `is_deleted`
) VALUES (
  '{d['stage']}', '{d['subject']}', '{row.module}', '{row.resource_type}', {sub_type_sql},
  '{d['grade_name']}', '{d['edition']}', '{d['brand_code']}',
  {node_sql},
  '{escape_sql(catalog_path)}',
  '{escape_sql(cat.unit_name)}', '{escape_sql(cat.lesson_name)}',
  '{escape_sql(row.title)}',
  '{escape_sql(row.filename)}',
  '{row.file_ext}',
  '{OSS_BUCKET}',
  '{escape_sql(row.key)}',
  '{escape_sql(oss_url)}',
  {file_size_kb}, 0, 0,
  1, 1, {sort},
  '{escape_sql(remark)}',
  '{now}',
  0
);
SET @rid = LAST_INSERT_ID();

INSERT INTO `edu_resource_placement` (`resource_id`, `catalog_node_id`, `is_primary`, `sort`)
SELECT @rid, {node_sql}, 1, {sort}
WHERE {node_sql} IS NOT NULL;

"""


def write_sql(buf: io.StringIO, rows: list[ResourceRow]) -> None:
    keys_escaped = [escape_sql(r.key) for r in rows]
    keys_list = ",\n  ".join(f"'{k}'" for k in keys_escaped)

    print("-- ============================================================", file=buf)
    print("-- 自动生成的 OSS 种子数据（前端类型对齐版）", file=buf)
    print(f"-- 桶: {OSS_BUCKET}", file=buf)
    print(f"-- OSS 路径: {OSS_PREFIX}", file=buf)
    print(f"-- 册别: {VOLUME_KEY}", file=buf)
    if rows:
        sample = rows[0].catalog
        print(f"-- 目录节点示例: {sample.catalog_node_code} ({sample.unit_name}/{sample.lesson_name})", file=buf)
    print(f"-- 生成时间: {datetime.now()}", file=buf)
    print(f"-- 文件数: {len(rows)}", file=buf)
    print("-- 前端 type 须为: 课件/教案/练习/试卷/学案/电子课本/教学反思/音频/朗读/视频/知识点", file=buf)
    print("-- ============================================================", file=buf)
    print(f"USE `{DB_NAME}`;", file=buf)
    print("SET NAMES utf8mb4;", file=buf)
    print(file=buf)

    print("-- 幂等清理", file=buf)
    print("DELETE p", file=buf)
    print("FROM `edu_resource_placement` p", file=buf)
    print("INNER JOIN `oss_primary_chinese_resource` r ON r.`id` = p.`resource_id`", file=buf)
    print("WHERE r.`oss_object_key` IN (", file=buf)
    print(f"  {keys_list}", file=buf)
    print(");", file=buf)
    print(file=buf)
    print("DELETE FROM `oss_primary_chinese_resource`", file=buf)
    print("WHERE `oss_object_key` IN (", file=buf)
    print(f"  {keys_list}", file=buf)
    print(");", file=buf)
    print(file=buf)

    print(f"-- 插入 {len(rows)} 条资源", file=buf)
    for i, row in enumerate(rows, 1):
        print(generate_insert_sql(row, i), file=buf)

    print("-- 校验", file=buf)
    print("SELECT", file=buf)
    print("  r.`id`, r.`module`, r.`type`, r.`sub_type`,", file=buf)
    print("  r.`grade_name`, r.`edition`, r.`unit_name`, r.`lesson_name`,", file=buf)
    print("  r.`catalog_node_id`, r.`title`, r.`file_ext`,", file=buf)
    print("  (SELECT COUNT(*) FROM `edu_resource_placement` p WHERE p.`resource_id` = r.`id`) AS placement_cnt", file=buf)
    print("FROM `oss_primary_chinese_resource` r", file=buf)
    print("WHERE r.`oss_object_key` IN (", file=buf)
    print(f"  {keys_list}", file=buf)
    print(")", file=buf)
    print("ORDER BY r.`sort`;", file=buf)
    print(file=buf)


def print_type_summary(rows: list[ResourceRow]) -> None:
    print("\n# 类型推断摘要（请核对是否与前端 Tab 一致）", file=sys.stderr)
    for row in rows:
        sub = row.sub_type or "-"
        print(
            f"  {row.filename[:40]:40}  type={row.resource_type:8}  sub={sub:12}  "
            f"node={row.catalog.catalog_node_code}",
            file=sys.stderr,
        )


def main() -> None:
    print(f"# 扫描 OSS: oss://{OSS_BUCKET}/{OSS_PREFIX}", file=sys.stderr)
    raw_files = list_oss_files(OSS_PREFIX)
    print(f"# 找到 {len(raw_files)} 个对象\n", file=sys.stderr)

    if not raw_files:
        print("# 未找到文件，请检查 OSS_PREFIX", file=sys.stderr)
        sys.exit(1)

    rows = [build_resource_row(f["Key"], f["Size"]) for f in raw_files]
    print_type_summary(rows)

    # 检查无法自动解析目录的文件
    unresolved = [r for r in rows if AUTO_RESOLVE_CATALOG and not r.catalog.resolved]
    if unresolved:
        print(
            f"\n[警告] {len(unresolved)} 个文件未能自动解析 catalog_node_code，"
            "请检查 OSS 路径层级或手动设置 CATALOG_NODE_CODE",
            file=sys.stderr,
        )
        for r in unresolved[:5]:
            print(f"  - {r.key}  →  fallback={r.catalog.catalog_node_code}", file=sys.stderr)
        if len(unresolved) > 5:
            print(f"  ... 另有 {len(unresolved) - 5} 个", file=sys.stderr)

    buf = io.StringIO()
    write_sql(buf, rows)

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filepath = os.path.join(OUTPUT_DIR, f"seed_oss_{timestamp}.sql")
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(buf.getvalue())

    print(f"\n# 完成！共 {len(rows)} 条 → {filepath}", file=sys.stderr)
    print("# 执行: mysql -u root -p xinketang < " + filepath, file=sys.stderr)


if __name__ == "__main__":
    main()
