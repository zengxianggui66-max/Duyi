package com.k12.lesson.controller;

import com.k12.common.Result;
import com.k12.common.dto.LessonPlanDTO;
import com.k12.common.entity.LessonPlan;
import com.k12.lesson.service.LessonPlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lesson")
public class LessonPlanController {

    private final LessonPlanService lessonPlanService;

    public LessonPlanController(LessonPlanService lessonPlanService) {
        this.lessonPlanService = lessonPlanService;
    }

    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                @Valid @RequestBody LessonPlanDTO dto) {
        return Result.success(lessonPlanService.generateLessonPlan(userId, dto));
    }

    @GetMapping("/history")
    public Result<List<LessonPlan>> getHistory(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(lessonPlanService.getUserPlans(userId));
    }
}
