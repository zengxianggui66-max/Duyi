package com.k12.resource.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "k12.home")
public class HomeProperties {

    private Cache cache = new Cache();
    private Schedule schedule = new Schedule();

    @Data
    public static class Cache {
        /** C 端首页读缓存开关 */
        private boolean enabled = true;
        /** bootstrap 缓存 TTL（秒） */
        private int bootstrapTtlSeconds = 60;
    }

    @Data
    public static class Schedule {
        /** 定时上下线扫描 */
        private boolean enabled = true;
        /** cron 默认每分钟 */
        private String cron = "0 * * * * *";
    }
}
