-- P3 搜索引擎影子接入：5 类量化就绪报表（MySQL 8+）
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/47_search_p3_readiness_reports.sql
USE xinketang;
SET NAMES utf8mb4;

SET @days := 7;
SET @since := DATE_SUB(NOW(), INTERVAL @days DAY);

SELECT '=== 1. 数据规模 ===' AS section;
SELECT COUNT(*) AS total_docs
FROM sys_search_document
WHERE status = 1 AND is_deleted = 0;

SELECT doc_type, COUNT(*) AS cnt
FROM sys_search_document
WHERE status = 1 AND is_deleted = 0
GROUP BY doc_type
ORDER BY cnt DESC;

SELECT channel_key, COUNT(*) AS cnt
FROM sys_search_document
WHERE status = 1 AND is_deleted = 0
GROUP BY channel_key
ORDER BY cnt DESC;

SELECT '=== 2. 耗时 P95/P99（近7天） ===' AS section;
SELECT
  COUNT(*) AS total_queries,
  ROUND(AVG(cost_ms), 2) AS avg_cost_ms,
  MAX(cost_ms) AS max_cost_ms
FROM search_query_log
WHERE create_time >= @since
  AND blocked_code IS NULL;

WITH ranked AS (
  SELECT
    cost_ms,
    ROW_NUMBER() OVER (ORDER BY cost_ms) AS rn,
    COUNT(*) OVER () AS total
  FROM search_query_log
  WHERE create_time >= @since
    AND blocked_code IS NULL
    AND cost_ms IS NOT NULL
)
SELECT cost_ms AS p95_cost_ms
FROM ranked
WHERE rn >= CEIL(total * 0.95)
ORDER BY rn
LIMIT 1;

WITH ranked AS (
  SELECT
    cost_ms,
    ROW_NUMBER() OVER (ORDER BY cost_ms) AS rn,
    COUNT(*) OVER () AS total
  FROM search_query_log
  WHERE create_time >= @since
    AND blocked_code IS NULL
    AND cost_ms IS NOT NULL
)
SELECT cost_ms AS p99_cost_ms
FROM ranked
WHERE rn >= CEIL(total * 0.99)
ORDER BY rn
LIMIT 1;

SELECT keyword, COUNT(*) AS query_count,
       ROUND(AVG(cost_ms), 2) AS avg_cost_ms,
       MAX(cost_ms) AS max_cost_ms
FROM search_query_log
WHERE create_time >= @since
  AND blocked_code IS NULL
GROUP BY keyword
ORDER BY max_cost_ms DESC
LIMIT 30;

SELECT '=== 3. 零结果词（近7天） ===' AS section;
SELECT
  COUNT(*) AS total_queries,
  SUM(CASE WHEN hit_count = 0 THEN 1 ELSE 0 END) AS zero_hit_queries,
  ROUND(SUM(CASE WHEN hit_count = 0 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS zero_result_rate_pct
FROM search_query_log
WHERE create_time >= @since
  AND blocked_code IS NULL;

SELECT keyword, normalized_keyword, COUNT(*) AS zero_count
FROM search_query_log
WHERE create_time >= @since
  AND hit_count = 0
  AND blocked_code IS NULL
GROUP BY keyword, normalized_keyword
ORDER BY zero_count DESC
LIMIT 50;

SELECT '=== 4. 点击转化 CTR（近7天） ===' AS section;
SELECT
  q.keyword,
  COUNT(DISTINCT q.id) AS query_count,
  COUNT(c.id) AS click_count,
  ROUND(COUNT(c.id) / COUNT(DISTINCT q.id), 4) AS ctr
FROM search_query_log q
LEFT JOIN search_click_log c
  ON c.keyword = q.keyword
 AND c.create_time >= q.create_time
 AND c.create_time < DATE_ADD(q.create_time, INTERVAL 30 MINUTE)
WHERE q.create_time >= @since
  AND q.blocked_code IS NULL
GROUP BY q.keyword
ORDER BY query_count DESC
LIMIT 50;

SELECT '=== 5. 点击位置质量（近7天） ===' AS section;
SELECT
  keyword,
  ROUND(AVG(position), 2) AS avg_click_position,
  SUM(CASE WHEN position <= 3 THEN 1 ELSE 0 END) AS top3_clicks,
  COUNT(*) AS total_clicks,
  ROUND(SUM(CASE WHEN position <= 3 THEN 1 ELSE 0 END) / COUNT(*), 4) AS top3_click_rate
FROM search_click_log
WHERE create_time >= @since
  AND click_type = 'result'
GROUP BY keyword
ORDER BY total_clicks DESC
LIMIT 50;
