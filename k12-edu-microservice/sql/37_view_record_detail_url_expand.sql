-- 扩展浏览记录 detail_url 字段长度，避免长 query 入库失败
USE `xinketang`;
SET NAMES utf8mb4;

ALTER TABLE `view_record`
    MODIFY COLUMN `detail_url` VARCHAR(1024) DEFAULT '' COMMENT '详情页路径（含必要 query）';
