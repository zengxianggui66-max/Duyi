package com.k12.resource.legacy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.k12.common.entity.SysConfig;
import com.k12.common.mapper.SysConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyReadApi410ServiceTest {

    @Mock
    private SysConfigMapper sysConfigMapper;

    private LegacyReadApi410Service service;

    @BeforeEach
    void setUp() {
        service = new LegacyReadApi410Service(sysConfigMapper);
    }

    @Test
    void flagOffByDefaultWhenMissingConfig() {
        when(sysConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        assertFalse(service.is410Enabled());
    }

    @Test
    void exactLegacyListPathMapsToUnifiedPage() {
        Optional<String> location = service.resolveReplacementLocation("/api/topic/resources/page");
        assertTrue(location.isPresent());
        assertEquals("/api/resources/page?sourceType=topic_resource", location.get());
    }

    @Test
    void browseSuitesMapsToUnifiedSuites() {
        Optional<String> location = service.resolveReplacementLocation("/api/resources/browse/suites");
        assertEquals("/api/resources/suites?sourceType=primary_chinese", location.get());
    }

    @Test
    void detailPathMapsToResolveGlobalId() {
        Optional<String> location = service.resolveReplacementLocation("/api/competition/resources/42");
        assertEquals("/api/resources/resolve-global-id?sourceType=competition_resource&sourceId=42", location.get());
    }

    @Test
    void minePathIsNotDeprecatedRead() {
        assertFalse(service.isDeprecatedLegacyRead("GET", "/api/primary-chinese/mine/stats"));
    }

    @Test
    void filterOptionsAreExcluded() {
        assertFalse(service.isDeprecatedLegacyRead("GET", "/api/topic/filter-options"));
    }

    @Test
    void postRequestsAreNotDeprecated() {
        assertFalse(service.isDeprecatedLegacyRead("POST", "/api/primary-chinese/page"));
    }

    @Test
    void primaryChinesePageIsDeprecatedWhenGet() {
        assertTrue(service.isDeprecatedLegacyRead("GET", "/api/primary-chinese/page"));
    }
}
