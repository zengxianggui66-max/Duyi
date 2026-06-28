package com.k12.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.AdminUserActionVO;
import com.k12.common.dto.AdminUserLoginLogVO;
import com.k12.common.dto.AdminUserStatsVO;
import com.k12.common.dto.AdminUserUploadVO;
import com.k12.common.dto.CollectItemVO;

public interface AdminUserBehaviorService {

    AdminUserStatsVO getStats(Long userId);

    Page<AdminUserUploadVO> listUploads(Long userId, int current, int size);

    Page<CollectItemVO> listCollections(Long userId, int current, int size);

    Page<AdminUserActionVO> listActions(Long userId, String actionType, int current, int size);

    Page<AdminUserLoginLogVO> listLoginLogs(Long userId, int current, int size);
}
