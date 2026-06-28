package com.k12.resource.cache;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.k12.common.entity.HomeBanner;
import com.k12.common.entity.HomeHotWord;
import com.k12.resource.mapper.HomeBannerMapper;
import com.k12.resource.mapper.HomeHotWordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Phase 7-F：扫描 start_time / end_time 自动上下线。
 */
@Slf4j
@Service
public class HomeScheduleService {

    private final HomeBannerMapper bannerMapper;
    private final HomeHotWordMapper hotWordMapper;
    private final HomeCacheService homeCacheService;
    public HomeScheduleService(HomeBannerMapper bannerMapper, HomeHotWordMapper hotWordMapper, HomeCacheService homeCacheService) {
        this.bannerMapper = bannerMapper;
        this.hotWordMapper = hotWordMapper;
        this.homeCacheService = homeCacheService;
    }


    @Scheduled(cron = "${k12.home.schedule.cron:0 * * * * *}")
    public void scanScheduledItems() {
        LocalDateTime now = LocalDateTime.now();
        int off = disableExpired(now);
        int on = enableStarted(now);
        if (off > 0 || on > 0) {
            homeCacheService.evictAll();
            log.info("Home schedule tick: disabled={}, enabled={}", off, on);
        }
    }

    private int disableExpired(LocalDateTime now) {
        LambdaUpdateWrapper<HomeBanner> bannerOff = new LambdaUpdateWrapper<>();
        bannerOff.eq(HomeBanner::getStatus, 1)
                .isNotNull(HomeBanner::getEndTime)
                .lt(HomeBanner::getEndTime, now)
                .set(HomeBanner::getStatus, 0)
                .set(HomeBanner::getUpdateTime, now);
        int b = bannerMapper.update(null, bannerOff);

        LambdaUpdateWrapper<HomeHotWord> hotOff = new LambdaUpdateWrapper<>();
        hotOff.eq(HomeHotWord::getStatus, 1)
                .isNotNull(HomeHotWord::getEndTime)
                .lt(HomeHotWord::getEndTime, now)
                .set(HomeHotWord::getStatus, 0)
                .set(HomeHotWord::getUpdateTime, now);
        int h = hotWordMapper.update(null, hotOff);
        return b + h;
    }

    private int enableStarted(LocalDateTime now) {
        LambdaUpdateWrapper<HomeBanner> bannerOn = new LambdaUpdateWrapper<>();
        bannerOn.eq(HomeBanner::getStatus, 0)
                .isNotNull(HomeBanner::getStartTime)
                .le(HomeBanner::getStartTime, now)
                .and(w -> w.isNull(HomeBanner::getEndTime).or().gt(HomeBanner::getEndTime, now))
                .set(HomeBanner::getStatus, 1)
                .set(HomeBanner::getUpdateTime, now);
        int b = bannerMapper.update(null, bannerOn);

        LambdaUpdateWrapper<HomeHotWord> hotOn = new LambdaUpdateWrapper<>();
        hotOn.eq(HomeHotWord::getStatus, 0)
                .isNotNull(HomeHotWord::getStartTime)
                .le(HomeHotWord::getStartTime, now)
                .and(w -> w.isNull(HomeHotWord::getEndTime).or().gt(HomeHotWord::getEndTime, now))
                .set(HomeHotWord::getStatus, 1)
                .set(HomeHotWord::getUpdateTime, now);
        int h = hotWordMapper.update(null, hotOn);
        return b + h;
    }

    /** 供 Admin 手动触发 */
    public int runOnce() {
        LocalDateTime now = LocalDateTime.now();
        int total = disableExpired(now) + enableStarted(now);
        homeCacheService.evictAll();
        return total;
    }
}
