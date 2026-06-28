package com.k12.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.annotation.AdminLog;
import com.k12.common.entity.SysOperationLog;
import com.k12.common.mapper.SysOperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 管理端写操作审计。勿加 {@code @ConditionalOnBean(SysOperationLogMapper)}，
 * 否则 MyBatis Mapper 注册晚于条件评估时 Aspect 不会加载，日志永远写不进去。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "k12.admin.rbac", name = "enabled", havingValue = "true")
public class AdminLogAspect {

    private final SysOperationLogMapper sysOperationLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(adminLog)")
    public Object around(ProceedingJoinPoint joinPoint, AdminLog adminLog) throws Throwable {
        long start = System.currentTimeMillis();
        SysOperationLog row = new SysOperationLog();
        row.setModule(adminLog.module());
        row.setAction(adminLog.action());
        row.setPermission(StringUtils.hasText(adminLog.permission()) ? adminLog.permission() : null);
        fillRequestInfo(row);
        try {
            Object result = joinPoint.proceed();
            row.setStatus(1);
            return result;
        } catch (Throwable ex) {
            row.setStatus(0);
            row.setErrorMsg(ex.getMessage());
            throw ex;
        } finally {
            row.setDurationMs((int) (System.currentTimeMillis() - start));
            try {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                if (joinPoint.getArgs().length > 0) {
                    row.setRequestParams(objectMapper.writeValueAsString(joinPoint.getArgs()[0]));
                } else {
                    row.setRequestParams(signature.getMethod().getName());
                }
            } catch (Exception ignored) {
                // ignore serialization errors
            }
            persist(row);
        }
    }

    private void fillRequestInfo(SysOperationLog row) {
        row.setUserId(0L);
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }
        HttpServletRequest request = attrs.getRequest();
        row.setRequestUri(request.getRequestURI());
        row.setRequestMethod(request.getMethod());
        row.setIp(request.getRemoteAddr());
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        if (StringUtils.hasText(userId)) {
            row.setUserId(Long.parseLong(userId));
        }
        row.setUsername(username);
    }

    private void persist(SysOperationLog row) {
        try {
            sysOperationLogMapper.insert(row);
        } catch (Exception ex) {
            log.error("写入 sys_operation_log 失败 module={} action={}: {}",
                    row.getModule(), row.getAction(), ex.getMessage(), ex);
        }
    }
}
