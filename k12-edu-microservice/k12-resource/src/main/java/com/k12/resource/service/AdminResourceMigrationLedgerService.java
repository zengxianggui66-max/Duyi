package com.k12.resource.service;

import com.k12.common.dto.ResourceMigrationLedgerUpdateDTO;
import com.k12.common.entity.ResourceMigrationLedger;

import java.util.List;

public interface AdminResourceMigrationLedgerService {

    List<ResourceMigrationLedger> list(Long adminUserId);

    ResourceMigrationLedger upsert(String sourceType, ResourceMigrationLedgerUpdateDTO dto, Long adminUserId);
}
