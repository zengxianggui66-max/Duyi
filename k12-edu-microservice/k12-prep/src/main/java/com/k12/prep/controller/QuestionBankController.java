package com.k12.prep.controller;

import com.k12.common.PageResult;
import com.k12.common.Result;
import com.k12.common.dto.QuestionQueryDTO;
import com.k12.common.entity.QuestionBank;
import com.k12.prep.service.QuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prep/questions")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    @GetMapping("/list")
    public Result<PageResult<QuestionBank>> list(QuestionQueryDTO query) {
        return Result.success(questionBankService.page(query));
    }

    @GetMapping("/detail/{id}")
    public Result<QuestionBank> detail(@PathVariable Long id) {
        return Result.success(questionBankService.getDetail(id));
    }
}
