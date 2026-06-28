package com.k12.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.dto.ExamPaperDTO;
import com.k12.common.entity.ExamPaper;
import com.k12.exam.mapper.ExamPaperMapper;
import com.k12.exam.service.ExamPaperService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExamPaperServiceImpl implements ExamPaperService {

    private final ExamPaperMapper examPaperMapper;
    private final ObjectMapper objectMapper;

    public ExamPaperServiceImpl(ExamPaperMapper examPaperMapper, ObjectMapper objectMapper) {
        this.examPaperMapper = examPaperMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> generateExamPaper(Long userId, ExamPaperDTO dto) {
        ExamPaper paper = new ExamPaper();
        paper.setUserId(userId);
        paper.setTitle(generateTitle(dto));
        paper.setGradeLevel(dto.getGradeLevel());
        paper.setSubject(dto.getSubject());
        paper.setDifficulty(dto.getDifficulty());
        paper.setTotalScore(dto.getTotalScore());
        paper.setDuration(dto.getDuration());
        paper.setQuestionConfig(dto.getQuestionConfig());
        paper.setStatus(0);
        examPaperMapper.insert(paper);

        Map<String, Object> result = new HashMap<>();
        result.put("id", paper.getId());
        result.put("title", paper.getTitle());
        result.put("totalScore", dto.getTotalScore());
        result.put("duration", dto.getDuration());
        result.put("difficulty", dto.getDifficulty());
        result.put("status", "completed");

        List<Map<String, Object>> sections = new ArrayList<>();
        int startNum = 1;
        List<Map<String, Object>> typeConfigs = new ArrayList<>();
        try {
            if (dto.getQuestionConfig() != null) {
                typeConfigs = objectMapper.readValue(dto.getQuestionConfig(), new TypeReference<>() {});
            }
        } catch (Exception ignored) {}

        if (typeConfigs.isEmpty()) {
            typeConfigs = Arrays.asList(
                    Map.of("type", "选择题", "count", 10, "score", 3),
                    Map.of("type", "填空题", "count", 6, "score", 4),
                    Map.of("type", "解答题", "count", 4, "score", 13)
            );
        }

        for (Map<String, Object> tc : typeConfigs) {
            String typeName = (String) tc.get("type");
            int count = ((Number) tc.get("count")).intValue();
            int score = ((Number) tc.get("score")).intValue();

            List<Map<String, Object>> questions = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Map<String, Object> question = new HashMap<>();
                question.put("content", typeName + "示例题目 " + (i + 1) + "（难度" + dto.getDifficulty() + "级）");
                question.put("score", score);
                if ("选择题".equals(typeName)) {
                    question.put("options", Arrays.asList("选项A", "选项B", "选项C", "选项D"));
                }
                questions.add(question);
            }

            sections.add(Map.of(
                    "title", typeName,
                    "startNum", startNum,
                    "count", count,
                    "scorePerQ", score,
                    "totalScore", count * score,
                    "questions", questions
            ));
            startNum += count;
        }

        result.put("sections", sections);
        result.put("totalQuestions", startNum - 1);

        try {
            paper.setQuestions(objectMapper.writeValueAsString(sections));
        } catch (Exception ignored) {}
        paper.setStatus(1);
        examPaperMapper.updateById(paper);

        return result;
    }

    @Override
    public List<ExamPaper> getUserPapers(Long userId) {
        return examPaperMapper.selectList(
                new LambdaQueryWrapper<ExamPaper>()
                        .eq(ExamPaper::getUserId, userId)
                        .orderByDesc(ExamPaper::getCreateTime)
                        .last("LIMIT 20"));
    }

    @Override
    public ExamPaper getDetail(Long id) {
        return examPaperMapper.selectById(id);
    }

    private String generateTitle(ExamPaperDTO dto) {
        String levelName = switch (dto.getGradeLevel()) {
            case "primary" -> "小学";
            case "junior" -> "初中";
            case "senior" -> "高中";
            default -> "";
        };
        return levelName + "智能组卷（" + dto.getTotalScore() + "分/" + dto.getDuration() + "分钟）";
    }
}
