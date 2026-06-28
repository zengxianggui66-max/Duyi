package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.dto.BehaviorQueryDTO;
import com.k12.common.dto.BehaviorStatsVO;
import com.k12.common.entity.DownloadRecord;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.common.entity.EduResource;
import com.k12.resource.mapper.DownloadRecordMapper;
import com.k12.resource.mapper.EduResourceMapper;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import com.k12.resource.service.AdminResourceService;
import com.k12.resource.service.DownloadRecordService;
import com.k12.resource.service.UserResourceActionService;
import com.k12.resource.util.StageKeyHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class DownloadRecordServiceImpl extends ServiceImpl<DownloadRecordMapper, DownloadRecord>
        implements DownloadRecordService {

    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final EduResourceMapper eduResourceMapper;
    private final AdminResourceService adminResourceService;
    private final UserResourceActionService userResourceActionService;
    public DownloadRecordServiceImpl(PrimaryChineseResourceMapper primaryChineseResourceMapper, EduResourceMapper eduResourceMapper, AdminResourceService adminResourceService, UserResourceActionService userResourceActionService) {
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.eduResourceMapper = eduResourceMapper;
        this.adminResourceService = adminResourceService;
        this.userResourceActionService = userResourceActionService;
    }


    @Override
    public Map<String, Object> listByPage(Long userId, BehaviorQueryDTO dto) {
        int current = dto.getCurrent() != null && dto.getCurrent() > 0 ? dto.getCurrent() : 1;
        int size = dto.getSize() != null && dto.getSize() > 0 ? dto.getSize() : 20;
        QueryWrapper<DownloadRecord> wrapper = baseQuery(userId, dto);
        wrapper.orderByDesc("create_time");
        Page<DownloadRecord> page = page(new Page<>(current, size), wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("records", page.getRecords());
        map.put("total", page.getTotal());
        map.put("current", page.getCurrent());
        map.put("size", page.getSize());
        map.put("pages", page.getPages());
        return map;
    }

    @Override
    public BehaviorStatsVO getStats(Long userId) {
        BehaviorStatsVO vo = new BehaviorStatsVO();
        vo.setTotal(count(baseQuery(userId, new BehaviorQueryDTO())));
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = todayStart.minusDays(6);
        QueryWrapper<DownloadRecord> todayQ = baseQuery(userId, new BehaviorQueryDTO());
        todayQ.ge("create_time", todayStart);
        QueryWrapper<DownloadRecord> weekQ = baseQuery(userId, new BehaviorQueryDTO());
        weekQ.ge("create_time", weekStart);
        vo.setTodayCount(count(todayQ));
        vo.setWeekCount(count(weekQ));
        return vo;
    }

    @Override
    public void addRecord(Long userId, Long resourceId, String resourceTitle, String resourceType) {
        if (userId == null || resourceId == null) {
            return;
        }
        String type = ResourceTypeConstants.normalize(resourceType);
        DownloadRecord record = new DownloadRecord();
        record.setUserId(userId);
        record.setResourceId(resourceId);
        record.setResourceType(type);
        if (StringUtils.hasText(resourceTitle)) {
            record.setResourceTitle(resourceTitle.trim());
        }
        fillSnapshot(record, resourceId, type);
        save(record);
        adminResourceService.recordDownload(resourceId);
        userResourceActionService.recordDownload(
                userId, resourceId, type, record.getResourceTitle(), "/api/resource/download/record");
    }

    @Override
    public void remove(Long userId, Long id) {
        QueryWrapper<DownloadRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("id", id);
        remove(wrapper);
    }

    @Override
    public void batchRemove(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        QueryWrapper<DownloadRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).in("id", ids);
        remove(wrapper);
    }

    private QueryWrapper<DownloadRecord> baseQuery(Long userId, BehaviorQueryDTO dto) {
        QueryWrapper<DownloadRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (dto != null) {
            if (StringUtils.hasText(dto.getKeyword())) {
                wrapper.like("resource_title", dto.getKeyword().trim());
            }
            if (StringUtils.hasText(dto.getStageKey())) {
                wrapper.eq("stage_key", dto.getStageKey());
            }
            if (StringUtils.hasText(dto.getSubject())) {
                wrapper.eq("subject", dto.getSubject());
            }
            if (StringUtils.hasText(dto.getTeachingType())) {
                wrapper.eq("teaching_type", dto.getTeachingType());
            }
        }
        return wrapper;
    }

    private void fillSnapshot(DownloadRecord record, Long resourceId, String resourceType) {
        if (ResourceTypeConstants.PRIMARY_CHINESE.equals(resourceType)) {
            PrimaryChineseResource res = primaryChineseResourceMapper.selectById(resourceId);
            if (res == null) {
                return;
            }
            if (!StringUtils.hasText(record.getResourceTitle())) {
                record.setResourceTitle(res.getTitle());
            }
            record.setFileExt(res.getFileExt());
            if (res.getFileSizeKb() != null) {
                record.setFileSize(res.getFileSizeKb().longValue() * 1024);
            }
            record.setSubject(res.getSubject());
            record.setGradeName(res.getGradeName());
            record.setTeachingType(res.getType());
            record.setStageKey(StageKeyHelper.toStageKey(res.getStage()));
            return;
        }
        EduResource res = eduResourceMapper.selectById(resourceId);
        if (res == null) {
            return;
        }
        if (!StringUtils.hasText(record.getResourceTitle())) {
            record.setResourceTitle(res.getTitle());
        }
        record.setFileExt(res.getFileExt());
        if (res.getFileSizeKb() != null) {
            record.setFileSize(res.getFileSizeKb().longValue() * 1024);
        }
        record.setSubject(res.getSubjectName());
        record.setGradeName(res.getGradeName());
        record.setTeachingType(res.getResourceTypeName());
        record.setStageKey(StageKeyHelper.toStageKey(res.getStageName()));
    }
}
