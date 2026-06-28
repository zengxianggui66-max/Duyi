package com.k12.resource.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceMainUpsertServiceTest {

    @Test
    void mapCompatLegacyStatus_published() {
        ResourceMainUpsertService.StatusTriple triple = ResourceMainUpsertService.mapCompatLegacyStatus(1);
        assertEquals(1, triple.auditStatus());
        assertEquals(1, triple.publishStatus());
        assertEquals(1, triple.legacyStatus());
    }

    @Test
    void mapCompatLegacyStatus_draft() {
        ResourceMainUpsertService.StatusTriple triple = ResourceMainUpsertService.mapCompatLegacyStatus(0);
        assertEquals(0, triple.auditStatus());
        assertEquals(0, triple.publishStatus());
        assertEquals(0, triple.legacyStatus());
    }

    @Test
    void mapEduAuditStatus_rejected() {
        assertEquals(2, ResourceMainUpsertService.mapEduAuditStatus(2));
        assertEquals(1, ResourceMainUpsertService.mapEduAuditStatus(1));
        assertEquals(-1, ResourceMainUpsertService.mapEduAuditStatus(-1));
    }
}
