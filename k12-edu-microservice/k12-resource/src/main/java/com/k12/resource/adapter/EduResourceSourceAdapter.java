package com.k12.resource.adapter;

import com.k12.resource.service.EduResourceService;
import org.springframework.stereotype.Component;

@Component
public class EduResourceSourceAdapter implements ResourceSourceAdapter {

    public static final String SOURCE_TYPE = "edu_resource";

    private final EduResourceService eduResourceService;

    public EduResourceSourceAdapter(EduResourceService eduResourceService) {
        this.eduResourceService = eduResourceService;
    }

    @Override
    public String sourceType() {
        return SOURCE_TYPE;
    }

    @Override
    public void incrementView(Long sourceId) {
        eduResourceService.incrementViewCount(sourceId);
    }

    @Override
    public void incrementDownload(Long sourceId) {
        eduResourceService.incrementDownloadCount(sourceId);
    }
}
