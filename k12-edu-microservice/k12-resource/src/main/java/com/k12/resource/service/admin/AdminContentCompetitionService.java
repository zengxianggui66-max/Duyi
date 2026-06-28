package com.k12.resource.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.PageResult;
import com.k12.common.dto.AdminContentPackageWriteDTO;
import com.k12.common.dto.AdminContentQueryDTO;
import com.k12.common.entity.CompetitionTrainingPackage;
import com.k12.resource.mapper.CompetitionTrainingPackageMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminContentCompetitionService {

    private final CompetitionTrainingPackageMapper packageMapper;

    public AdminContentCompetitionService(CompetitionTrainingPackageMapper packageMapper) {
        this.packageMapper = packageMapper;
    }

    public PageResult<CompetitionTrainingPackage> listPackages(AdminContentQueryDTO query) {
        Page<CompetitionTrainingPackage> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<CompetitionTrainingPackage> w = buildWrapper(query);
        w.orderByDesc(CompetitionTrainingPackage::getSort).orderByDesc(CompetitionTrainingPackage::getId);
        Page<CompetitionTrainingPackage> result = packageMapper.selectPage(page, w);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public CompetitionTrainingPackage getPackage(Long id) {
        CompetitionTrainingPackage pkg = packageMapper.selectById(id);
        if (pkg == null) {
            throw new BusinessException(404, "竞赛套卷不存在");
        }
        return pkg;
    }

    public CompetitionTrainingPackage createPackage(AdminContentPackageWriteDTO dto) {
        CompetitionTrainingPackage pkg = new CompetitionTrainingPackage();
        applyWrite(pkg, dto);
        if (pkg.getStatus() == null) {
            pkg.setStatus(1);
        }
        if (pkg.getResourceCount() == null) {
            pkg.setResourceCount(0);
        }
        if (pkg.getDownloadCount() == null) {
            pkg.setDownloadCount(0);
        }
        packageMapper.insert(pkg);
        return pkg;
    }

    public CompetitionTrainingPackage updatePackage(Long id, AdminContentPackageWriteDTO dto) {
        CompetitionTrainingPackage pkg = getPackage(id);
        applyWrite(pkg, dto);
        packageMapper.updateById(pkg);
        return pkg;
    }

    public void updatePackageStatus(Long id, int status) {
        CompetitionTrainingPackage pkg = getPackage(id);
        pkg.setStatus(status);
        packageMapper.updateById(pkg);
    }

    public void deletePackage(Long id) {
        getPackage(id);
        packageMapper.deleteById(id);
    }

    private LambdaQueryWrapper<CompetitionTrainingPackage> buildWrapper(AdminContentQueryDTO query) {
        LambdaQueryWrapper<CompetitionTrainingPackage> w = new LambdaQueryWrapper<>();
        if (!Boolean.TRUE.equals(query.getIncludeDisabled()) && query.getStatus() == null) {
            w.eq(CompetitionTrainingPackage::getStatus, 1);
        } else if (query.getStatus() != null) {
            w.eq(CompetitionTrainingPackage::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getCategory())) {
            w.eq(CompetitionTrainingPackage::getCategory, query.getCategory());
        }
        if (StringUtils.hasText(query.getGradeStage())) {
            w.eq(CompetitionTrainingPackage::getGradeStage, query.getGradeStage());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            w.and(q -> q.like(CompetitionTrainingPackage::getTitle, kw)
                    .or().like(CompetitionTrainingPackage::getSummary, kw)
                    .or().like(CompetitionTrainingPackage::getTags, kw));
        }
        return w;
    }

    private void applyWrite(CompetitionTrainingPackage pkg, AdminContentPackageWriteDTO dto) {
        pkg.setTitle(dto.getTitle().trim());
        pkg.setSummary(StringUtils.hasText(dto.getSummary()) ? dto.getSummary().trim() : "");
        if (StringUtils.hasText(dto.getCategory())) {
            pkg.setCategory(dto.getCategory());
        } else if (pkg.getCategory() == null) {
            pkg.setCategory("math");
        }
        pkg.setGradeStage(StringUtils.hasText(dto.getGradeStage()) ? dto.getGradeStage() : "all");
        if (dto.getCoverUrl() != null) {
            pkg.setCoverUrl(dto.getCoverUrl());
        }
        if (dto.getIcon() != null) {
            pkg.setIcon(dto.getIcon());
        }
        if (dto.getTags() != null) {
            pkg.setTags(dto.getTags());
        }
        if (dto.getIsFree() != null) {
            pkg.setIsFree(dto.getIsFree());
        }
        if (dto.getIsElite() != null) {
            pkg.setIsElite(dto.getIsElite());
        }
        if (dto.getSort() != null) {
            pkg.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            pkg.setStatus(dto.getStatus());
        }
    }
}
