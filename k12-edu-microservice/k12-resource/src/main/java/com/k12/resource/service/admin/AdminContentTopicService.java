package com.k12.resource.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.PageResult;
import com.k12.common.dto.AdminContentPackageWriteDTO;
import com.k12.common.dto.AdminContentQueryDTO;
import com.k12.common.entity.TopicAlbum;
import com.k12.resource.mapper.TopicAlbumMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminContentTopicService {

    private final TopicAlbumMapper albumMapper;

    public AdminContentTopicService(TopicAlbumMapper albumMapper) {
        this.albumMapper = albumMapper;
    }

    public PageResult<TopicAlbum> listAlbums(AdminContentQueryDTO query) {
        Page<TopicAlbum> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<TopicAlbum> w = buildWrapper(query);
        w.orderByDesc(TopicAlbum::getSort).orderByDesc(TopicAlbum::getId);
        Page<TopicAlbum> result = albumMapper.selectPage(page, w);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public TopicAlbum getAlbum(Long id) {
        TopicAlbum album = albumMapper.selectById(id);
        if (album == null) {
            throw new BusinessException(404, "专辑不存在");
        }
        return album;
    }

    public TopicAlbum createAlbum(AdminContentPackageWriteDTO dto) {
        TopicAlbum album = new TopicAlbum();
        applyWrite(album, dto);
        if (album.getStatus() == null) {
            album.setStatus(1);
        }
        if (album.getResourceCount() == null) {
            album.setResourceCount(0);
        }
        if (album.getDownloadCount() == null) {
            album.setDownloadCount(0);
        }
        albumMapper.insert(album);
        return album;
    }

    public TopicAlbum updateAlbum(Long id, AdminContentPackageWriteDTO dto) {
        TopicAlbum album = getAlbum(id);
        applyWrite(album, dto);
        albumMapper.updateById(album);
        return album;
    }

    public void updateAlbumStatus(Long id, int status) {
        TopicAlbum album = getAlbum(id);
        album.setStatus(status);
        albumMapper.updateById(album);
    }

    public void deleteAlbum(Long id) {
        getAlbum(id);
        albumMapper.deleteById(id);
    }

    private LambdaQueryWrapper<TopicAlbum> buildWrapper(AdminContentQueryDTO query) {
        LambdaQueryWrapper<TopicAlbum> w = new LambdaQueryWrapper<>();
        if (!Boolean.TRUE.equals(query.getIncludeDisabled()) && query.getStatus() == null) {
            w.eq(TopicAlbum::getStatus, 1);
        } else if (query.getStatus() != null) {
            w.eq(TopicAlbum::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getCategory())) {
            w.eq(TopicAlbum::getCategory, query.getCategory());
        }
        if (StringUtils.hasText(query.getRegion())) {
            w.eq(TopicAlbum::getRegion, query.getRegion());
        }
        if (StringUtils.hasText(query.getGradeStage())) {
            w.eq(TopicAlbum::getGradeStage, query.getGradeStage());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            w.and(q -> q.like(TopicAlbum::getTitle, kw)
                    .or().like(TopicAlbum::getSummary, kw)
                    .or().like(TopicAlbum::getTags, kw));
        }
        return w;
    }

    private void applyWrite(TopicAlbum album, AdminContentPackageWriteDTO dto) {
        album.setTitle(dto.getTitle().trim());
        album.setSummary(StringUtils.hasText(dto.getSummary()) ? dto.getSummary().trim() : "");
        if (StringUtils.hasText(dto.getCategory())) {
            album.setCategory(dto.getCategory());
        } else if (album.getCategory() == null) {
            album.setCategory("elite");
        }
        album.setRegion(StringUtils.hasText(dto.getRegion()) ? dto.getRegion() : "all");
        album.setGradeStage(StringUtils.hasText(dto.getGradeStage()) ? dto.getGradeStage() : "all");
        if (dto.getCoverUrl() != null) {
            album.setCoverUrl(dto.getCoverUrl());
        }
        if (dto.getIcon() != null) {
            album.setIcon(dto.getIcon());
        }
        if (dto.getTags() != null) {
            album.setTags(dto.getTags());
        }
        if (dto.getIsFree() != null) {
            album.setIsFree(dto.getIsFree());
        }
        if (dto.getIsElite() != null) {
            album.setIsElite(dto.getIsElite());
        }
        if (dto.getSort() != null) {
            album.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            album.setStatus(dto.getStatus());
        }
    }
}
