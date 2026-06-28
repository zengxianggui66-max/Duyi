package com.k12.prep.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.PageResult;
import com.k12.common.dto.QuestionQueryDTO;
import com.k12.common.entity.QuestionBank;
import com.k12.prep.mapper.QuestionBankMapper;
import com.k12.prep.service.QuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements QuestionBankService {

    private final QuestionBankMapper questionBankMapper;

    @Override
    public PageResult<QuestionBank> page(QuestionQueryDTO query) {
        LambdaQueryWrapper<QuestionBank> w = new LambdaQueryWrapper<>();
        w.eq(QuestionBank::getStatus, 1);
        if (StringUtils.hasText(query.getGradeLevel())) {
            w.eq(QuestionBank::getGradeLevel, query.getGradeLevel());
        }
        if (StringUtils.hasText(query.getSubject())) {
            w.eq(QuestionBank::getSubject, query.getSubject());
        }
        if (query.getDifficulty() != null) {
            w.eq(QuestionBank::getDifficulty, query.getDifficulty());
        }
        if (StringUtils.hasText(query.getQuestionType())) {
            w.eq(QuestionBank::getQuestionType, query.getQuestionType());
        }
        if (StringUtils.hasText(query.getSourceType())) {
            w.eq(QuestionBank::getSourceType, query.getSourceType());
        }
        if (StringUtils.hasText(query.getRegion()) && !"all".equals(query.getRegion())) {
            w.and(q -> q.eq(QuestionBank::getRegion, query.getRegion())
                    .or().eq(QuestionBank::getRegion, "national"));
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            w.and(q -> q.like(QuestionBank::getStem, kw)
                    .or().like(QuestionBank::getKnowledgePoints, kw)
                    .or().like(QuestionBank::getSourceName, kw));
        }
        w.orderByDesc(QuestionBank::getUsageCount).orderByDesc(QuestionBank::getId);
        Page<QuestionBank> page = new Page<>(query.getCurrent(), query.getSize());
        Page<QuestionBank> result = questionBankMapper.selectPage(page, w);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public QuestionBank getDetail(Long id) {
        QuestionBank q = questionBankMapper.selectById(id);
        if (q == null) {
            throw new BusinessException("试题不存在");
        }
        q.setUsageCount((q.getUsageCount() == null ? 0 : q.getUsageCount()) + 1);
        questionBankMapper.updateById(q);
        return q;
    }
}
