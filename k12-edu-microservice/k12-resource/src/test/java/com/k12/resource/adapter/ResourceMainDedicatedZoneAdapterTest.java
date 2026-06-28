package com.k12.resource.adapter;

import com.k12.common.BusinessException;
import com.k12.common.dto.AdminResourceUpdateDTO;
import com.k12.common.entity.CompetitionResource;
import com.k12.common.entity.CultureResource;
import com.k12.common.entity.TopicResource;
import com.k12.resource.mapper.CompetitionResourceMapper;
import com.k12.resource.mapper.CultureResourceMapper;
import com.k12.resource.mapper.TopicResourceMapper;
import com.k12.resource.service.CompetitionZoneService;
import com.k12.resource.service.CultureStudyService;
import com.k12.resource.service.TopicZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceMainDedicatedZoneAdapterTest {

    @Mock
    private TopicZoneService topicZoneService;
    @Mock
    private TopicResourceMapper topicResourceMapper;
    @Mock
    private CultureStudyService cultureStudyService;
    @Mock
    private CultureResourceMapper cultureResourceMapper;
    @Mock
    private CompetitionZoneService competitionZoneService;
    @Mock
    private CompetitionResourceMapper competitionResourceMapper;

    private TopicSourceAdapter topicAdapter;
    private CultureSourceAdapter cultureAdapter;
    private CompetitionSourceAdapter competitionAdapter;

    @BeforeEach
    void setUp() {
        topicAdapter = new TopicSourceAdapter(topicZoneService, topicResourceMapper);
        cultureAdapter = new CultureSourceAdapter(cultureStudyService, cultureResourceMapper);
        competitionAdapter = new CompetitionSourceAdapter(competitionZoneService, competitionResourceMapper);
    }

    static Stream<Arguments> dedicatedZoneAdapters() {
        return Stream.of(
                Arguments.of("topic_resource"),
                Arguments.of("culture_resource"),
                Arguments.of("competition_resource")
        );
    }

    @ParameterizedTest
    @MethodSource("dedicatedZoneAdapters")
    void auditApproveMapsToPublishedStatus(String sourceType) {
        seedEntity(sourceType, 11L);
        adapterOf(sourceType).audit(11L, 1, "ok", 1L, "admin");
        assertStatus(sourceType, 1);
    }

    @ParameterizedTest
    @MethodSource("dedicatedZoneAdapters")
    void auditRejectMapsToRejectedStatus(String sourceType) {
        seedEntity(sourceType, 12L);
        adapterOf(sourceType).audit(12L, 0, "bad", 1L, "admin");
        assertStatus(sourceType, 2);
    }

    @Test
    void topicOfflineRecycleRestoreStatusMapping() {
        when(topicResourceMapper.selectById(13L)).thenAnswer(invocation -> {
            TopicResource entity = new TopicResource();
            entity.setId(13L);
            return entity;
        });
        when(topicResourceMapper.updateById(any(TopicResource.class))).thenReturn(1);

        topicAdapter.offline(13L, 1L);
        topicAdapter.recycle(13L, 1L);
        topicAdapter.restore(13L, 1L);

        ArgumentCaptor<TopicResource> captor = ArgumentCaptor.forClass(TopicResource.class);
        verify(topicResourceMapper, times(3)).updateById(captor.capture());
        assertEquals(3, captor.getAllValues().get(0).getStatus());
        assertEquals(4, captor.getAllValues().get(1).getStatus());
        assertEquals(1, captor.getAllValues().get(2).getStatus());
    }

    @Test
    void cultureRecommendMapsToEliteFlag() {
        seedEntity(CultureSourceAdapter.SOURCE_TYPE, 14L);
        cultureAdapter.setRecommend(14L, true, 1L);

        ArgumentCaptor<CultureResource> captor = ArgumentCaptor.forClass(CultureResource.class);
        verify(cultureResourceMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getIsElite());
    }

    @Test
    void topicUpdateAppliesEditableFields() {
        TopicResource entity = new TopicResource();
        entity.setId(21L);
        when(topicResourceMapper.selectById(21L)).thenReturn(entity);

        AdminResourceUpdateDTO dto = new AdminResourceUpdateDTO();
        dto.setTitle("新标题");
        dto.setSort(9);
        dto.setIsFree(0);
        topicAdapter.update(21L, dto, 1L);

        ArgumentCaptor<TopicResource> captor = ArgumentCaptor.forClass(TopicResource.class);
        verify(topicResourceMapper).updateById(captor.capture());
        assertEquals("新标题", captor.getValue().getTitle());
        assertEquals(9, captor.getValue().getSort());
        assertEquals(0, captor.getValue().getIsFree());
    }

    @Test
    void cultureExternalPreviewOnlyWhenAllowPreviewDisabled() {
        assertEquals(true, cultureAdapter.isExternalPreview(0, "https://example.com/a.pdf"));
        assertEquals(false, cultureAdapter.isExternalPreview(1, "https://example.com/a.pdf"));
        assertEquals(false, cultureAdapter.isExternalPreview(0, "/local/file.pdf"));
    }

    @Test
    void missingEntityThrowsNotFound() {
        when(topicResourceMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> topicAdapter.publish(99L, 1L));
    }

    private AbstractDedicatedZoneSourceAdapter adapterOf(String sourceType) {
        return switch (sourceType) {
            case TopicSourceAdapter.SOURCE_TYPE -> topicAdapter;
            case CultureSourceAdapter.SOURCE_TYPE -> cultureAdapter;
            case CompetitionSourceAdapter.SOURCE_TYPE -> competitionAdapter;
            default -> throw new IllegalArgumentException(sourceType);
        };
    }

    private void seedEntity(String sourceType, Long id) {
        switch (sourceType) {
            case TopicSourceAdapter.SOURCE_TYPE -> {
                TopicResource entity = new TopicResource();
                entity.setId(id);
                when(topicResourceMapper.selectById(id)).thenReturn(entity);
                when(topicResourceMapper.updateById(any(TopicResource.class))).thenReturn(1);
            }
            case CultureSourceAdapter.SOURCE_TYPE -> {
                CultureResource entity = new CultureResource();
                entity.setId(id);
                when(cultureResourceMapper.selectById(id)).thenReturn(entity);
                when(cultureResourceMapper.updateById(any(CultureResource.class))).thenReturn(1);
            }
            case CompetitionSourceAdapter.SOURCE_TYPE -> {
                CompetitionResource entity = new CompetitionResource();
                entity.setId(id);
                when(competitionResourceMapper.selectById(id)).thenReturn(entity);
                when(competitionResourceMapper.updateById(any(CompetitionResource.class))).thenReturn(1);
            }
            default -> throw new IllegalArgumentException(sourceType);
        }
    }

    private void assertStatus(String sourceType, int expectedStatus) {
        switch (sourceType) {
            case TopicSourceAdapter.SOURCE_TYPE -> {
                ArgumentCaptor<TopicResource> captor = ArgumentCaptor.forClass(TopicResource.class);
                verify(topicResourceMapper).updateById(captor.capture());
                assertEquals(expectedStatus, captor.getValue().getStatus());
            }
            case CultureSourceAdapter.SOURCE_TYPE -> {
                ArgumentCaptor<CultureResource> captor = ArgumentCaptor.forClass(CultureResource.class);
                verify(cultureResourceMapper).updateById(captor.capture());
                assertEquals(expectedStatus, captor.getValue().getStatus());
            }
            case CompetitionSourceAdapter.SOURCE_TYPE -> {
                ArgumentCaptor<CompetitionResource> captor = ArgumentCaptor.forClass(CompetitionResource.class);
                verify(competitionResourceMapper).updateById(captor.capture());
                assertEquals(expectedStatus, captor.getValue().getStatus());
            }
            default -> throw new IllegalArgumentException(sourceType);
        }
    }
}
