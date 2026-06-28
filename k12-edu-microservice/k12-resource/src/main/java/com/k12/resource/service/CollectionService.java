package com.k12.resource.service;



import com.baomidou.mybatisplus.extension.service.IService;

import com.k12.common.dto.CollectQueryDTO;

import com.k12.common.dto.CollectStatsVO;

import com.k12.common.entity.Collection;



import java.util.List;

import java.util.Map;



/**

 * 收藏服务接口

 */

public interface CollectionService extends IService<Collection> {



    List<Collection> getUserCollections(Long userId);



    void collect(Long userId, Long resourceId, String resourceType);



    void collectWithSnapshot(Long userId, Long resourceId, String resourceType, Map<String, Object> snapshot);



    void uncollect(Long userId, Long resourceId, String resourceType);



    boolean isCollected(Long userId, Long resourceId, String resourceType);



    Map<String, Object> listByPage(Long userId, CollectQueryDTO dto);



    CollectStatsVO getStats(Long userId);



    default void collect(Long userId, Long resourceId) {

        collect(userId, resourceId, "resource");

    }



    default void uncollect(Long userId, Long resourceId) {

        uncollect(userId, resourceId, "resource");

    }



    default boolean isCollected(Long userId, Long resourceId) {

        return isCollected(userId, resourceId, "resource");

    }

}

