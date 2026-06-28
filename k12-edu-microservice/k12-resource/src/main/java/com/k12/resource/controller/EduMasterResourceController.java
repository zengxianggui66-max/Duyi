package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.PrimaryChineseResourceVO;
import com.k12.common.dto.ResourceWriteDTO;
import com.k12.common.dto.ResourceWriteResultVO;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.resource.security.UserIdResolver;
import com.k12.resource.service.PrimaryChineseResourceService;
import com.k12.resource.service.ResourceWriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * M4：主表写入 API（edu_resource + 双写宽表）
 */
@Slf4j
@RestController
@RequestMapping("/api/resources")
public class EduMasterResourceController {

    private final ResourceWriteService resourceWriteService;
    private final PrimaryChineseResourceService primaryChineseResourceService;
    private final UserIdResolver userIdResolver;
    public EduMasterResourceController(ResourceWriteService resourceWriteService, PrimaryChineseResourceService primaryChineseResourceService, UserIdResolver userIdResolver) {
        this.resourceWriteService = resourceWriteService;
        this.primaryChineseResourceService = primaryChineseResourceService;
        this.userIdResolver = userIdResolver;
    }


    @PostMapping
    public Result<PrimaryChineseResourceVO> create(
            @RequestBody ResourceWriteDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = userIdResolver.resolve(request);
            if (userId != null && dto.getUploaderId() == null) {
                dto.setUploaderId(userId);
            }
            ResourceWriteResultVO result = resourceWriteService.create(dto, userId);
            PrimaryChineseResource saved = primaryChineseResourceService.getById(result.getId());
            return Result.success(PrimaryChineseResourceVO.fromEntity(saved));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("创建资源失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<PrimaryChineseResourceVO> update(
            @PathVariable Long id,
            @RequestBody ResourceWriteDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = userIdResolver.resolve(request);
            ResourceWriteResultVO result = resourceWriteService.update(id, dto, userId);
            PrimaryChineseResource saved = primaryChineseResourceService.getById(result.getId());
            return Result.success(PrimaryChineseResourceVO.fromEntity(saved));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("更新资源失败 id={}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
}
