-- 为专题资源补充 file_url，便于资料篮批量下载联调
-- mysql -u root -p xinketang < sql/20_topic_resource_file_url.sql
USE `xinketang`;

UPDATE `topic_resource`
SET `file_url` = 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf'
WHERE `id` BETWEEN 5101 AND 5110
  AND (`file_url` IS NULL OR `file_url` = '');
