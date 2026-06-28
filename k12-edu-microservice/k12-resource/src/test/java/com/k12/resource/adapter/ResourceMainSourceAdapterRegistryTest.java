package com.k12.resource.adapter;

import com.k12.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceMainSourceAdapterRegistryTest {

    private ResourceSourceAdapterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ResourceSourceAdapterRegistry(List.of(
                stub(PrimaryChineseSourceAdapter.SOURCE_TYPE, true, true, true),
                stub(TopicSourceAdapter.SOURCE_TYPE, true, false, false),
                stub(CultureSourceAdapter.SOURCE_TYPE, true, false, false),
                stub(CompetitionSourceAdapter.SOURCE_TYPE, true, false, false),
                stub(EduResourceSourceAdapter.SOURCE_TYPE, false, false, false)
        ));
    }

    @Test
    void requireReturnsRegisteredAdapter() {
        assertEquals(PrimaryChineseSourceAdapter.SOURCE_TYPE,
                registry.require(PrimaryChineseSourceAdapter.SOURCE_TYPE).sourceType());
        assertEquals(EduResourceSourceAdapter.SOURCE_TYPE,
                registry.get(EduResourceSourceAdapter.SOURCE_TYPE).sourceType());
    }

    @Test
    void getReturnsNullForUnknownSourceType() {
        assertNull(registry.get("unknown_source"));
    }

    @Test
    void requireThrowsForUnknownSourceType() {
        BusinessException ex = assertThrows(BusinessException.class, () -> registry.require("unknown_source"));
        assertTrue(ex.getMessage().contains("不支持的资源来源"));
    }

    @Test
    void primaryChineseAdapterSupportsPlacementAndTop() {
        ResourceSourceAdapter adapter = registry.require(PrimaryChineseSourceAdapter.SOURCE_TYPE);
        assertTrue(adapter.supportsUpdate());
        assertTrue(adapter.supportsPlacement());
        assertTrue(adapter.supportsTop());
        assertTrue(adapter.supportsFullAudit());
    }

    @Test
    void eduResourceAdapterIsReadCounterOnly() {
        ResourceSourceAdapter adapter = registry.require(EduResourceSourceAdapter.SOURCE_TYPE);
        assertThrows(BusinessException.class, () -> adapter.update(1L, null, 1L));
    }

    private static ResourceSourceAdapter stub(String sourceType,
                                              boolean supportsUpdate,
                                              boolean supportsPlacement,
                                              boolean supportsTop) {
        return new ResourceSourceAdapter() {
            @Override
            public String sourceType() {
                return sourceType;
            }

            @Override
            public void incrementView(Long sourceId) {
            }

            @Override
            public void incrementDownload(Long sourceId) {
            }

            @Override
            public boolean supportsUpdate() {
                return supportsUpdate;
            }

            @Override
            public boolean supportsPlacement() {
                return supportsPlacement;
            }

            @Override
            public boolean supportsTop() {
                return supportsTop;
            }

            @Override
            public boolean supportsFullAudit() {
                return PrimaryChineseSourceAdapter.SOURCE_TYPE.equals(sourceType);
            }
        };
    }
}
