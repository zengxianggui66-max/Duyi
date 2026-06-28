#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成 35：七彩小学 baseline（栏目、单元别名、统编树包外节点 y1s1–y6s2）"""
from __future__ import annotations

import json
import re
from pathlib import Path

from primary_chinese_curriculum import CHINESE_UNITS

VOLUME_KEYS = [
    "y1s1", "y1s2", "y2s1", "y2s2", "y3s1", "y3s2",
    "y4s1", "y4s2", "y5s1", "y5s2", "y6s1", "y6s2",
]

# 七彩文件夹名 → 统编 canonical（primary_chinese_curriculum）
QICAI_ALIAS_OVERRIDES: dict[str, list[tuple[str, str]]] = {
    "y1s1": [
        ("第一单元·识字", "第一单元：识字"),
        ("第一单元", "第一单元：识字"),
        ("第二单元·拼音", "汉语拼音"),
        ("第二单元", "汉语拼音"),
        ("第三单元·课文", "第四单元：课文"),
        ("第四单元·识字", "第五单元：识字"),
        ("第五单元·课文", "第六单元：课文"),
        ("第六单元·识字", "第七单元：课文"),
        ("第七单元·课文", "第八单元：课文"),
    ],
}

PACK_ROOTS = [
    ("pack_mid_review", "期中复习", "期中复习", 901, "folder"),
    ("pack_final_review", "期末复习", "期末复习", 902, "folder"),
    ("pack_guoxue", "国学阅读", "国学阅读", 903, "folder"),
    ("pack_teacher", "教师工作包", "教师工作包", 904, "folder"),
]

MID_REVIEW_LEAVES = ["期中检测卷"]

# 期末复习 · 专项知识点（按册可扩展，缺省用 default）
FINAL_REVIEW_KNOWLEDGE_POINTS: dict[str, list[str]] = {
    "default": [
        "拼音与识字",
        "词语与句子",
        "阅读与鉴赏",
        "口语交际",
        "习作",
        "古诗与积累",
    ],
    "y5s1": [
        "字音字形",
        "词语运用",
        "句子与修辞",
        "阅读与鉴赏",
        "习作",
        "口语交际",
        "古诗与文言文",
    ],
    "y6s1": [
        "字音字形",
        "词语运用",
        "句子与修辞",
        "阅读与鉴赏",
        "习作",
        "口语交际",
        "古诗与文言文",
    ],
}
TEACHER_PACK_LEAVES = [
    "安全", "健康", "礼仪", "黑板报", "手抄报", "班队会活动", "家访登记表",
    "日常行为规范", "工作计划总结", "常用表格", "课堂用语", "学生评语",
    "学生守则", "教学计划", "教学进度表与教材分析",
]


def sql_escape(s: str) -> str:
    return s.replace("\\", "\\\\").replace("'", "''")


def meta_json(obj: dict) -> str:
    return json.dumps(obj, ensure_ascii=False).replace("'", "\\'")


def slug(s: str) -> str:
    s = re.sub(r"[^\w\u4e00-\u9fff]+", "_", s)
    return s[:48] or "n"


def emit_modules(lines: list[str]) -> None:
    lines.extend([
        "-- ==================== 1. edu_module 七彩小学扩展栏目 ====================",
        "INSERT INTO `edu_module` (`id`, `code`, `name`, `icon`, `module_category`, `applicable_stages`, `description`, `sort`, `status`) VALUES",
        "(34, 'guoxue_reading', '国学阅读', '📜', 'reading', '[\"primary\"]', '七彩课堂·按单元国学阅读', 34, 1),",
        "(35, 'teacher_pack', '教师工作包', '🧰', 'material', '[\"primary\"]', '七彩课堂·班主任/教学管理素材', 35, 1),",
        "(36, 'mid_review_pack', '期中复习', '📋', 'review', '[\"primary\"]', '七彩课堂·期中复习包（检测卷等）', 36, 1),",
        "(37, 'final_review_pack', '期末复习', '📋', 'review', '[\"primary\"]', '七彩课堂·期末复习包（课件/专项/检测卷）', 37, 1)",
        "ON DUPLICATE KEY UPDATE",
        "  `name` = VALUES(`name`), `icon` = VALUES(`icon`), `module_category` = VALUES(`module_category`),",
        "  `applicable_stages` = VALUES(`applicable_stages`), `description` = VALUES(`description`),",
        "  `sort` = VALUES(`sort`), `status` = VALUES(`status`);",
        "",
        "INSERT INTO `edu_module_stage` (`module_id`, `stage_id`, `sort`)",
        "SELECT m.id, s.id, m.sort FROM `edu_module` m",
        "JOIN `edu_stage` s ON s.code = 'primary'",
        "WHERE m.code IN ('guoxue_reading','teacher_pack','mid_review_pack','final_review_pack')",
        "ON DUPLICATE KEY UPDATE `sort` = VALUES(`sort`);",
        "",
    ])


def emit_alias_table(lines: list[str]) -> None:
    lines.extend([
        "-- ==================== 2. 七彩 ↔ 统编单元别名表 ====================",
        "CREATE TABLE IF NOT EXISTS `edu_catalog_unit_alias` (",
        "  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,",
        "  `volume_key` VARCHAR(16) NOT NULL COMMENT 'y1s1 等',",
        "  `subject` VARCHAR(32) NOT NULL DEFAULT '语文',",
        "  `alias_name` VARCHAR(200) NOT NULL COMMENT '七彩/OSS 文件夹名',",
        "  `canonical_unit_name` VARCHAR(200) NOT NULL COMMENT '统编目录单元名',",
        "  `match_rule` VARCHAR(20) NOT NULL DEFAULT 'exact' COMMENT 'exact|contains',",
        "  `brand_code` VARCHAR(32) NOT NULL DEFAULT 'qicai',",
        "  `sort` SMALLINT NOT NULL DEFAULT 0,",
        "  `status` TINYINT NOT NULL DEFAULT 1,",
        "  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,",
        "  PRIMARY KEY (`id`),",
        "  KEY `idx_alias_lookup` (`volume_key`, `subject`, `alias_name`(64)),",
        "  KEY `idx_canonical` (`volume_key`, `canonical_unit_name`(64))",
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目录单元别名（七彩文件夹→统编）';",
        "",
        "DELETE FROM `edu_catalog_unit_alias` WHERE `brand_code` = 'qicai' AND `volume_key` IN ("
        + ", ".join(f"'{k}'" for k in VOLUME_KEYS)
        + ");",
        "",
    ])

    rows: list[tuple[str, str, str, str, int]] = []
    seen: set[tuple[str, str, str]] = set()

    def add(vol: str, alias: str, canonical: str, rule: str = "exact", sort: int = 0) -> None:
        key = (vol, alias, canonical)
        if key in seen or not alias or not canonical:
            return
        seen.add(key)
        rows.append((vol, alias, canonical, rule, sort))

    for vol, units in CHINESE_UNITS.items():
        for idx, (unit_name, _lessons) in enumerate(units, 1):
            add(vol, unit_name, unit_name, "exact", idx)
            alt = unit_name.replace("：", "·")
            if alt != unit_name:
                add(vol, alt, unit_name, "exact", idx + 500)
            alt2 = unit_name.replace("：", "")
            if alt2 != unit_name and alt2 != alt:
                add(vol, alt2, unit_name, "contains", idx + 600)
        for alias, canonical in QICAI_ALIAS_OVERRIDES.get(vol, []):
            add(vol, alias, canonical, "exact", 0)

    lines.append(
        "INSERT INTO `edu_catalog_unit_alias` "
        "(`volume_key`,`subject`,`alias_name`,`canonical_unit_name`,`match_rule`,`brand_code`,`sort`,`status`) VALUES"
    )
    vals = []
    for vol, alias, canonical, rule, sort in rows:
        vals.append(
            f"('{vol}','语文','{sql_escape(alias)}','{sql_escape(canonical)}','{rule}','qicai',{sort},1)"
        )
    for i in range(0, len(vals), 40):
        chunk = vals[i : i + 40]
        lines.append(",\n".join(chunk) + (";" if i + 40 >= len(vals) else ","))
    lines.append("")


def knowledge_points_for_volume(vol: str) -> list[str]:
    return FINAL_REVIEW_KNOWLEDGE_POINTS.get(vol) or FINAL_REVIEW_KNOWLEDGE_POINTS["default"]


def emit_final_review_pack(
    lines: list[str],
    vol: str,
    pack_code: str,
    pack_path: str,
    module: str,
) -> None:
    """期末复习：复习课件(单元/专项) + 专项复习题 + 期末检测卷"""
    var = f"@{vol}_{pack_code}"

    def insert_node(
        parent_var: str,
        node_code: str,
        name: str,
        name_path: str,
        depth: int,
        node_type: str,
        sort: int,
        meta: dict,
    ) -> str:
        m = meta_json(meta)
        lines.append(
            "INSERT INTO `edu_catalog_node` "
            "(`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) "
            f"SELECT s.id, {parent_var}, '{node_code}', '{sql_escape(name)}', "
            f"'{sql_escape(name_path)}', {depth}, '{node_type}', {sort}, '{m}', 1 "
            "FROM `edu_catalog_scheme` s WHERE s.code = 'textbook_unit' LIMIT 1;"
        )
        child_var = f"@{node_code}"
        lines.append(f"SET {child_var} := LAST_INSERT_ID();")
        lines.append("")
        return child_var

    # 复习课件
    cw_var = insert_node(
        var,
        f"{vol}_{pack_code}_复习课件",
        "复习课件",
        f"{pack_path}/复习课件",
        2,
        "folder",
        1,
        {
            "volumeKey": vol,
            "subject": "语文",
            "defaultModule": module,
            "reviewKind": "courseware",
        },
    )

    unit_scope_var = insert_node(
        cw_var,
        f"{vol}_{pack_code}_复习课件_单元复习",
        "单元复习",
        f"{pack_path}/复习课件/单元复习",
        3,
        "folder",
        1,
        {
            "volumeKey": vol,
            "subject": "语文",
            "defaultModule": module,
            "reviewKind": "courseware",
            "reviewScope": "unit",
        },
    )

    units = CHINESE_UNITS.get(vol, [])
    for uidx, (unit_name, _) in enumerate(units, 1):
        if unit_name == "附录":
            continue
        u_code = f"{vol}_{pack_code}_复习课件_单元_{uidx:02d}"
        u_path = f"{pack_path}/复习课件/单元复习/{unit_name}"
        u_meta = meta_json({
            "volumeKey": vol,
            "subject": "语文",
            "defaultModule": module,
            "reviewKind": "courseware",
            "reviewScope": "unit",
            "canonicalUnit": unit_name,
        })
        lines.append(
            "INSERT INTO `edu_catalog_node` "
            "(`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) "
            f"SELECT s.id, {unit_scope_var}, '{u_code}', '{sql_escape(unit_name)}', "
            f"'{sql_escape(u_path)}', 4, 'leaf', {uidx}, '{u_meta}', 1 "
            "FROM `edu_catalog_scheme` s WHERE s.code = 'textbook_unit' LIMIT 1;"
        )
    lines.append("")

    special_scope_var = insert_node(
        cw_var,
        f"{vol}_{pack_code}_复习课件_专项复习",
        "专项复习",
        f"{pack_path}/复习课件/专项复习",
        3,
        "folder",
        2,
        {
            "volumeKey": vol,
            "subject": "语文",
            "defaultModule": module,
            "reviewKind": "courseware",
            "reviewScope": "special",
        },
    )

    for kidx, kp in enumerate(knowledge_points_for_volume(vol), 1):
        kp_code = f"{vol}_{pack_code}_复习课件_专项_{slug(kp)}"
        kp_path = f"{pack_path}/复习课件/专项复习/{kp}"
        kp_meta = meta_json({
            "volumeKey": vol,
            "subject": "语文",
            "defaultModule": module,
            "reviewKind": "courseware",
            "reviewScope": "special",
            "knowledgePoint": kp,
        })
        lines.append(
            "INSERT INTO `edu_catalog_node` "
            "(`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) "
            f"SELECT s.id, {special_scope_var}, '{kp_code}', '{sql_escape(kp)}', "
            f"'{sql_escape(kp_path)}', 4, 'leaf', {kidx}, '{kp_meta}', 1 "
            "FROM `edu_catalog_scheme` s WHERE s.code = 'textbook_unit' LIMIT 1;"
        )
    lines.append("")

    # 专项复习题（按知识点）
    ex_var = insert_node(
        var,
        f"{vol}_{pack_code}_专项复习题",
        "专项复习题",
        f"{pack_path}/专项复习题",
        2,
        "folder",
        2,
        {
            "volumeKey": vol,
            "subject": "语文",
            "defaultModule": module,
            "reviewKind": "exercise",
        },
    )

    for kidx, kp in enumerate(knowledge_points_for_volume(vol), 1):
        kp_code = f"{vol}_{pack_code}_专项复习题_{slug(kp)}"
        kp_path = f"{pack_path}/专项复习题/{kp}"
        kp_meta = meta_json({
            "volumeKey": vol,
            "subject": "语文",
            "defaultModule": module,
            "reviewKind": "exercise",
            "reviewScope": "special",
            "knowledgePoint": kp,
        })
        lines.append(
            "INSERT INTO `edu_catalog_node` "
            "(`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) "
            f"SELECT s.id, {ex_var}, '{kp_code}', '{sql_escape(kp)}', "
            f"'{sql_escape(kp_path)}', 3, 'leaf', {kidx}, '{kp_meta}', 1 "
            "FROM `edu_catalog_scheme` s WHERE s.code = 'textbook_unit' LIMIT 1;"
        )
    lines.append("")

    # 期末检测卷（考试筛选）
    insert_node(
        var,
        f"{vol}_{pack_code}_期末检测卷",
        "期末检测卷",
        f"{pack_path}/期末检测卷",
        2,
        "folder",
        3,
        {
            "volumeKey": vol,
            "subject": "语文",
            "defaultModule": module,
            "reviewKind": "exam",
            "filterProfile": "final_exam",
        },
    )


def emit_pack_nodes(lines: list[str]) -> None:
    lines.extend([
        "-- ==================== 3. 统编树 · 语文根下七彩包外节点（小学 12 册） ====================",
        "-- 依赖 34_seed_catalog_primary_2023_full.sql 已写入 textbook_unit 语文根",
        "",
    ])

    for vol in VOLUME_KEYS:
        if vol not in CHINESE_UNITS:
            continue
        root_code = f"{vol}_语文_root"
        lines.append(f"-- {vol} 语文包外节点")
        lines.append(f"SET @root := (SELECT n.`id` FROM `edu_catalog_node` n")
        lines.append(
            f"  JOIN `edu_catalog_scheme` s ON s.id = n.scheme_id AND s.code = 'textbook_unit'"
        )
        lines.append(f"  WHERE n.code = '{root_code}' LIMIT 1);")
        lines.append(
            f"DELETE FROM `edu_catalog_node` WHERE `parent_id` = @root AND `code` LIKE '{vol}_pack_%';"
        )
        lines.append("")

        subj_label = "语文（统编版）"
        root_path = f"/{subj_label}"

        for code, name, module, sort, ntype in PACK_ROOTS:
            pack_code = f"{vol}_{code}"
            pack_path = f"{root_path}/{name}"
            meta = meta_json({
                "volumeKey": vol,
                "subject": "语文",
                "edition": "统编版",
                "defaultModule": module,
                "packSection": True,
                "brandCode": "qicai",
            })
            lines.append(
                "INSERT INTO `edu_catalog_node` "
                "(`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) "
                f"SELECT s.id, @root, '{pack_code}', '{sql_escape(name)}', "
                f"'{sql_escape(pack_path)}', 1, '{ntype}', {sort}, '{meta}', 1 "
                "FROM `edu_catalog_scheme` s WHERE s.code = 'textbook_unit' LIMIT 1;"
            )
            lines.append(f"SET @{vol}_{code} := LAST_INSERT_ID();")
            lines.append("")

            if code == "pack_final_review":
                emit_final_review_pack(lines, vol, code, pack_path, module)
                continue

            leaves: list[str] = []
            if code == "pack_mid_review":
                leaves = MID_REVIEW_LEAVES
            elif code == "pack_teacher":
                leaves = TEACHER_PACK_LEAVES
            elif code == "pack_guoxue":
                for uidx, (unit_name, _) in enumerate(CHINESE_UNITS[vol], 1):
                    u_code = f"{vol}_pack_guoxue_u{uidx:02d}"
                    u_path = f"{pack_path}/{unit_name}"
                    u_meta = meta_json({
                        "volumeKey": vol,
                        "subject": "语文",
                        "defaultModule": "国学阅读",
                        "canonicalUnit": unit_name,
                    })
                    lines.append(
                        "INSERT INTO `edu_catalog_node` "
                        "(`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) "
                        f"SELECT s.id, @{vol}_{code}, '{u_code}', '{sql_escape(unit_name)}', "
                        f"'{sql_escape(u_path)}', 2, 'unit', {uidx}, '{u_meta}', 1 "
                        "FROM `edu_catalog_scheme` s WHERE s.code = 'textbook_unit' LIMIT 1;"
                    )
                continue

            for li, leaf in enumerate(leaves, 1):
                l_code = f"{vol}_{code}_{slug(leaf)}"
                l_path = f"{pack_path}/{leaf}"
                l_meta = meta_json({
                    "volumeKey": vol,
                    "subject": "语文",
                    "defaultModule": module,
                })
                lines.append(
                    "INSERT INTO `edu_catalog_node` "
                    "(`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) "
                    f"SELECT s.id, @{vol}_{code}, '{l_code}', '{sql_escape(leaf)}', "
                    f"'{sql_escape(l_path)}', 2, 'leaf', {li}, '{l_meta}', 1 "
                    "FROM `edu_catalog_scheme` s WHERE s.code = 'textbook_unit' LIMIT 1;"
                )
            lines.append("")


def generate_sql() -> str:
    lines = [
        "-- ============================================================",
        "-- 35 七彩课堂 · 小学 1-6 语文 baseline",
        "-- 生成：python sql/tools/generate_qicai_primary_baseline.py",
        "-- 执行：mysql -u root -p xinketang < sql/35_seed_qicai_primary_baseline.sql",
        "-- 依赖：28_catalog_scheme.sql、27_brand_baseline.sql、34_seed_catalog_primary_2023_full.sql",
        "-- ============================================================",
        "USE `xinketang`;",
        "SET NAMES utf8mb4;",
        "",
    ]
    emit_modules(lines)
    emit_alias_table(lines)
    emit_pack_nodes(lines)
    lines.append("-- 完成")
    return "\n".join(lines) + "\n"


def main() -> None:
    out = Path(__file__).resolve().parent.parent / "35_seed_qicai_primary_baseline.sql"
    out.write_text(generate_sql(), encoding="utf-8")
    print(f"Wrote {out}")


if __name__ == "__main__":
    main()
