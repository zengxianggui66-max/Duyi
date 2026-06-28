#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成小学 1-6 年级全科目录 SQL（2023 版课标结构）"""
from __future__ import annotations

import json
import re
from pathlib import Path

from primary_chinese_curriculum import CHINESE_UNITS

SCHEME_CODE = "textbook_unit"
VOLUME_KEYS = [
    "y1s1", "y1s2", "y2s1", "y2s2", "y3s1", "y3s2",
    "y4s1", "y4s2", "y5s1", "y5s2", "y6s1", "y6s2",
]

# volumeKey -> { subject -> { edition, units: [(unit_name, [lessons])] } }
CURRICULUM: dict[str, dict[str, dict]] = {}

def _u(name: str, lessons: list[str] | str):
    if isinstance(lessons, str):
        lessons = [x.strip() for x in re.split(r"[、,，]", lessons) if x.strip()]
    return (name.strip(), lessons)


def _add(vol: str, subject: str, edition: str, units: list):
    CURRICULUM.setdefault(vol, {})[subject] = {"edition": edition, "units": units}


# ---------- 一年级上册 ----------
_add("y1s1", "数学", "人教版", [
    _u("1.准备课", ["数一数", "比多少"]),
    _u("2.位置", ["上下前后", "左右"]),
    _u("3.1-5认识与加减法", ["认识数字", "比大小", "分与合", "加减运算", "0的运算"]),
    _u("4.认识图形", ["长方体", "正方体", "圆柱", "球"]),
    _u("5.6-10认识与加减法", ["6-10认识", "连加连减", "加减混合"]),
    _u("6.11-20各数认识", ["数的认读", "十加几运算"]),
    _u("7.认识钟表", ["认识整时"]),
    _u("8.20以内进位加法", ["9加几", "8、7、6加几", "5、4、3、2加几"]),
])
_add("y1s1", "英语", "人教PEP", [
    _u("Unit1 School", ["教室物品"]),
    _u("Unit2 Face", ["身体五官"]),
    _u("Unit3 Animals", ["小动物"]),
    _u("Unit4 Numbers", ["数字1-10"]),
    _u("Unit5 Colours", ["颜色"]),
    _u("Unit6 Fruit", ["水果"]),
])

# ---------- 一年级下册 ----------
_add("y1s2", "数学", "人教版", [
    _u("1.认识平面图形", ["长方形", "正方形", "圆", "平行四边形", "三角形"]),
    _u("2.20以内退位减法", ["十几减9", "十几减8、7、6", "十几减5、4、3、2"]),
    _u("3.分类与整理", ["分类与整理"]),
    _u("4.100以内数的认识", ["数数", "读写", "大小比较"]),
    _u("5.认识人民币", ["元角分换算", "简单购物计算"]),
    _u("6.100以内加减法一", ["整十数加减", "两位数加减"]),
    _u("7.找规律", ["找规律"]),
])
_add("y1s2", "英语", "人教PEP", [
    _u("Unit1 Classroom", ["教室设施"]),
    _u("Unit2 Schoolbag", ["文具"]),
    _u("Unit3 Animals", ["陆地动物"]),
    _u("Unit4 Food", ["主食零食"]),
    _u("Unit5 Drink", ["饮品"]),
    _u("Unit6 Clothes", ["服饰"]),
])

# ---------- 二年级上册 ----------
_add("y2s1", "数学", "人教版", [
    _u("1.长度单位", ["厘米", "米", "线段"]),
    _u("2.100以内进退位加减法", ["100以内进退位加减法"]),
    _u("3.角的初步认识", ["直角", "锐角", "钝角"]),
    _u("4.表内乘法一", ["1-6乘法口诀"]),
    _u("5.观察物体一", ["观察物体"]),
    _u("6.表内乘法二", ["7-9乘法口诀"]),
    _u("7.认识时间", ["几时几分"]),
    _u("8.数学广角搭配", ["搭配问题"]),
])
_add("y2s1", "英语", "人教PEP", [
    _u("Unit1 My Family", ["家人"]),
    _u("Unit2 My Friends", ["朋友"]),
    _u("Unit3 My Body", ["身体部位"]),
    _u("Unit4 Time", ["时间"]),
    _u("Unit5 My Clothes", ["衣物"]),
    _u("Unit6 My Home", ["家居"]),
])

# ---------- 二年级下册 ----------
_add("y2s2", "数学", "人教版", [
    _u("1.数据收集整理", ["数据收集整理"]),
    _u("2.表内除法一", ["平均分", "除法算式", "乘法口诀求商"]),
    _u("3.图形运动", ["轴对称", "平移旋转"]),
    _u("4.表内除法二", ["7-9口诀求商"]),
    _u("5.混合运算", ["混合运算"]),
    _u("6.有余数的除法", ["有余数的除法"]),
    _u("7.万以内数的认识", ["万以内数的认识"]),
    _u("8.克与千克", ["克与千克"]),
    _u("9.推理", ["推理"]),
])
_add("y2s2", "英语", "人教PEP", [
    _u("Unit1 My School", ["校园"]),
    _u("Unit2 My Classroom", ["教室"]),
    _u("Unit3 My Day", ["日常作息"]),
    _u("Unit4 Weather", ["天气"]),
    _u("Unit5 Seasons", ["四季"]),
    _u("Unit6 Travel", ["出行"]),
])

# ---------- 三年级上册 ----------
_add("y3s1", "数学", "人教版", [
    _u("1.时分秒", ["时分秒"]),
    _u("2.万以内加减法一", ["万以内加减法一"]),
    _u("3.测量", ["毫米分米千米", "吨"]),
    _u("4.万以内加减法二", ["万以内加减法二"]),
    _u("5.倍的认识", ["倍的认识"]),
    _u("6.多位数乘一位数", ["多位数乘一位数"]),
    _u("7.长方形正方形周长", ["长方形正方形周长"]),
    _u("8.分数初步认识", ["分数初步认识"]),
    _u("9.集合", ["集合"]),
])
_add("y3s1", "英语", "人教PEP", [
    _u("Unit1 Hello", ["问候"]),
    _u("Unit2 Colours", ["颜色"]),
    _u("Unit3 My schoolbag", ["书包"]),
    _u("Unit4 My home", ["家"]),
    _u("Unit5 Dinner ready", ["晚餐"]),
    _u("Unit6 How many", ["数量"]),
])

# ---------- 三年级下册 ----------
_add("y3s2", "数学", "人教版", [
    _u("1.位置与方向", ["位置与方向"]),
    _u("2.除数是一位数的除法", ["除数是一位数的除法"]),
    _u("3.复式统计表", ["复式统计表"]),
    _u("4.两位数乘两位数", ["两位数乘两位数"]),
    _u("5.面积", ["面积"]),
    _u("6.年月日", ["年月日"]),
    _u("7.小数初步认识", ["小数初步认识"]),
    _u("8.搭配问题", ["搭配问题"]),
])
_add("y3s2", "英语", "人教PEP", [
    _u("Unit1 Welcome back", ["返校"]),
    _u("Unit2 My favourite season", ["季节"]),
    _u("Unit3 My school calendar", ["校历"]),
    _u("Unit4 When is", ["日期"]),
    _u("Unit5 Whose", ["所属"]),
    _u("Unit6 Work quietly", ["行为规范"]),
])

# ---------- 四年级上册 ----------
_add("y4s1", "数学", "人教版", [
    _u("1.大数的认识", ["大数的认识"]),
    _u("2.公顷平方千米", ["公顷平方千米"]),
    _u("3.角的度量", ["角的度量"]),
    _u("4.三位数乘两位数", ["三位数乘两位数"]),
    _u("5.平行四边形梯形", ["平行四边形梯形"]),
    _u("6.除数是两位数除法", ["除数是两位数除法"]),
    _u("7.条形统计图", ["条形统计图"]),
    _u("8.优化", ["优化"]),
])
_add("y4s1", "英语", "人教PEP", [
    _u("Unit1 Where is", ["方位"]),
    _u("Unit2 Ways to go school", ["交通方式"]),
    _u("Unit3 Weekend plan", ["周末计划"]),
    _u("Unit4 Pen pal", ["笔友"]),
    _u("Unit5 Jobs", ["职业"]),
    _u("Unit6 Feelings", ["情绪"]),
])

# ---------- 四年级下册 ----------
_add("y4s2", "数学", "人教版", [
    _u("1.四则运算", ["四则运算"]),
    _u("2.观察物体二", ["观察物体二"]),
    _u("3.运算定律", ["运算定律"]),
    _u("4.小数意义性质", ["小数意义性质"]),
    _u("5.三角形", ["三角形"]),
    _u("6.小数加减法", ["小数加减法"]),
    _u("7.图形运动", ["图形运动"]),
    _u("8.平均数鸡兔同笼", ["平均数", "鸡兔同笼"]),
])
_add("y4s2", "英语", "人教PEP", [
    _u("Unit1 Taller", ["身高体型"]),
    _u("Unit2 Last weekend", ["过去周末"]),
    _u("Unit3 Where did you go", ["出行过去式"]),
    _u("Unit4 Then and now", ["今昔对比"]),
])

# ---------- 五年级上册 ----------
_add("y5s1", "数学", "人教版", [
    _u("1.小数乘法", ["小数乘法"]),
    _u("2.位置", ["位置"]),
    _u("3.小数除法", ["小数除法"]),
    _u("4.可能性", ["可能性"]),
    _u("5.简易方程", ["简易方程"]),
    _u("6.多边形面积", ["多边形面积"]),
    _u("7.植树问题", ["植树问题"]),
])
_add("y5s1", "英语", "人教PEP", [
    _u("Unit1 复习重难点", ["一般过去式", "比较级"]),
])

# ---------- 五年级下册 ----------
_add("y5s2", "数学", "人教版", [
    _u("1.观察物体三", ["观察物体三"]),
    _u("2.因数与倍数", ["因数与倍数"]),
    _u("3.长方体正方体", ["长方体正方体"]),
    _u("4.分数意义性质", ["分数意义性质"]),
    _u("5.图形旋转", ["图形旋转"]),
    _u("6.分数加减法", ["分数加减法"]),
    _u("7.折线统计图", ["折线统计图"]),
    _u("8.找次品", ["找次品"]),
])
_add("y5s2", "英语", "人教PEP", [
    _u("Unit1 复习重难点", ["一般过去式", "比较级"]),
])

# ---------- 六年级上册 ----------
_add("y6s1", "数学", "人教版", [
    _u("1.分数乘法", ["分数乘法"]),
    _u("2.位置与方向二", ["位置与方向二"]),
    _u("3.分数除法", ["分数除法"]),
    _u("4.比", ["比"]),
    _u("5.圆", ["圆"]),
    _u("6.百分数一", ["百分数一"]),
    _u("7.扇形统计图", ["扇形统计图"]),
    _u("8.数与形", ["数与形"]),
])
_add("y6s1", "英语", "人教PEP", [
    _u("Unit1 复习", ["小学英语综合"]),
])

# ---------- 六年级下册 ----------
_add("y6s2", "数学", "人教版", [
    _u("1.负数", ["负数"]),
    _u("2.百分数二", ["百分数二"]),
    _u("3.圆柱圆锥", ["圆柱圆锥"]),
    _u("4.比例", ["比例"]),
    _u("5.鸽巢问题", ["鸽巢问题"]),
    _u("6.小学总复习", ["小学总复习"]),
])
_add("y6s2", "英语", "人教PEP", [
    _u("Unit1 毕业复习", ["小学英语综合"]),
])

# 统编语文（详见 primary_chinese_curriculum.py）
for _vol, _units in CHINESE_UNITS.items():
    _add(_vol, "语文", "统编版", _units)


def slug(s: str) -> str:
    s = re.sub(r"[^\w\u4e00-\u9fff]+", "_", s)
    return s[:40] or "n"


def meta_json(obj: dict) -> str:
    return json.dumps(obj, ensure_ascii=False).replace("'", "\\'")


def sql_escape(s: str) -> str:
    return s.replace("\\", "\\\\").replace("'", "''")


def generate_sql() -> str:
    lines: list[str] = [
        "-- ============================================================",
        "-- 小学 1-6 年级全科目录（2023 版：统编语文 + 人教数学 + 人教 PEP 英语）",
        "-- 生成：python sql/tools/generate_primary_catalog.py",
        "-- 执行：mysql -u root -p xinketang < sql/34_seed_catalog_primary_2023_full.sql",
        "-- 依赖：28_catalog_scheme.sql、27_brand_baseline.sql",
        "-- ============================================================",
        "USE `xinketang`;",
        "SET NAMES utf8mb4;",
        "",
        "SET @scheme_id = (SELECT `id` FROM `edu_catalog_scheme` WHERE `code` = 'textbook_unit' LIMIT 1);",
        "",
        "-- 清理旧的小学册别目录（仅含 volumeKey 的节点）",
    ]
    keys_sql = ", ".join(f"'{k}'" for k in VOLUME_KEYS)
    lines.append(
        f"DELETE FROM `edu_catalog_node` WHERE `scheme_id` = @scheme_id "
        f"AND JSON_UNQUOTE(JSON_EXTRACT(`meta`, '$.volumeKey')) IN ({keys_sql});"
    )
    lines.append("")

    subj_sort = {"语文": 1, "数学": 2, "英语": 3}

    for vol in VOLUME_KEYS:
        if vol not in CURRICULUM:
            continue
        subjects = CURRICULUM[vol]
        for subj in sorted(subjects.keys(), key=lambda s: subj_sort.get(s, 9)):
            info = subjects[subj]
            edition = info["edition"]
            subj_label = f"{subj}（{edition}）" if edition else subj
            root_code = f"{vol}_{slug(subj)}_root"
            root_path = f"/{subj_label}"
            root_meta = meta_json({
                "volumeKey": vol,
                "subject": subj,
                "edition": edition,
                "defaultModule": "同步备课",
            })
            lines.append(
                f"-- {vol} {subj_label}\n"
                f"INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES\n"
                f"(@scheme_id, 0, '{root_code}', '{sql_escape(subj_label)}', '{sql_escape(root_path)}', 0, 'folder', {subj_sort.get(subj, 9)}, '{root_meta}', 1);"
            )
            lines.append(f"SET @{vol}_{slug(subj)}_root = LAST_INSERT_ID();")
            lines.append("")

            u_idx = 0
            for unit_name, lessons in info["units"]:
                u_idx += 1
                u_code = f"{vol}_{slug(subj)}_u{u_idx:02d}"
                u_path = f"{root_path}/{unit_name}"
                u_meta = meta_json({"volumeKey": vol, "subject": subj, "edition": edition})
                lines.append(
                    f"INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES\n"
                    f"(@scheme_id, @{vol}_{slug(subj)}_root, '{u_code}', '{sql_escape(unit_name)}', '{sql_escape(u_path)}', 1, 'unit', {u_idx}, '{u_meta}', 1);"
                )
                lines.append(f"SET @{u_code} = LAST_INSERT_ID();")

                if lessons:
                    lesson_rows = []
                    for l_idx, lesson in enumerate(lessons, 1):
                        l_code = f"{u_code}_l{l_idx:02d}"
                        l_path = f"{u_path}/{lesson}"
                        l_meta = meta_json({"volumeKey": vol, "subject": subj})
                        lesson_rows.append(
                            f"(@scheme_id, @{u_code}, '{l_code}', '{sql_escape(lesson)}', '{sql_escape(l_path)}', 2, 'lesson', {l_idx}, '{l_meta}', 1)"
                        )
                    lines.append(
                        "INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES\n"
                        + ",\n".join(lesson_rows)
                        + ";\n"
                    )
                else:
                    lines.append("")

    lines.append("-- 完成")
    return "\n".join(lines)


def main():
    out = Path(__file__).resolve().parent.parent / "34_seed_catalog_primary_2023_full.sql"
    out.write_text(generate_sql(), encoding="utf-8")
    print(f"Wrote {out} ({out.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main()
