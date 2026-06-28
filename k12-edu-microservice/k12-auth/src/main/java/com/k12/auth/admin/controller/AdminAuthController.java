package com.k12.auth.admin.controller;

import com.k12.auth.admin.service.AdminRbacService;
import com.k12.common.BusinessException;
import com.k12.common.Result;
import com.k12.common.dto.AdminMeVO;
import com.k12.common.dto.AdminMenuVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminRbacService adminRbacService;
    public AdminAuthController(AdminRbacService adminRbacService) {
        this.adminRbacService = adminRbacService;
    }


    @GetMapping("/auth/me")
    public Result<AdminMeVO> me(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        return Result.success(adminRbacService.getAdminMe(userId));
    }

    @GetMapping("/menus")
    public Result<List<AdminMenuVO>> menus(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        return Result.success(adminRbacService.listMenus(userId));
    }

    @GetMapping("/permissions")
    public Result<List<String>> permissions(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或Token已过期");
        }
        return Result.success(adminRbacService.listPermissions(userId));
    }
}
