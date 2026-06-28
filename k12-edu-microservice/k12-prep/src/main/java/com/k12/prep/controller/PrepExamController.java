package com.k12.prep.controller;

import com.k12.common.Result;
import com.k12.common.dto.AssembleExamDTO;
import com.k12.common.dto.ExamAssemblyVO;
import com.k12.common.dto.ExamPaperSummaryVO;
import com.k12.common.dto.SmartExamDTO;
import com.k12.prep.service.ExamAssemblyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/prep/exam")
@RequiredArgsConstructor
public class PrepExamController {

    private final ExamAssemblyService examAssemblyService;

    @GetMapping("/preview")
    public Result<ExamAssemblyVO> preview(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(examAssemblyService.previewFromBasket(userId));
    }

    @PostMapping("/preview")
    public Result<ExamAssemblyVO> previewWithIds(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AssembleExamDTO dto) {
        return Result.success(examAssemblyService.previewWithQuestionIds(userId, dto));
    }

    @PostMapping("/assemble")
    public Result<ExamAssemblyVO> assemble(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AssembleExamDTO dto) {
        return Result.success(examAssemblyService.assembleFromBasket(userId, dto));
    }

    @PostMapping("/smart-generate")
    public Result<ExamAssemblyVO> smartGenerate(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody SmartExamDTO dto) {
        return Result.success(examAssemblyService.smartGenerate(userId, dto));
    }

    @GetMapping("/papers")
    public Result<List<ExamPaperSummaryVO>> listPapers(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(examAssemblyService.listMyPapers(userId));
    }

    @GetMapping("/papers/{id}")
    public Result<ExamAssemblyVO> paperDetail(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return Result.success(examAssemblyService.getPaperDetail(userId, id));
    }

    @PostMapping("/export/word")
    public ResponseEntity<byte[]> exportWord(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AssembleExamDTO dto,
            @RequestParam(defaultValue = "false") boolean answerOnly) {
        byte[] body = examAssemblyService.exportWord(userId, dto, answerOnly);
        ExamAssemblyVO preview = examAssemblyService.previewWithQuestionIds(userId, dto);
        String filename = (answerOnly ? preview.getTitle() + "-参考答案" : preview.getTitle()) + ".doc";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("application/msword"))
                .body(body);
    }
}
