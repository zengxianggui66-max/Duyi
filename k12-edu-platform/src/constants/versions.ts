/**
 * 教材版本配置（按学段分组）
 */
export interface VersionInfo {
  key: string
  name: string
  shortName: string
}

export const versions: Record<string, VersionInfo[]> = {
  preschool: [
    { key: 'bridge_standard', name: '幼小衔接标准课程', shortName: '幼小衔接' },
    { key: 'kindergarten_theme', name: '幼儿园主题活动课程', shortName: '主题活动' },
    { key: 'pinyin_literacy', name: '拼音识字启蒙专项', shortName: '拼音识字' },
    { key: 'math_enlightenment', name: '数学启蒙专项', shortName: '数学启蒙' },
    { key: 'habit_ready', name: '入学准备与习惯养成', shortName: '入学准备' },
  ],
  primary: [
    { key: 'pep_primary', name: '人教版（部编版）', shortName: '人教·部编' },
    { key: 'bsd_primary', name: '北师大版', shortName: '北师大' },
    { key: 'suke', name: '苏教版', shortName: '苏教' },
    { key: 'waiyan_primary', name: '外研版', shortName: '外研' },
    { key: 'hujiao', name: '沪教版', shortName: '沪教' },
    { key: 'oxford_discover', name: 'Oxford Discover', shortName: 'OD' },
    { key: 'power_up', name: 'Power Up', shortName: 'PU' },
    { key: 'get_smart', name: 'Get Smart', shortName: 'GS' },
    { key: 'national_geo', name: '国家地理启蒙系列', shortName: 'NG' },
    { key: 'treasures', name: 'Treasures', shortName: 'Treas' },
    { key: 'tiered_basic', name: '分层·基础版', shortName: '分层基础' },
    { key: 'tiered_standard', name: '分层·提升版', shortName: '分层提升' },
    { key: 'tiered_advanced', name: '分层·培优版', shortName: '分层培优' },
    { key: 'special_math', name: '专项·计算/奥数', shortName: '专项·计算' },
    { key: 'special_reading', name: '专项·阅读/作文', shortName: '专项·阅读' },
    { key: 'steam_course', name: 'STEAM课程', shortName: 'STEAM' },
    { key: 'coding_intro', name: '编程启蒙教材', shortName: '编程' },
    { key: 'guoxue', name: '国学/传统文化教材', shortName: '国学' },
  ],
  junior: [
    { key: 'pep_junior', name: '人教版（部编版）', shortName: '人教·部编' },
    { key: 'bsd_junior', name: '北师大版', shortName: '北师大' },
    { key: 'suke_junior', name: '苏教版', shortName: '苏教' },
    { key: 'waiyan_junior', name: '外研版', shortName: '外研' },
    { key: 'hujiao_junior', name: '沪教版', shortName: '沪教' },
    { key: 'xiangjiao', name: '湘教版（地理）', shortName: '湘教' },
    { key: 'lujiao', name: '鲁教版（化学）', shortName: '鲁教' },
    { key: 'think', name: 'Think', shortName: 'Think' },
    { key: 'unlock', name: 'Unlock', shortName: 'Unlock' },
    { key: 'oxford_discover_j', name: 'Oxford Discover高阶版', shortName: 'OD高阶' },
    { key: 'tiered_basic_j', name: '分层·基础版', shortName: '分层基础' },
    { key: 'tiered_standard_j', name: '分层·提升版', shortName: '分层提升' },
    { key: 'tiered_sprint_j', name: '分层·冲刺版', shortName: '分层冲刺' },
    { key: 'special_zhongkao_func', name: '中考专题·函数', shortName: '函数' },
    { key: 'special_zhongkao_grammar', name: '中考专题·语法', shortName: '语法' },
    { key: 'special_zhongkao_reading', name: '中考专题·阅读', shortName: '阅读' },
    { key: 'special_zhongkao_composition', name: '中考专题·作文', shortName: '作文' },
    { key: 'steam_junior', name: 'STEAM课程', shortName: 'STEAM' },
    { key: 'coding_advanced', name: '编程进阶教材', shortName: '编程进阶' },
  ],
  senior: [
    { key: 'pep_senior', name: '人教版（部编版）', shortName: '人教·部编' },
    { key: 'xiangjiao_senior', name: '湘教版（地理）', shortName: '湘教' },
    { key: 'lujiao_senior', name: '鲁教版（化学）', shortName: '鲁教' },
    { key: 'waiyan_senior', name: '外研版', shortName: '外研' },
    { key: 'think_senior', name: 'Think高阶版', shortName: 'Think' },
    { key: 'unlock_senior', name: 'Unlock', shortName: 'Unlock' },
    { key: 'ielts_toefl', name: '雅思/托福备考教材', shortName: '雅思/托福' },
    { key: 'tiered_basic_s', name: '分层·基础版', shortName: '分层基础' },
    { key: 'tiered_standard_s', name: '分层·提升版', shortName: '分层提升' },
    { key: 'tiered_sprint_s', name: '分层·冲刺版', shortName: '分层冲刺' },
    { key: 'special_gaokao_func', name: '高考专题·函数/导数', shortName: '函数/导数' },
    { key: 'special_gaokao_reading', name: '高考专题·阅读/写作', shortName: '阅读/写作' },
    { key: 'special_gaokao_lizong', name: '高考专题·理综', shortName: '理综' },
    { key: 'special_gaokao_wenzong', name: '高考专题·文综', shortName: '文综' },
    { key: 'art_culture', name: '艺考生文化补弱教材', shortName: '艺考文化' },
    { key: 'competition_training', name: '竞赛集训教材', shortName: '竞赛' },
  ],
  art: [
    { key: 'cagf_art', name: '中国美术学院考级教材', shortName: '国美考级' },
    { key: 'moc_art', name: '文化部艺术发展中心考级教材', shortName: '文旅部考级' },
    { key: 'calligraphy_exam', name: '中国书法家协会书法考级教材', shortName: '书法考级' },
    { key: 'national_art_exam', name: '全国美术考级指定教材', shortName: '全国考级' },
    { key: 'local_art', name: '地方美育协会自编教材', shortName: '地方教材' },
    { key: 'org_art_advance', name: '机构内部考级进阶教材', shortName: '机构教材' },
    { key: 'art_sketch_manual', name: '素描临摹手册', shortName: '素描手册' },
    { key: 'art_color_manual', name: '色彩临摹手册', shortName: '色彩手册' },
    { key: 'art_design_manual', name: '设计基础教程', shortName: '设计教程' },
    { key: 'art_digital_manual', name: '数字绘画教程', shortName: '数绘教程' },
  ],
  dance: [
    { key: 'cda_chinese', name: '中国舞蹈家协会考级教材（中国舞1-10级）', shortName: '舞协考级' },
    { key: 'bda_manual', name: '北京舞蹈学院考级教材', shortName: '北舞教材' },
    { key: 'folk_dance_manual', name: '民族民间舞考级教材', shortName: '民舞教材' },
    { key: 'latin_dance_manual', name: '拉丁舞考级教材（ISDA/CBDF）', shortName: '拉丁考级' },
    { key: 'ballet_dance_manual', name: '芭蕾舞考级教材', shortName: '芭蕾考级' },
    { key: 'street_dance_manual', name: '街舞考级教材', shortName: '街舞考级' },
    { key: 'org_dance_advance', name: '机构内部进阶舞蹈教材', shortName: '机构教材' },
    { key: 'chinese_classical', name: '中国古典舞身韵教程', shortName: '古典舞身韵' },
    { key: 'ballroom_std', name: '摩登舞标准教程', shortName: '摩登教程' },
    { key: 'hiphop_manual', name: '流行舞/爵士舞教程', shortName: '流行舞' },
    { key: 'dance_composition', name: '舞蹈编导基础教程', shortName: '编导教程' },
  ],
}

/**
 * 根据学段获取版本列表
 */
export function getVersionsByGrade(level: string): VersionInfo[] {
  return versions[level] || []
}

/**
 * 根据 key 获取版本名称
 */
export function getVersionName(key: string, level?: string): string {
  const list = level ? versions[level] : Object.values(versions).flat()
  return list.find(v => v.key === key)?.name || key
}
