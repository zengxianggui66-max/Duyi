-- P1 索引数据种子：静态频道入口 + 从 resource_search_index / article 迁移
-- mysql -u root -p xinketang < sql/42_sys_search_document_seed.sql
USE xinketang;
SET NAMES utf8mb4;

-- 1. 静态频道/入口（与 SearchDocumentSyncService.syncStaticEntries 对齐）
INSERT INTO sys_search_document (
  doc_id, doc_type, biz_id, title, subtitle, summary, content_text, keyword_text,
  channel_key, channel_name, route_path, status, is_deleted, quality_score, hot_score, publish_time
) VALUES
('channel:theme-class-meeting', 'channel', 'theme-class-meeting', '主题班会', '特色频道 / 德育', '主题班会频道入口', '主题班会频道入口', '班会,德育,成长', 'class_meeting', '特色频道', '/theme-class-meeting', 1, 0, 2, 10, NOW()),
('channel:career', 'feature', 'career', '生涯规划', '特色频道 / 升学指导', '生涯规划频道', '生涯规划频道', '生涯,志愿,升学', 'career', '特色频道', '/topic', 1, 0, 2, 10, NOW()),
('channel:culture', 'channel', 'culture', '传统文化', '特色频道 / 国学', '传统文化频道', '传统文化频道', '传统文化,国学,诗词', 'culture', '传统文化', '/culture', 1, 0, 2, 10, NOW()),
('channel:competition', 'channel', 'competition', '竞赛专区', '特色频道 / 奥赛', '竞赛专区频道', '竞赛专区频道', '竞赛,奥赛,奥数', 'competition', '竞赛专区', '/competition', 1, 0, 2, 10, NOW()),
('channel:topic', 'channel', 'topic', '专题资源', '特色频道 / 专题', '专题资源频道', '专题资源频道', '专题,备考,冲刺', 'topic', '专题资源', '/topic', 1, 0, 2, 10, NOW()),
('prep:lesson', 'prep', 'lesson', '备课中心', '备课专区 / 教案课件', '备课中心入口', '备课中心入口', '备课,教案,课件,学案', 'prep', '备课专区', '/lesson', 1, 0, 2, 10, NOW()),
('prep:smart', 'prep', 'smart-lesson', '智能备课', '备课专区 / AI', '智能备课入口', '智能备课入口', '智能备课,AI', 'prep', '备课专区', '/lesson/smart', 1, 0, 2, 10, NOW()),
('prep:assemble', 'prep', 'assemble', '组卷', '备课专区 / 试卷', '智能组卷入口', '智能组卷入口', '组卷,试卷,出题', 'prep', '备课专区', '/lesson/assemble', 1, 0, 2, 10, NOW()),
('news:hub', 'page', 'news', '教育资讯', '资讯频道入口', '教育资讯首页', '教育资讯首页', '资讯,新闻,政策,通知', 'news', '教育资讯', '/news', 1, 0, 2, 10, NOW()),
('news:notice', 'page', 'notice', '教育局通知', '教育资讯 / 通知', '教育局通知栏目', '教育局通知栏目', '教育局,通知,公告', 'news', '教育资讯', '/news/list?keyword=通知', 1, 0, 2, 9, NOW()),
('page:help', 'page', 'help', '帮助中心', '系统帮助', '帮助中心', '帮助中心', '帮助,FAQ,使用说明', NULL, NULL, '/help', 1, 0, 2, 5, NOW())
ON DUPLICATE KEY UPDATE
  title=VALUES(title), subtitle=VALUES(subtitle), summary=VALUES(summary),
  content_text=VALUES(content_text), keyword_text=VALUES(keyword_text),
  route_path=VALUES(route_path), hot_score=VALUES(hot_score), status=1, is_deleted=0;

-- 2. 学段学科浏览入口
INSERT INTO sys_search_document (
  doc_id, doc_type, biz_id, title, subtitle, summary, content_text, keyword_text,
  stage_key, stage_name, subject_key, subject_name, channel_key, channel_name,
  route_path, status, is_deleted, quality_score, hot_score, publish_time
)
SELECT
  CONCAT('subject:', s.code, ':', sub.code),
  'subject',
  CONCAT(s.code, ':', sub.code),
  CONCAT(s.name, sub.name),
  CONCAT(s.name, ' / ', sub.name, ' / 同步资源'),
  CONCAT(s.name, ' / ', sub.name, ' / 同步资源'),
  CONCAT(s.name, ' / ', sub.name, ' / 同步资源'),
  CONCAT(s.name, ',', sub.name),
  s.code, s.name, sub.code, sub.name,
  'stage_resource', '学段资源',
  CONCAT('/subject/', s.code, '/', sub.code, '/tongbian2024'),
  1, 0, 2, 8, NOW()
FROM edu_stage s
INNER JOIN edu_subject sub ON sub.stage_id = s.id
WHERE s.status = 1 AND sub.status = 1
ON DUPLICATE KEY UPDATE
  title=VALUES(title), route_path=VALUES(route_path), status=1, is_deleted=0;

-- 3. 从 P0+ 扁平索引迁移资源文档
INSERT INTO sys_search_document (
  doc_id, doc_type, biz_id, title, subtitle, summary, content_text, keyword_text,
  stage_key, stage_name, subject_key, subject_name, grade_key, grade_name,
  channel_key, channel_name, module_key, module_name, resource_type_key, resource_type_name,
  route_path, download_count, view_count, hot_score, vip_flag, status, is_deleted, publish_time, quality_score
)
SELECT
  rsi.doc_id,
  'resource',
  CAST(rsi.resource_id AS CHAR),
  rsi.title,
  CONCAT_WS(' / ', rsi.stage_name, rsi.subject, rsi.teaching_type),
  rsi.summary,
  rsi.search_text,
  rsi.search_text,
  rsi.stage_key, rsi.stage_name, NULL, rsi.subject,
  NULL, rsi.grade_name,
  rsi.channel_key, rsi.channel_name,
  NULL, rsi.module_name, NULL, rsi.teaching_type,
  rsi.detail_route,
  IFNULL(rsi.download_count, 0), IFNULL(rsi.view_count, 0),
  IFNULL(rsi.hot_score, 0), IFNULL(rsi.vip_flag, 0), 1, 0,
  rsi.publish_time, 1
FROM resource_search_index rsi
WHERE rsi.status = 1
ON DUPLICATE KEY UPDATE
  title=VALUES(title), summary=VALUES(summary), content_text=VALUES(content_text),
  route_path=VALUES(route_path), hot_score=VALUES(hot_score), status=1, is_deleted=0;

-- 4. 教育资讯文章
INSERT INTO sys_search_document (
  doc_id, doc_type, biz_id, title, subtitle, summary, content_text, keyword_text,
  channel_key, channel_name, module_key, module_name,
  route_path, cover_url, view_count, status, is_deleted, publish_time, quality_score, hot_score
)
SELECT
  CONCAT('news:', a.id),
  'news',
  CAST(a.id AS CHAR),
  a.title,
  CONCAT('教育资讯 / ', IFNULL(a.category_name, ''), IF(a.sub_category IS NOT NULL AND a.sub_category != '', CONCAT(' / ', a.sub_category), '')),
  IFNULL(a.summary, a.title),
  LEFT(REPLACE(REPLACE(IFNULL(a.content, ''), '<', ' '), '>', ' '), 8000),
  CONCAT_WS(' ', a.tags, a.category_name, a.sub_category, a.source_name),
  'news', '教育资讯',
  a.category, a.category_name,
  CONCAT('/news/', a.id),
  a.cover_url,
  IFNULL(a.view_count, 0), 1, 0,
  a.publish_time, 2,
  LOG(IFNULL(a.view_count, 0) + 1) + 2
FROM article a
WHERE a.status = 1 AND a.deleted = 0
ON DUPLICATE KEY UPDATE
  title=VALUES(title), summary=VALUES(summary), content_text=VALUES(content_text),
  route_path=VALUES(route_path), cover_url=VALUES(cover_url), status=1, is_deleted=0;

-- 5. edu_resource 维度资源（若扁平表未覆盖）
INSERT INTO sys_search_document (
  doc_id, doc_type, biz_id, title, subtitle, summary, content_text, keyword_text,
  stage_key, stage_name, subject_key, subject_name, grade_name,
  channel_key, channel_name, module_name, resource_type_name,
  route_path, download_count, view_count, status, is_deleted, publish_time, quality_score, hot_score
)
SELECT
  CONCAT('resource:', r.id),
  'resource',
  CAST(r.id AS CHAR),
  r.title,
  CONCAT_WS(' / ', s.name, sub.name, rt.name),
  IFNULL(r.description, r.title),
  CONCAT_WS(' ', r.description, u.name, m.name),
  CONCAT_WS(' ', m.name, rt.name, u.name, e.name),
  s.code, s.name, sub.code, sub.name, g.name,
  'stage_resource', '学段资源',
  m.name, rt.name,
  CONCAT('/resource/', r.id),
  IFNULL(r.download_count, 0), IFNULL(r.view_count, 0),
  1, 0, r.upload_time, 1,
  LOG(IFNULL(r.download_count, 0) + 1) * 2 + LOG(IFNULL(r.view_count, 0) + 1)
FROM edu_resource r
LEFT JOIN edu_resource_dimension rd ON r.id = rd.resource_id
LEFT JOIN edu_stage s ON rd.stage_id = s.id
LEFT JOIN edu_subject sub ON rd.subject_id = sub.id
LEFT JOIN edu_grade g ON rd.grade_id = g.id
LEFT JOIN edu_edition e ON rd.edition_id = e.id
LEFT JOIN edu_module m ON rd.module_id = m.id
LEFT JOIN edu_resource_type rt ON rd.resource_type_id = rt.id
LEFT JOIN edu_unit u ON rd.unit_id = u.id
WHERE r.is_deleted = 0 AND r.status = 1
ON DUPLICATE KEY UPDATE
  title=VALUES(title), route_path=VALUES(route_path), status=1, is_deleted=0;

SELECT 'seed_done' AS status,
  (SELECT COUNT(*) FROM sys_search_document WHERE status=1 AND is_deleted=0) AS total_docs,
  (SELECT COUNT(*) FROM sys_search_document WHERE doc_type IN ('channel','feature','page','prep')) AS channel_entries,
  (SELECT COUNT(*) FROM sys_search_document WHERE doc_type='news') AS news_docs,
  (SELECT COUNT(*) FROM sys_search_document WHERE doc_type='resource') AS resource_docs,
  (SELECT COUNT(*) FROM sys_search_document WHERE doc_type='subject') AS subject_docs;
