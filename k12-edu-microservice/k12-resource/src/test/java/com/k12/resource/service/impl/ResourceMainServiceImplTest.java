package com.k12.resource.service.impl;

import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.entity.VAdminResourceMain;
import com.k12.resource.adapter.EduResourceSourceAdapter;
import com.k12.resource.adapter.ResourceSourceAdapter;
import com.k12.resource.adapter.ResourceSourceAdapterRegistry;
import com.k12.resource.mapper.EduResourceFileMapper;
import com.k12.resource.mapper.PreviewFailQueueMapper;
import com.k12.resource.mapper.ResourceMainMapper;
import com.k12.resource.mapper.VAdminResourceMainMapper;
import com.k12.resource.service.CollectionService;
import com.k12.resource.service.DocumentPreviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceMainServiceImplTest {

    @Mock
    private VAdminResourceMainMapper viewMapper;
    @Mock
    private ResourceMainMapper resourceMainMapper;
    @Mock
    private EduResourceFileMapper eduResourceFileMapper;
    @Mock
    private DocumentPreviewService documentPreviewService;
    @Mock
    private PreviewFailQueueMapper previewFailQueueMapper;
    @Mock
    private CollectionService collectionService;

    private ResourceSourceAdapter eduAdapter;
    private ResourceSourceAdapterRegistry adapterRegistry;
    private ResourceMainServiceImpl service;

    @BeforeEach
    void setUp() {
        eduAdapter = mock(ResourceSourceAdapter.class);
        when(eduAdapter.sourceType()).thenReturn(EduResourceSourceAdapter.SOURCE_TYPE);
        adapterRegistry = new ResourceSourceAdapterRegistry(List.of(eduAdapter));
        service = new ResourceMainServiceImpl(
                viewMapper,
                resourceMainMapper,
                eduResourceFileMapper,
                documentPreviewService,
                previewFailQueueMapper,
                adapterRegistry,
                collectionService);
    }

    @Test
    void viewDelegatesToSourceAdapter() {
        VAdminResourceMain row = publicRow(EduResourceSourceAdapter.SOURCE_TYPE, 100L);
        when(viewMapper.findByGlobalId(1L)).thenReturn(row);

        service.view(1L, 9L);

        verify(eduAdapter).incrementView(100L);
    }

    @Test
    void downloadDelegatesToSourceAdapterForPlatformResource() {
        VAdminResourceMain row = publicRow(EduResourceSourceAdapter.SOURCE_TYPE, 101L);
        row.setOssUrl("https://cdn.example.com/a.pdf");
        when(viewMapper.findByGlobalId(2L)).thenReturn(row);
        when(eduResourceFileMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        service.download(2L, 9L);

        verify(eduAdapter).incrementDownload(101L);
    }

    @Test
    void collectUsesAdapterCollectResourceType() {
        when(eduAdapter.collectResourceType()).thenReturn(ResourceTypeConstants.RESOURCE);
        VAdminResourceMain row = publicRow(EduResourceSourceAdapter.SOURCE_TYPE, 102L);
        when(viewMapper.findByGlobalId(3L)).thenReturn(row);

        service.collect(3L, 9L);

        verify(collectionService).collectWithSnapshot(
                eq(9L),
                eq(102L),
                eq(ResourceTypeConstants.RESOURCE),
                org.mockito.ArgumentMatchers.anyMap());
    }

    private static VAdminResourceMain publicRow(String sourceType, Long sourceId) {
        VAdminResourceMain row = new VAdminResourceMain();
        row.setGlobalId(1L);
        row.setSourceType(sourceType);
        row.setSourceId(sourceId);
        row.setTitle("demo");
        row.setAuditStatus(ResourceStatusConstants.AUDIT_APPROVED);
        row.setPublishStatus(ResourceStatusConstants.PUBLISH_PUBLISHED);
        return row;
    }
}
