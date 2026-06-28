-- ============================================================
-- 新课堂教育 — 00 初始化数据库（统一库名 xinketang）
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `xinketang`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 废弃旧库名（若存在）
DROP DATABASE IF EXISTS `k12_edu`;

USE `xinketang`;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '数据库 xinketang 已就绪（已移除 k12_edu）' AS message;
