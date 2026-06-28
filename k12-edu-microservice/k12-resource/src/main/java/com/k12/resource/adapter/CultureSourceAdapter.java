package com.k12.resource.adapter;

import com.k12.common.BusinessException;
import com.k12.common.dto.AdminResourceUpdateDTO;
import com.k12.common.entity.CultureResource;
import com.k12.resource.mapper.CultureResourceMapper;
import com.k12.resource.service.CultureStudyService;
import org.springframework.stereotype.Component;

@Component
public class CultureSourceAdapter extends AbstractDedicatedZoneSourceAdapter {

    public static final String SOURCE_TYPE = "culture_resource";

    private final CultureStudyService cultureStudyService;
    private final CultureResourceMapper cultureResourceMapper;

    public CultureSourceAdapter(CultureStudyService cultureStudyService,
                                CultureResourceMapper cultureResourceMapper) {
        this.cultureStudyService = cultureStudyService;
        this.cultureResourceMapper = cultureResourceMapper;
    }

    @Override
    public String sourceType() {
        return SOURCE_TYPE;
    }

    @Override
    public boolean isExternalPreview(Integer allowPreview, String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return Integer.valueOf(0).equals(allowPreview)
                && (url.startsWith("http://") || url.startsWith("https://"));
    }

    @Override
    protected void doIncrementView(Long sourceId) {
        cultureStudyService.incrementResourceView(sourceId);
    }

    @Override
    protected void doIncrementDownload(Long sourceId) {
        cultureStudyService.incrementResourceDownload(sourceId);
    }

    @Override
    protected void doApplyGenericUpdate(Long sourceId, AdminResourceUpdateDTO dto) {
        CultureResource entity = requireEntity(sourceId);
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getSort() != null) {
            entity.setSort(dto.getSort());
        }
        if (dto.getIsFree() != null) {
            entity.setIsFree(dto.getIsFree());
        }
        cultureResourceMapper.updateById(entity);
    }

    @Override
    protected void doUpdateStatus(Long sourceId, int status) {
        CultureResource entity = requireEntity(sourceId);
        entity.setStatus(status);
        cultureResourceMapper.updateById(entity);
    }

    @Override
    protected void doUpdateElite(Long sourceId, int isElite) {
        CultureResource entity = requireEntity(sourceId);
        entity.setIsElite(isElite);
        cultureResourceMapper.updateById(entity);
    }

    private CultureResource requireEntity(Long sourceId) {
        CultureResource entity = cultureResourceMapper.selectById(sourceId);
        if (entity == null) {
            throw new BusinessException(404, "传统文化资源不存在");
        }
        return entity;
    }
}
