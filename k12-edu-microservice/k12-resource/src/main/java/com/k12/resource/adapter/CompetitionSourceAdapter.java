package com.k12.resource.adapter;

import com.k12.common.BusinessException;
import com.k12.common.dto.AdminResourceUpdateDTO;
import com.k12.common.entity.CompetitionResource;
import com.k12.resource.mapper.CompetitionResourceMapper;
import com.k12.resource.service.CompetitionZoneService;
import org.springframework.stereotype.Component;

@Component
public class CompetitionSourceAdapter extends AbstractDedicatedZoneSourceAdapter {

    public static final String SOURCE_TYPE = "competition_resource";

    private final CompetitionZoneService competitionZoneService;
    private final CompetitionResourceMapper competitionResourceMapper;

    public CompetitionSourceAdapter(CompetitionZoneService competitionZoneService,
                                    CompetitionResourceMapper competitionResourceMapper) {
        this.competitionZoneService = competitionZoneService;
        this.competitionResourceMapper = competitionResourceMapper;
    }

    @Override
    public String sourceType() {
        return SOURCE_TYPE;
    }

    @Override
    protected void doIncrementView(Long sourceId) {
        competitionZoneService.incrementResourceView(sourceId);
    }

    @Override
    protected void doIncrementDownload(Long sourceId) {
        competitionZoneService.incrementResourceDownload(sourceId);
    }

    @Override
    protected void doApplyGenericUpdate(Long sourceId, AdminResourceUpdateDTO dto) {
        CompetitionResource entity = requireEntity(sourceId);
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getSort() != null) {
            entity.setSort(dto.getSort());
        }
        if (dto.getIsFree() != null) {
            entity.setIsFree(dto.getIsFree());
        }
        competitionResourceMapper.updateById(entity);
    }

    @Override
    protected void doUpdateStatus(Long sourceId, int status) {
        CompetitionResource entity = requireEntity(sourceId);
        entity.setStatus(status);
        competitionResourceMapper.updateById(entity);
    }

    @Override
    protected void doUpdateElite(Long sourceId, int isElite) {
        CompetitionResource entity = requireEntity(sourceId);
        entity.setIsElite(isElite);
        competitionResourceMapper.updateById(entity);
    }

    private CompetitionResource requireEntity(Long sourceId) {
        CompetitionResource entity = competitionResourceMapper.selectById(sourceId);
        if (entity == null) {
            throw new BusinessException(404, "竞赛资源不存在");
        }
        return entity;
    }
}
