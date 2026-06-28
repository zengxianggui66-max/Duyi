package com.k12.resource.service;

import com.k12.common.dto.AdminCatalogNodeAdminVO;
import com.k12.common.dto.AdminCatalogNodeWriteDTO;
import com.k12.common.dto.CatalogNodeVO;
import com.k12.common.dto.CatalogSchemeVO;
import com.k12.common.entity.EduCatalogNode;

import java.util.List;

public interface AdminCatalogService {

    List<CatalogSchemeVO> listSchemes();

    List<CatalogNodeVO> getAdminTree(
            Integer schemeId,
            String schemeCode,
            String volumeKey,
            String gradeName,
            String subject,
            boolean includeDisabled);

    List<AdminCatalogNodeAdminVO> listNodesFlat(
            Integer schemeId,
            Long parentId,
            String volumeKey,
            boolean includeDisabled);

    EduCatalogNode createNode(AdminCatalogNodeWriteDTO dto, Long adminUserId);

    EduCatalogNode updateNode(Long id, AdminCatalogNodeWriteDTO dto, Long adminUserId);

    void setNodeStatus(Long id, Integer status, Long adminUserId);

    void deleteNode(Long id, Long adminUserId);

    /** 从 classpath unit-catalog.json 导入指定册别到 textbook_unit 方案 */
    int importVolumeFromStaticJson(String volumeKey, Long adminUserId);
}
