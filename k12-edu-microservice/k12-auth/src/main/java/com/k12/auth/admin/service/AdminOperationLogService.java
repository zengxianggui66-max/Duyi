package com.k12.auth.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.AdminOperationLogVO;
import com.k12.common.entity.SysOperationLog;
import com.k12.common.mapper.SysOperationLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class AdminOperationLogService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysOperationLogMapper sysOperationLogMapper;
    public AdminOperationLogService(SysOperationLogMapper sysOperationLogMapper) {
        this.sysOperationLogMapper = sysOperationLogMapper;
    }


    public Page<AdminOperationLogVO> listLogs(
            int current,
            int size,
            String module,
            String username,
            String action,
            Integer status,
            String startTime,
            String endTime) {
        int pageNo = Math.max(current, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) {
            wrapper.eq(SysOperationLog::getModule, module.trim());
        }
        if (StringUtils.hasText(username)) {
            wrapper.like(SysOperationLog::getUsername, username.trim());
        }
        if (StringUtils.hasText(action)) {
            wrapper.eq(SysOperationLog::getAction, action.trim());
        }
        if (status != null) {
            wrapper.eq(SysOperationLog::getStatus, status);
        }
        LocalDateTime start = parseTime(startTime);
        if (start != null) {
            wrapper.ge(SysOperationLog::getCreateTime, start);
        }
        LocalDateTime end = parseTime(endTime);
        if (end != null) {
            wrapper.le(SysOperationLog::getCreateTime, end);
        }
        wrapper.orderByDesc(SysOperationLog::getCreateTime);
        Page<SysOperationLog> page = sysOperationLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<AdminOperationLogVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVo).toList());
        return result;
    }

    /** 兼容旧 API（module + username 筛选） */
    public Page<AdminOperationLogVO> listLogs(int current, int size, String module, String username) {
        return listLogs(current, size, module, username, null, null, null, null);
    }

    public Page<AdminOperationLogVO> listUserTargetLogs(Long targetUserId, int current, int size) {
        int pageNo = Math.max(current, 1);
        int pageSize = Math.min(Math.max(size, 1), 50);
        String prefix = "/api/admin/users/" + targetUserId;
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOperationLog::getModule, "user");
        wrapper.and(w -> w.likeRight(SysOperationLog::getRequestUri, prefix));
        wrapper.orderByDesc(SysOperationLog::getCreateTime);
        Page<SysOperationLog> page = sysOperationLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<AdminOperationLogVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVo).toList());
        return result;
    }

    private AdminOperationLogVO toVo(SysOperationLog row) {
        AdminOperationLogVO vo = new AdminOperationLogVO();
        vo.setId(row.getId());
        vo.setUserId(row.getUserId());
        vo.setUsername(row.getUsername());
        vo.setModule(row.getModule());
        vo.setAction(row.getAction());
        vo.setPermission(row.getPermission());
        vo.setRequestUri(row.getRequestUri());
        vo.setRequestMethod(row.getRequestMethod());
        vo.setStatus(row.getStatus());
        vo.setErrorMsg(row.getErrorMsg());
        vo.setDurationMs(row.getDurationMs());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }

    private LocalDateTime parseTime(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String text = raw.trim();
        try {
            if (text.length() == 10) {
                return LocalDateTime.parse(text + " 00:00:00", DT);
            }
            return LocalDateTime.parse(text, DT);
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(text);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
