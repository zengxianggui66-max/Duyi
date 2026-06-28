package com.k12.resource.cache;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Admin 写操作后清首页缓存。
 */
@Aspect
@Component
public class HomeCacheEvictAspect {

    private final HomeCacheService homeCacheService;
    public HomeCacheEvictAspect(HomeCacheService homeCacheService) {
        this.homeCacheService = homeCacheService;
    }


    @AfterReturning(
            "(@annotation(org.springframework.web.bind.annotation.PutMapping) "
                    + "|| @annotation(org.springframework.web.bind.annotation.PostMapping) "
                    + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)) "
                    + "&& (within(com.k12.resource.controller.AdminHome*) "
                    + "|| within(com.k12.resource.controller.AdminOpsChannelController))")
    public void evictAfterAdminWrite() {
        homeCacheService.evictAll();
    }
}
