package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.dto.BehaviorQueryDTO;
import com.k12.common.dto.BehaviorStatsVO;
import com.k12.common.entity.EduResource;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.common.entity.ViewRecord;
import com.k12.resource.mapper.EduResourceMapper;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import com.k12.resource.mapper.ViewRecordMapper;
import com.k12.resource.service.AdminResourceService;
import com.k12.resource.service.UserResourceActionService;
import com.k12.resource.service.ViewRecordService;
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
public class ViewRecordServiceImpl extends ServiceImpl<ViewRecordMapper, ViewRecord> implements ViewRecordService {

    private static final int DETAIL_URL_MAX_LEN = 255;

    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final EduResourceMapper eduResourceMapper;
    private final AdminResourceService adminResourceService;
    private final UserResourceActionService userResourceActionService;
    public ViewRecordServiceImpl(PrimaryChineseResourceMapper primaryChineseResourceMapper, EduResourceMapper eduResourceMapper, AdminResourceService adminResourceService, UserResourceActionService userResourceActionService) {
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.eduResourceMapper = eduResourceMapper;
        this.adminResourceService = adminResourceService;
        this.userResourceActionService = userResourceActionService;
    }


    @Override
    public Map<String, Object> listByPage(Long userId, BehaviorQueryDTO dto) {
        int current = dto.getCurrent() != null && dto.getCurrent() > 0 ? dto.getCurrent() : 1;
        int size = dto.getSize() != null && dto.getSize() > 0 ? dto.getSize() : 20;
        QueryWrapper<ViewRecord> wrapper = baseQuery(userId, dto);
        wrapper.orderByDesc("update_time");
        Page<ViewRecord> page = page(new Page<>(current, size), wrapper);
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
        QueryWrapper<ViewRecord> todayQ = baseQuery(userId, new BehaviorQueryDTO());
        todayQ.ge("update_time", todayStart);
        QueryWrapper<ViewRecord> weekQ = baseQuery(userId, new BehaviorQueryDTO());
        weekQ.ge("update_time", weekStart);
        vo.setTodayCount(count(todayQ));
        vo.setWeekCount(count(weekQ));
        return vo;
    }

    @Override
    public void upsertView(Long userId, Long resourceId, String resourceType, Map<String, Object> snapshot) {
        if (userId == null || resourceId == null) {
            return;
        }
        String type = ResourceTypeConstants.normalize(resourceType);
        QueryWrapper<ViewRecord> existingQ = new QueryWrapper<>();
        existingQ.eq("user_id", userId).eq("resource_id", resourceId).eq("resource_type", type);
        ViewRecord existing = getOne(existingQ, false);
        ViewRecord record = existing != null ? existing : new ViewRecord();
        record.setUserId(userId);
        record.setResourceId(resourceId);
        record.setResourceType(type);
        applySnapshot(record, snapshot);
        if (!StringUtils.hasText(record.getTitle())) {
            fillFromResource(record, resourceId, type);
        }
        if (existing != null) {
            updateById(record);
        } else {
            save(record);
        }
        adminResourceService.recordView(resourceId);
        userResourceActionService.recordView(
                userId, resourceId, type, record.getTitle(), "/api/resource/view");
    }

    @Override
    public void remove(Long userId, Long id) {
        QueryWrapper<ViewRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("id", id);
        remove(wrapper);
    }

    @Override
    public void batchRemove(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        QueryWrapper<ViewRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).in("id", ids);
        remove(wrapper);
    }

    @Override
    public void clearAll(Long userId) {
        QueryWrapper<ViewRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        remove(wrapper);
    }

    private QueryWrapper<ViewRecord> baseQuery(Long userId, BehaviorQueryDTO dto) {
        QueryWrapper<ViewRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (dto != null) {
            if (StringUtils.hasText(dto.getKeyword())) {
                wrapper.like("title", dto.getKeyword().trim());
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

    private void applySnapshot(ViewRecord record, Map<String, Object> snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            return;
        }
        putIfText(record::setTitle, snapshot.get("title"));
        putIfText(record::setSubject, snapshot.get("subject"));
        putIfText(record::setStageKey, snapshot.get("stageKey"));
        putIfText(record::setStage, snapshot.get("stage"));
        putIfText(record::setGradeName, snapshot.get("gradeName"));
        putIfText(record::setTeachingType, snapshot.get("teachingType"));
        putIfText(record::setFileExt, snapshot.get("fileExt"));
        putIfText(record::setOssUrl, snapshot.get("ossUrl"));
        putDetailUrl(record, snapshot.get("url"));
        if (snapshot.get("detailUrl") != null) {
            putDetailUrl(record, snapshot.get("detailUrl"));
        }
    }

    private void putDetailUrl(ViewRecord record, Object value) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return;
        }
        String url = String.valueOf(value).trim();
        if (url.length() > DETAIL_URL_MAX_LEN) {
            url = url.substring(0, DETAIL_URL_MAX_LEN);
        }
        record.setDetailUrl(url);
    }

    private void putIfText(java.util.function.Consumer<String> setter, Object value) {
        if (value != null && StringUtils.hasText(String.valueOf(value))) {
            setter.accept(String.valueOf(value).trim());
        }
    }

    private void fillFromResource(ViewRecord record, Long resourceId, String resourceType) {
        if (ResourceTypeConstants.PRIMARY_CHINESE.equals(resourceType)) {
            PrimaryChineseResource res = primaryChineseResourceMapper.selectById(resourceId);
            if (res == null) {
                return;
            }
            record.setTitle(res.getTitle());
            record.setSubject(res.getSubject());
            record.setStage(res.getStage());
            record.setStageKey(StageKeyHelper.toStageKey(res.getStage()));
            record.setGradeName(res.getGradeName());
            record.setTeachingType(res.getType());
            record.setFileExt(res.getFileExt());
            record.setOssUrl(res.getOssUrl());
            return;
        }
        EduResource res = eduResourceMapper.selectById(resourceId);
        if (res == null) {
            return;
        }
        record.setTitle(res.getTitle());
        record.setSubject(res.getSubjectName());
        record.setGradeName(res.getGradeName());
        record.setTeachingType(res.getResourceTypeName());
        record.setFileExt(res.getFileExt());
        record.setOssUrl(res.getOssUrl());
        record.setStage(res.getStageName());
        record.setStageKey(StageKeyHelper.toStageKey(res.getStageName()));
    }
}
