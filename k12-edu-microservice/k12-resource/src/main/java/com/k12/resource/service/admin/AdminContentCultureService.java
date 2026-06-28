package com.k12.resource.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.PageResult;
import com.k12.common.dto.AdminContentPackageWriteDTO;
import com.k12.common.dto.AdminContentQueryDTO;
import com.k12.common.entity.CultureStudyPackage;
import com.k12.resource.mapper.CultureStudyPackageMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminContentCultureService {

    private final CultureStudyPackageMapper packageMapper;

    public AdminContentCultureService(CultureStudyPackageMapper packageMapper) {
        this.packageMapper = packageMapper;
    }

    public PageResult<CultureStudyPackage> listPackages(AdminContentQueryDTO query) {
        Page<CultureStudyPackage> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<CultureStudyPackage> w = buildWrapper(query);
        w.orderByDesc(CultureStudyPackage::getSort).orderByDesc(CultureStudyPackage::getId);
        Page<CultureStudyPackage> result = packageMapper.selectPage(page, w);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public CultureStudyPackage getPackage(Long id) {
        CultureStudyPackage pkg = packageMapper.selectById(id);
        if (pkg == null) {
            throw new BusinessException(404, "研学包不存在");
        }
        return pkg;
    }

    public CultureStudyPackage createPackage(AdminContentPackageWriteDTO dto) {
        CultureStudyPackage pkg = new CultureStudyPackage();
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

    public CultureStudyPackage updatePackage(Long id, AdminContentPackageWriteDTO dto) {
        CultureStudyPackage pkg = getPackage(id);
        applyWrite(pkg, dto);
        packageMapper.updateById(pkg);
        return pkg;
    }

    public void updatePackageStatus(Long id, int status) {
        CultureStudyPackage pkg = getPackage(id);
        pkg.setStatus(status);
        packageMapper.updateById(pkg);
    }

    public void deletePackage(Long id) {
        getPackage(id);
        packageMapper.deleteById(id);
    }

    private LambdaQueryWrapper<CultureStudyPackage> buildWrapper(AdminContentQueryDTO query) {
        LambdaQueryWrapper<CultureStudyPackage> w = new LambdaQueryWrapper<>();
        if (!Boolean.TRUE.equals(query.getIncludeDisabled()) && query.getStatus() == null) {
            w.eq(CultureStudyPackage::getStatus, 1);
        } else if (query.getStatus() != null) {
            w.eq(CultureStudyPackage::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getRegion())) {
            w.eq(CultureStudyPackage::getRegion, query.getRegion());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            w.and(q -> q.like(CultureStudyPackage::getTitle, kw)
                    .or().like(CultureStudyPackage::getSummary, kw)
                    .or().like(CultureStudyPackage::getLocation, kw));
        }
        return w;
    }

    private void applyWrite(CultureStudyPackage pkg, AdminContentPackageWriteDTO dto) {
        pkg.setTitle(dto.getTitle().trim());
        pkg.setSummary(StringUtils.hasText(dto.getSummary()) ? dto.getSummary().trim() : "");
        pkg.setRegion(StringUtils.hasText(dto.getRegion()) ? dto.getRegion() : "sichuan");
        if (StringUtils.hasText(dto.getDurationType())) {
            pkg.setDurationType(dto.getDurationType());
        }
        if (dto.getDurationLabel() != null) {
            pkg.setDurationLabel(dto.getDurationLabel());
        }
        if (dto.getSuitableAudience() != null) {
            pkg.setSuitableAudience(dto.getSuitableAudience());
        }
        if (dto.getLocation() != null) {
            pkg.setLocation(dto.getLocation());
        }
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
