package com.k12.prep.service;

import com.k12.common.PageResult;
import com.k12.common.dto.QuestionQueryDTO;
import com.k12.common.entity.QuestionBank;

public interface QuestionBankService {
    PageResult<QuestionBank> page(QuestionQueryDTO query);

    QuestionBank getDetail(Long id);
}
