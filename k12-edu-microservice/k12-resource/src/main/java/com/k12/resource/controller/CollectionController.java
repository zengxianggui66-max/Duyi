package com.k12.resource.controller;



import com.k12.common.Result;

import com.k12.common.constant.ResourceTypeConstants;

import com.k12.common.dto.CollectQueryDTO;

import com.k12.common.dto.CollectStatsVO;

import com.k12.common.entity.Collection;

import com.k12.resource.security.UserIdResolver;

import com.k12.resource.service.CollectionService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;



import java.util.List;

import java.util.Map;



/**

 * 收藏接口

 */

@RestController

@RequestMapping("/api/resource/collect")
public class CollectionController {



    private final CollectionService collectionService;

    private final UserIdResolver userIdResolver;
    public CollectionController(CollectionService collectionService, UserIdResolver userIdResolver) {
        this.collectionService = collectionService;
        this.userIdResolver = userIdResolver;
    }




    @GetMapping("/list")

    public Result<List<Collection>> list(HttpServletRequest request) {

        Long userId = userIdResolver.resolve(request);

        if (userId == null) {

            return Result.fail("请先登录");

        }

        return Result.success(collectionService.getUserCollections(userId));

    }



    @GetMapping("/page")

    public Result<Map<String, Object>> page(CollectQueryDTO dto, HttpServletRequest request) {

        Long userId = userIdResolver.resolve(request);

        if (userId == null) {

            return Result.fail("请先登录");

        }

        return Result.success(collectionService.listByPage(userId, dto));

    }



    @GetMapping("/stats")

    public Result<CollectStatsVO> stats(HttpServletRequest request) {

        Long userId = userIdResolver.resolve(request);

        if (userId == null) {

            return Result.fail("请先登录");

        }

        return Result.success(collectionService.getStats(userId));

    }



    @PostMapping

    public Result<Void> collect(

            @RequestBody Map<String, Object> params,

            HttpServletRequest request) {

        Long userId = userIdResolver.resolve(request);

        if (userId == null) {

            return Result.fail("请先登录");

        }

        Long resourceId = toLong(params.get("resourceId"));

        if (resourceId == null) {

            return Result.fail("resourceId 不能为空");

        }

        String resourceType = params.get("resourceType") != null

                ? String.valueOf(params.get("resourceType"))

                : ResourceTypeConstants.RESOURCE;

        collectionService.collectWithSnapshot(userId, resourceId, resourceType, params);

        return Result.success(null);

    }



    @DeleteMapping("/{resourceId}")

    public Result<Void> uncollect(

            @PathVariable Long resourceId,

            @RequestParam(required = false) String resourceType,

            HttpServletRequest request) {

        Long userId = userIdResolver.resolve(request);

        if (userId == null) {

            return Result.fail("请先登录");

        }

        collectionService.uncollect(

                userId,

                resourceId,

                resourceType != null ? resourceType : ResourceTypeConstants.RESOURCE);

        return Result.success(null);

    }



    @GetMapping("/check/{resourceId}")

    public Result<Boolean> checkCollected(

            @PathVariable Long resourceId,

            @RequestParam(required = false) String resourceType,

            HttpServletRequest request) {

        Long userId = userIdResolver.resolve(request);

        if (userId == null) {

            return Result.success(false);

        }

        return Result.success(collectionService.isCollected(

                userId,

                resourceId,

                resourceType != null ? resourceType : ResourceTypeConstants.RESOURCE));

    }



    private Long toLong(Object value) {

        if (value == null) {

            return null;

        }

        if (value instanceof Number n) {

            return n.longValue();

        }

        try {

            return Long.parseLong(String.valueOf(value));

        } catch (NumberFormatException e) {

            return null;

        }

    }

}

