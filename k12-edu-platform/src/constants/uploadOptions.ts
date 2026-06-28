/**
 * @deprecated 仅作 taxonomySource / dictionarySource 在 API 失败时的离线兜底。
 * 禁止新业务直接引用；上传模块请使用 taxonomySource / dictionarySource。
 */
export type OptionItem = { label: string; value: string }
export type VersionGroup = { label: string; options: OptionItem[] }

// 学段对应的学科
export const subjectsByGrade = {
  preschool: [
    { label: '拼音识字', value: 'chinese' },
    { label: '数学启蒙', value: 'math' },
    { label: '习惯养成', value: 'habit' },
    { label: '综合活动', value: 'activity' }
  ],
  primary: [
    { label: '语文', value: 'chinese' },
    { label: '数学', value: 'math' },
    { label: '英语', value: 'english' },
    { label: '科学', value: 'science' },
    { label: 'STEAM', value: 'steam' },
    { label: '道德与法治', value: 'morality' },
    { label: '体育', value: 'pe' },
    { label: '音乐', value: 'music' },
    { label: '美术', value: 'art' }
  ],
  junior: [
    { label: '语文', value: 'chinese' },
    { label: '数学', value: 'math' },
    { label: '英语', value: 'english' },
    { label: '物理', value: 'physics' },
    { label: '化学', value: 'chemistry' },
    { label: '生物', value: 'biology' },
    { label: '历史', value: 'history' },
    { label: '地理', value: 'geography' },
    { label: '道德与法治', value: 'politics' }
  ],
  senior: [
    { label: '语文', value: 'chinese' },
    { label: '数学', value: 'math' },
    { label: '英语', value: 'english' },
    { label: '物理', value: 'physics' },
    { label: '化学', value: 'chemistry' },
    { label: '生物', value: 'biology' },
    { label: '历史', value: 'history' },
    { label: '地理', value: 'geography' },
    { label: '政治', value: 'politics' }
  ],
  art: [
    { label: '儿童创意美术', value: 'child_art' },
    { label: '素描', value: 'sketch' },
    { label: '水粉', value: 'gouache' },
    { label: '动漫', value: 'anime' },
    { label: '国画', value: 'chinese_painting' },
    { label: '书法', value: 'calligraphy' },
    { label: '手工', value: 'handcraft' },
    { label: '艺考美术', value: 'art_exam' }
  ],
  dance: [
    { label: '中国舞', value: 'chinese_dance' },
    { label: '民族舞', value: 'folk_dance' },
    { label: '古典舞', value: 'classical_dance' },
    { label: '拉丁舞', value: 'latin' },
    { label: '摩登舞', value: 'modern_dance' },
    { label: '街舞', value: 'hiphop' },
    { label: '芭蕾', value: 'ballet' },
    { label: '艺考舞蹈', value: 'dance_exam' }
  ]
}

// 学段对应的年级
export const gradesByGrade = {
  preschool: [
    { label: '中班', value: 'kindergarten_middle' },
    { label: '大班', value: 'kindergarten_senior' },
    { label: '幼小衔接', value: 'kindergarten_bridge' },
    { label: '暑假衔接', value: 'summer_bridge' }
  ],
  primary: [
    { label: '一年级', value: 'grade_1' },
    { label: '二年级', value: 'grade_2' },
    { label: '三年级', value: 'grade_3' },
    { label: '四年级', value: 'grade_4' },
    { label: '五年级', value: 'grade_5' },
    { label: '六年级', value: 'grade_6' }
  ],
  junior: [
    { label: '七年级', value: 'grade_7' },
    { label: '八年级', value: 'grade_8' },
    { label: '九年级', value: 'grade_9' }
  ],
  senior: [
    { label: '高一', value: 'grade_10' },
    { label: '高二', value: 'grade_11' },
    { label: '高三', value: 'grade_12' }
  ],
  art: [
    { label: '1级', value: 'level_1' },
    { label: '2级', value: 'level_2' },
    { label: '3级', value: 'level_3' },
    { label: '4级', value: 'level_4' },
    { label: '5级', value: 'level_5' },
    { label: '6级', value: 'level_6' },
    { label: '7级', value: 'level_7' },
    { label: '8级', value: 'level_8' },
    { label: '9级', value: 'level_9' },
    { label: '10级', value: 'level_10' },
    { label: '艺考', value: 'art_exam' }
  ],
  dance: [
    { label: '1级', value: 'level_1' },
    { label: '2级', value: 'level_2' },
    { label: '3级', value: 'level_3' },
    { label: '4级', value: 'level_4' },
    { label: '5级', value: 'level_5' },
    { label: '6级', value: 'level_6' },
    { label: '7级', value: 'level_7' },
    { label: '8级', value: 'level_8' },
    { label: '9级', value: 'level_9' },
    { label: '10级', value: 'level_10' },
    { label: '艺考', value: 'dance_exam' }
  ]
}

// 学段对应的教材版本
export const textbookVersionsByGrade = {
  preschool: [
    {
      label: '幼小衔接课程',
      options: [
        { label: '幼小衔接标准课程', value: 'bridge_standard' },
        { label: '拼音识字启蒙专项', value: 'pinyin_literacy' },
        { label: '数学启蒙专项', value: 'math_enlightenment' },
        { label: '入学准备与习惯养成', value: 'habit_ready' }
      ]
    },
    {
      label: '园本/机构课程',
      options: [
        { label: '幼儿园主题活动课程', value: 'kindergarten_theme' },
        { label: '机构定制启蒙课程', value: 'org_preschool' }
      ]
    }
  ],
  primary: [
    {
      label: '国内课标版',
      options: [
        { label: '人教版', value: 'pep' },
        { label: '北师大版', value: 'bsd' },
        { label: '苏教版', value: 'sjsz' },
        { label: '外研版', value: 'wy' },
        { label: '沪教版', value: 'shb' },
        { label: '教科版', value: 'jk' }
      ]
    },
    {
      label: '国际原版',
      options: [
        { label: '美国加州Treasures', value: 'us_treasures' },
        { label: '英国国家课程', value: 'uk_nc' }
      ]
    },
    {
      label: '机构定制版',
      options: [
        { label: '学而思版', value: 'xes' },
        { label: '新东方版', value: 'new_orien' }
      ]
    }
  ],
  junior: [
    {
      label: '国内课标版',
      options: [
        { label: '人教版', value: 'pep' },
        { label: '北师大版', value: 'bsd' },
        { label: '苏教版', value: 'sjsz' },
        { label: '沪教版', value: 'shb' },
        { label: '教科版', value: 'jk' }
      ]
    },
    {
      label: '国际原版',
      options: [
        { label: 'IB课程', value: 'ib' },
        { label: 'AP课程', value: 'ap' }
      ]
    },
    {
      label: '中考专题定制版',
      options: [
        { label: '中考一轮复习', value: 'zk_review_1' },
        { label: '中考二轮复习', value: 'zk_review_2' }
      ]
    }
  ],
  senior: [
    {
      label: '国内课标版',
      options: [
        { label: '人教A版', value: 'pep_a' },
        { label: '人教B版', value: 'pep_b' },
        { label: '北师大版', value: 'bsd' },
        { label: '苏教版', value: 'sjsz' },
        { label: '沪教版', value: 'shb' }
      ]
    },
    {
      label: '国际原版',
      options: [
        { label: 'IB课程', value: 'ib' },
        { label: 'AP课程', value: 'ap' },
        { label: 'A-Level', value: 'alevel' }
      ]
    },
    {
      label: '高考专题定制版',
      options: [
        { label: '高考一轮复习', value: 'gk_review_1' },
        { label: '高考二轮复习', value: 'gk_review_2' },
        { label: '艺考文化课', value: 'art_culture' }
      ]
    }
  ],
  art: [
    {
      label: '考级教材',
      options: [
        { label: '国美考级', value: 'cma' },
        { label: '舞协考级', value: 'cda' },
        { label: '北舞考级', value: 'bda' }
      ]
    },
    {
      label: '机构自编教程',
      options: [
        { label: '机构定制', value: 'custom' }
      ]
    },
    {
      label: '艺考专用教材',
      options: [
        { label: '联考教材', value: 'art_union' },
        { label: '校考教材', value: 'art_school' }
      ]
    }
  ],
  dance: [
    {
      label: '考级教材',
      options: [
        { label: '国美考级', value: 'cma' },
        { label: '舞协考级', value: 'cda' },
        { label: '北舞考级', value: 'bda' }
      ]
    },
    {
      label: '机构自编教程',
      options: [
        { label: '机构定制', value: 'custom' }
      ]
    },
    {
      label: '艺考专用教材',
      options: [
        { label: '联考教材', value: 'dance_union' },
        { label: '校考教材', value: 'dance_school' }
      ]
    }
  ]
}

// 学段对应的备考类型
export const examTypesByGrade = {
  preschool: [
    { label: '幼小衔接', value: 'kindergarten_bridge' },
    { label: '入学准备', value: 'school_ready' },
    { label: '习惯养成', value: 'habit_ready' }
  ],
  primary: [
    { label: '校内同步', value: 'school_sync' },
    { label: '小升初择校', value: 'primary_exam' },
    { label: 'KET/PET', value: 'ket_pet' },
    { label: '奥数', value: 'olympiad' },
    { label: '编程考级', value: 'coding_exam' }
  ],
  junior: [
    { label: '校内同步', value: 'school_sync' },
    { label: '中考冲刺', value: 'middle_exam' },
    { label: '学科竞赛', value: 'competition' },
    { label: '英语能力竞赛', value: 'english_contest' }
  ],
  senior: [
    { label: '校内同步', value: 'school_sync' },
    { label: '高考冲刺', value: 'college_entrance' },
    { label: '艺体考文化课', value: 'art_culture' },
    { label: '学科竞赛', value: 'competition' },
    { label: '留学英语备考', value: 'ielts_toefl' }
  ],
  art: [
    { label: '考级(1-10级)', value: 'art_level_exam' },
    { label: '艺考备考', value: 'art_exam_prep' },
    { label: '艺术特长', value: 'art_talent' },
    { label: '比赛备赛', value: 'art_competition' }
  ],
  dance: [
    { label: '考级(1-10级)', value: 'dance_level_exam' },
    { label: '艺考备考', value: 'dance_exam_prep' },
    { label: '艺术特长', value: 'dance_talent' },
    { label: '比赛备赛', value: 'dance_competition' }
  ]
}
