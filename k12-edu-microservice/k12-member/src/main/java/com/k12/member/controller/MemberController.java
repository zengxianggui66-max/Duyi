package com.k12.member.controller;

import com.k12.common.Result;
import com.k12.member.security.UserIdResolver;
import com.k12.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final UserIdResolver userIdResolver;

    public MemberController(MemberService memberService, UserIdResolver userIdResolver) {
        this.memberService = memberService;
        this.userIdResolver = userIdResolver;
    }

    @PostMapping("/order")
    public Result<Map<String, Object>> createOrder(HttpServletRequest request,
                                                   @RequestParam Integer memberLevel) {
        Long userId = userIdResolver.resolve(request);
        return Result.success(memberService.createOrder(userId, memberLevel));
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getMemberInfo(HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        return Result.success(memberService.getMemberInfo(userId));
    }
}
