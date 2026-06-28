package com.k12.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.AdminPrimaryResourceQueryDTO;
import com.k12.common.dto.AdminResourceAuditInsightsVO;
import com.k12.common.dto.AdminResourceBatchResultVO;
import com.k12.common.dto.AdminResourceDetailVO;
import com.k12.common.dto.AdminResourceListVO;
import com.k12.common.dto.AdminResourceUpdateDTO;

import java.util.List;
import java.util.Map;

public interface AdminPrimaryResourceService {

    Page<AdminResourceListVO> listPage(AdminPrimaryResourceQueryDTO query, Long adminUserId);

    Page<AdminResourceListVO> listPending(AdminPrimaryResourceQueryDTO query, Long adminUserId);

    Map<String, Object> getResourceStats(Long adminUserId);

    AdminResourceDetailVO getDetail(Long id, Long adminUserId);

    void update(Long id, AdminResourceUpdateDTO dto, Long adminUserId);

    void publish(Long id, Long adminUserId);

    void offline(Long id, Long adminUserId);

    void setRecommend(Long id, boolean recommend, Long adminUserId);

    void setTop(Long id, boolean top, Integer topSort, Long adminUserId);

    void moveToRecycle(Long id, Long adminUserId);

    void auditResource(Long id, Integer status, String reason, Long auditorId, String auditorName);

    AdminResourceBatchResultVO batchAudit(List<Long> ids, String action, String reason,
                                        Long auditorId, String auditorName);

    AdminResourceAuditInsightsVO getAuditInsights(Long id, Long adminUserId);

    void restoreFromRecycle(Long id, Long adminUserId);

    AdminResourceBatchResultVO batchAction(List<Long> ids, String action, Long adminUserId);
}
