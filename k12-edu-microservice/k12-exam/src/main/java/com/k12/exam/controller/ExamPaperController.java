package com.k12.exam.controller;

import com.k12.common.Result;
import com.k12.common.dto.ExamPaperDTO;
import com.k12.common.entity.ExamPaper;
import com.k12.exam.service.ExamPaperService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
public class ExamPaperController {

    private final ExamPaperService examPaperService;

    public ExamPaperController(ExamPaperService examPaperService) {
        this.examPaperService = examPaperService;
    }

    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                @Valid @RequestBody ExamPaperDTO dto) {
        return Result.success(examPaperService.generateExamPaper(userId, dto));
    }

    @GetMapping("/history")
    public Result<List<ExamPaper>> getHistory(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(examPaperService.getUserPapers(userId));
    }

    @GetMapping("/detail/{id}")
    public Result<ExamPaper> getDetail(@PathVariable Long id) {
        return Result.success(examPaperService.getDetail(id));
    }
}
