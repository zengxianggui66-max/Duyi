package com.k12.auth.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.AdminSystemLoginLogVO;
import com.k12.common.util.RoleUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminSystemLoginLogService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;
    public AdminSystemLoginLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    private static final RowMapper<AdminSystemLoginLogVO> ROW_MAPPER = (rs, rowNum) -> {
        AdminSystemLoginLogVO vo = new AdminSystemLoginLogVO();
        vo.setId(rs.getLong("id"));
        Object uidObj = rs.getObject("user_id");
        vo.setUserId(uidObj != null ? ((Number) uidObj).longValue() : null);
        vo.setUsername(rs.getString("username"));
        vo.setLoginType(rs.getString("login_type"));
        vo.setSuccess(rs.getInt("success"));
        vo.setFailReason(rs.getString("fail_reason"));
        vo.setIp(rs.getString("ip"));
        vo.setUserAgent(rs.getString("user_agent"));
        Timestamp ts = rs.getTimestamp("create_time");
        vo.setCreateTime(ts != null ? ts.toLocalDateTime() : null);
        return vo;
    };

    public Page<AdminSystemLoginLogVO> listLogs(
            int current,
            int size,
            String username,
            String loginType,
            Integer success,
            Boolean staffOnly,
            String startTime,
            String endTime) {
        int pageNo = Math.max(current, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);

        StringBuilder from = new StringBuilder("""
                FROM user_login_log l
                LEFT JOIN user u ON u.id = l.user_id AND u.deleted = 0
                WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();

        if (StringUtils.hasText(username)) {
            from.append(" AND l.username LIKE ?");
            args.add("%" + username.trim() + "%");
        }
        if (StringUtils.hasText(loginType)) {
            from.append(" AND l.login_type = ?");
            args.add(loginType.trim());
        }
        if (success != null) {
            from.append(" AND l.success = ?");
            args.add(success);
        }
        if (Boolean.TRUE.equals(staffOnly)) {
            from.append(" AND u.role = ?");
            args.add(RoleUtils.STAFF_ROLE);
        } else if (Boolean.FALSE.equals(staffOnly)) {
            from.append(" AND (u.role IS NULL OR u.role <> ?)");
            args.add(RoleUtils.STAFF_ROLE);
        }
        LocalDateTime start = parseTime(startTime);
        if (start != null) {
            from.append(" AND l.create_time >= ?");
            args.add(Timestamp.valueOf(start));
        }
        LocalDateTime end = parseTime(endTime);
        if (end != null) {
            from.append(" AND l.create_time <= ?");
            args.add(Timestamp.valueOf(end));
        }

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) " + from,
                Long.class,
                args.toArray());
        if (total == null) {
            total = 0L;
        }

        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add((pageNo - 1) * pageSize);
        pageArgs.add(pageSize);

        List<AdminSystemLoginLogVO> records = jdbcTemplate.query(
                """
                SELECT l.id, l.user_id, l.username, l.login_type, l.success,
                       l.fail_reason, l.ip, l.user_agent, l.create_time
                """ + from + """
                 ORDER BY l.create_time DESC
                 LIMIT ?, ?
                """,
                ROW_MAPPER,
                pageArgs.toArray());

        Page<AdminSystemLoginLogVO> page = new Page<>(pageNo, pageSize, total);
        page.setRecords(records);
        return page;
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
