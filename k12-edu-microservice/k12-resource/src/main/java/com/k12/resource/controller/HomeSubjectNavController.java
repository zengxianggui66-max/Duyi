package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.HomeSubjectNavVO;
import com.k12.resource.service.HomeSubjectNavService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 5-F: home subject detail overlay
 * GET /api/home/subject-nav?stage=primary&subject=chinese
 */
@RestController
@RequestMapping("/api/home")
public class HomeSubjectNavController {

    private final HomeSubjectNavService homeSubjectNavService;
    public HomeSubjectNavController(HomeSubjectNavService homeSubjectNavService) {
        this.homeSubjectNavService = homeSubjectNavService;
    }


    @GetMapping("/subject-nav")
    public Result<HomeSubjectNavVO> subjectNav(
            @RequestParam String stage,
            @RequestParam String subject) {
        return Result.success(homeSubjectNavService.getSubjectNav(stage, subject));
    }
}
