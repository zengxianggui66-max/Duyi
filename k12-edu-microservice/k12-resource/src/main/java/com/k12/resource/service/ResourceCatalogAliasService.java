package com.k12.resource.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.ResourceCatalogAliasQueryDTO;
import com.k12.common.dto.ResourceCatalogAliasUpsertDTO;
import com.k12.common.entity.ResourceCatalogAlias;

public interface ResourceCatalogAliasService {

    Page<ResourceCatalogAlias> list(ResourceCatalogAliasQueryDTO query, Long adminUserId);

    ResourceCatalogAlias upsert(ResourceCatalogAliasUpsertDTO dto, Long adminUserId);
}
