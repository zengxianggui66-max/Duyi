package com.k12.resource.config;



import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;



import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;



@Data

@Component

@ConfigurationProperties(prefix = "k12.search")

public class SearchProperties {

    /** 单客户端（IP 或 userId）每分钟最大搜索次数 */

    private int rateLimitPerMinute = 30;

    /** 索引表为空时启动自动全量重建 */

    private boolean indexAutoRebuildOnEmpty = true;

    /** 未登录用户是否隐藏 VIP/精品资源 */

    private boolean hideVipForAnonymous = true;

    /** 敏感词（命中则拒绝搜索） */

    private List<String> sensitiveWords = new ArrayList<>(List.of(

            "赌博", "色情", "暴力", "反动", "邪教"

    ));

    /** 直跳判定阈值（可灰度调参） */

    private Redirect redirect = new Redirect();

    /** 内存缓存 TTL（秒） */

    private Cache cache = new Cache();

    /** 停用词 / 同义词（P2） */

    private Lexicon lexicon = new Lexicon();

    /** P3 搜索引擎（OpenSearch 影子接入 / 灰度切换） */

    private Engine engine = new Engine();



    @Data

    public static class Engine {

        private boolean enabled = false;

        private boolean shadow = true;

        private boolean fallbackToMysql = true;

        private String provider = "opensearch";

        private String indexName = "k12_search_document_v1";

        /** 逗号分隔，如 localhost:9200 */
        private String hosts = "";

        private String scheme = "http";

        private String username = "";

        private String password = "";

        /** 灰度流量 0-100，enabled=true 时生效 */
        private int trafficPercent = 0;

        public boolean isConfigured() {
            return hosts != null && !hosts.isBlank();
        }
    }



    @Data

    public static class Lexicon {

        private List<String> stopWords = new ArrayList<>();

        private Map<String, List<String>> synonyms = new LinkedHashMap<>();

    }



    @Data

    public static class Redirect {

        private boolean enabled = true;

        /** 首条结果最低得分 */

        private double minTopScore = 4.0;

        /** 首条与次条最小分差 */

        private double minScoreGap = 0.8;

        /** 是否要求标题与 query 完全一致（忽略大小写） */

        private boolean requireExactTitle = true;

    }



    @Data

    public static class Cache {

        private boolean enabled = true;

        private int suggestTtlSeconds = 60;

        private int hotTtlSeconds = 300;

        private int searchTtlSeconds = 30;

    }

}

