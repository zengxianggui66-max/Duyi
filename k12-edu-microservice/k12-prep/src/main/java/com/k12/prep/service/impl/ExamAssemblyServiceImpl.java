package com.k12.prep.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.AssembleExamDTO;
import com.k12.common.dto.ExamAssemblyVO;
import com.k12.common.dto.ExamPaperSummaryVO;
import com.k12.common.dto.SmartExamDTO;
import com.k12.common.entity.ExamPaper;
import com.k12.common.entity.PrepBasketItem;
import com.k12.common.entity.QuestionBank;
import com.k12.prep.mapper.ExamPaperMapper;
import com.k12.prep.mapper.QuestionBankMapper;
import com.k12.prep.service.ExamAssemblyService;
import com.k12.prep.service.PrepBasketService;
import com.k12.prep.util.WordExportHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamAssemblyServiceImpl implements ExamAssemblyService {

    private final QuestionBankMapper questionBankMapper;
    private final ExamPaperMapper examPaperMapper;
    private final PrepBasketService prepBasketService;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> TYPE_NAMES = Map.of(
            "choice", "\u4e00\u3001\u9009\u62e9\u9898",
            "blank", "\u4e8c\u3001\u586b\u7a7a\u9898",
            "answer", "\u4e09\u3001\u89e3\u7b54\u9898",
            "composite", "\u56db\u3001\u7efc\u5408\u9898"
    );

    private static final Map<String, String> SMART_TYPE_MAP = Map.of(
            "choice", "choice",
            "fill", "blank",
            "blank", "blank",
            "judge", "choice",
            "short", "answer",
            "essay", "answer",
            "answer", "answer",
            "composite", "composite"
    );

    @Override
    public ExamAssemblyVO assembleFromBasket(Long userId, AssembleExamDTO dto) {
        if (userId == null) {
            throw new BusinessException(401, "\u8bf7\u5148\u767b\u5f55");
        }
        List<Long> ids = resolveQuestionIds(userId, dto.getQuestionIds());
        return buildAndSave(userId, dto, loadOrderedQuestions(ids));
    }

    @Override
    public ExamAssemblyVO previewFromBasket(Long userId) {
        return previewWithQuestionIds(userId, new AssembleExamDTO());
    }

    @Override
    public ExamAssemblyVO previewWithQuestionIds(Long userId, AssembleExamDTO dto) {
        if (userId == null) {
            throw new BusinessException(401, "\u8bf7\u5148\u767b\u5f55");
        }
        List<Long> ids = resolveQuestionIds(userId, dto.getQuestionIds());
        List<QuestionBank> ordered = loadOrderedQuestions(ids);
        if (dto.getQuestionIds() == null || dto.getQuestionIds().isEmpty()) {
            dto.setQuestionIds(ids);
        }
        return buildPreview(dto, ordered);
    }

    @Override
    public ExamAssemblyVO smartGenerate(Long userId, SmartExamDTO dto) {
        if (userId == null) {
            throw new BusinessException(401, "\u8bf7\u5148\u767b\u5f55");
        }
        if (!StringUtils.hasText(dto.getGradeLevel()) || !StringUtils.hasText(dto.getSubject())) {
            throw new BusinessException("\u8bf7\u914d\u7f6e\u5b66\u6bb5\u4e0e\u5b66\u79d1");
        }
        int difficulty = dto.getDifficulty() == null ? 3 : dto.getDifficulty();
        LinkedHashSet<Long> pickedIds = new LinkedHashSet<>();

        if (Boolean.TRUE.equals(dto.getUseBasketQuestions())) {
            prepBasketService.getBasket(userId).getItems().stream()
                    .filter(i -> "question".equals(i.getItemType()))
                    .map(PrepBasketItem::getRefId)
                    .forEach(pickedIds::add);
        }

        if (dto.getTypeCounts() != null) {
            for (SmartExamDTO.QuestionTypeCount tc : dto.getTypeCounts()) {
                if (tc.getCount() == null || tc.getCount() <= 0) {
                    continue;
                }
                String qType = SMART_TYPE_MAP.getOrDefault(tc.getQuestionType(), tc.getQuestionType());
                pickRandomQuestions(dto.getGradeLevel(), dto.getSubject(), qType, difficulty, tc.getCount(), pickedIds)
                        .forEach(q -> pickedIds.add(q.getId()));
            }
        }

        if (pickedIds.isEmpty()) {
            throw new BusinessException("\u672a\u5339\u914d\u5230\u8bd5\u9898\uff0c\u8bf7\u8c03\u6574\u9898\u578b\u914d\u7f6e\u6216\u5148\u5411\u8d44\u6599\u7bee\u6dfb\u52a0\u8bd5\u9898");
        }

        List<Long> ids = new ArrayList<>(pickedIds);
        List<QuestionBank> ordered = loadOrderedQuestions(ids);

        AssembleExamDTO assemble = new AssembleExamDTO();
        assemble.setQuestionIds(ids);
        assemble.setGradeLevel(dto.getGradeLevel());
        assemble.setSubject(dto.getSubject());
        assemble.setDifficulty(difficulty);
        assemble.setDuration(dto.getDuration() != null ? dto.getDuration() : 90);
        assemble.setTitle(StringUtils.hasText(dto.getTitle())
                ? dto.getTitle()
                : buildSmartTitle(dto.getGradeLevel(), dto.getSubject()));
        return buildAndSave(userId, assemble, ordered);
    }

    @Override
    public byte[] exportWord(Long userId, AssembleExamDTO dto, boolean answerOnly) {
        ExamAssemblyVO vo = previewWithQuestionIds(userId, dto);
        String html = answerOnly ? vo.getAnswerHtml() : vo.getPreviewHtml();
        String title = answerOnly ? vo.getTitle() + "-\u53c2\u8003\u7b54\u6848" : vo.getTitle();
        return WordExportHelper.toDocBytes(title, html);
    }

    @Override
    public List<ExamPaperSummaryVO> listMyPapers(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "\u8bf7\u5148\u767b\u5f55");
        }
        List<ExamPaper> papers = examPaperMapper.selectList(new LambdaQueryWrapper<ExamPaper>()
                .eq(ExamPaper::getUserId, userId)
                .eq(ExamPaper::getStatus, 1)
                .orderByDesc(ExamPaper::getCreateTime)
                .last("LIMIT 50"));
        List<ExamPaperSummaryVO> list = new ArrayList<>();
        for (ExamPaper p : papers) {
            ExamPaperSummaryVO vo = new ExamPaperSummaryVO();
            vo.setId(p.getId());
            vo.setTitle(p.getTitle());
            vo.setGradeLevel(p.getGradeLevel());
            vo.setSubject(p.getSubject());
            vo.setTotalScore(p.getTotalScore());
            vo.setDuration(p.getDuration());
            vo.setQuestionCount(countQuestions(p.getQuestions()));
            vo.setCreateTime(p.getCreateTime());
            list.add(vo);
        }
        return list;
    }

    @Override
    public ExamAssemblyVO getPaperDetail(Long userId, Long paperId) {
        if (userId == null) {
            throw new BusinessException(401, "\u8bf7\u5148\u767b\u5f55");
        }
        ExamPaper paper = examPaperMapper.selectById(paperId);
        if (paper == null || !userId.equals(paper.getUserId())) {
            throw new BusinessException("\u8bd5\u5377\u4e0d\u5b58\u5728");
        }
        List<Long> ids = extractQuestionIds(paper.getQuestions());
        if (ids.isEmpty()) {
            ExamAssemblyVO vo = new ExamAssemblyVO();
            vo.setPaperId(paper.getId());
            vo.setTitle(paper.getTitle());
            vo.setTotalScore(paper.getTotalScore());
            vo.setDuration(paper.getDuration());
            vo.setPreviewHtml("<p>\u6682\u65e0\u8bd5\u9898\u5185\u5bb9</p>");
            return vo;
        }
        AssembleExamDTO dto = new AssembleExamDTO();
        dto.setTitle(paper.getTitle());
        dto.setQuestionIds(ids);
        dto.setGradeLevel(paper.getGradeLevel());
        dto.setSubject(paper.getSubject());
        dto.setDuration(paper.getDuration());
        dto.setDifficulty(paper.getDifficulty());
        ExamAssemblyVO vo = buildPreview(dto, loadOrderedQuestions(ids));
        vo.setPaperId(paper.getId());
        return vo;
    }

    private List<QuestionBank> pickRandomQuestions(
            String gradeLevel, String subject, String questionType,
            int difficulty, int count, Set<Long> excludeIds) {
        int minD = Math.max(1, difficulty - 1);
        int maxD = Math.min(5, difficulty + 1);
        LambdaQueryWrapper<QuestionBank> w = new LambdaQueryWrapper<>();
        w.eq(QuestionBank::getStatus, 1)
                .eq(QuestionBank::getGradeLevel, gradeLevel)
                .eq(QuestionBank::getSubject, subject)
                .eq(QuestionBank::getQuestionType, questionType)
                .between(QuestionBank::getDifficulty, minD, maxD);
        if (!excludeIds.isEmpty()) {
            w.notIn(QuestionBank::getId, excludeIds);
        }
        w.last("ORDER BY RAND() LIMIT " + count);
        List<QuestionBank> found = new ArrayList<>(questionBankMapper.selectList(w));
        if (found.size() < count) {
            LambdaQueryWrapper<QuestionBank> w2 = new LambdaQueryWrapper<>();
            w2.eq(QuestionBank::getStatus, 1)
                    .eq(QuestionBank::getGradeLevel, gradeLevel)
                    .eq(QuestionBank::getSubject, subject)
                    .eq(QuestionBank::getQuestionType, questionType);
            if (!excludeIds.isEmpty()) {
                w2.notIn(QuestionBank::getId, excludeIds);
            }
            w2.last("ORDER BY RAND() LIMIT " + (count - found.size()));
            found.addAll(questionBankMapper.selectList(w2));
        }
        for (QuestionBank q : found) {
            q.setUsageCount((q.getUsageCount() == null ? 0 : q.getUsageCount()) + 1);
            questionBankMapper.updateById(q);
        }
        return found;
    }

    private List<Long> resolveQuestionIds(Long userId, List<Long> fromDto) {
        if (fromDto != null && !fromDto.isEmpty()) {
            return fromDto;
        }
        return prepBasketService.getBasket(userId).getItems().stream()
                .filter(i -> "question".equals(i.getItemType()))
                .sorted(Comparator.comparingInt(i -> i.getSortOrder() == null ? 0 : i.getSortOrder()))
                .map(PrepBasketItem::getRefId)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("deprecation")
    private List<QuestionBank> loadOrderedQuestions(List<Long> ids) {
        if (ids.isEmpty()) {
            throw new BusinessException("\u8d44\u6599\u7bee\u4e2d\u6ca1\u6709\u8bd5\u9898\uff0c\u8bf7\u5148\u9009\u9898");
        }
        List<QuestionBank> list = questionBankMapper.selectBatchIds(ids);
        Map<Long, QuestionBank> map = list.stream()
                .collect(Collectors.toMap(QuestionBank::getId, q -> q, (a, b) -> a));
        List<QuestionBank> ordered = new ArrayList<>();
        for (Long id : ids) {
            QuestionBank q = map.get(id);
            if (q != null) {
                ordered.add(q);
            }
        }
        if (ordered.isEmpty()) {
            throw new BusinessException("\u8bd5\u9898\u4e0d\u5b58\u5728\u6216\u5df2\u4e0b\u67b6");
        }
        return ordered;
    }

    private ExamAssemblyVO buildAndSave(Long userId, AssembleExamDTO dto, List<QuestionBank> ordered) {
        ExamAssemblyVO vo = buildPreview(dto, ordered);
        try {
            ExamPaper paper = new ExamPaper();
            paper.setUserId(userId);
            paper.setTitle(vo.getTitle());
            paper.setGradeLevel(StringUtils.hasText(dto.getGradeLevel())
                    ? dto.getGradeLevel() : ordered.get(0).getGradeLevel());
            paper.setSubject(StringUtils.hasText(dto.getSubject())
                    ? dto.getSubject() : ordered.get(0).getSubject());
            paper.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 3);
            paper.setTotalScore(vo.getTotalScore());
            paper.setDuration(dto.getDuration() != null ? dto.getDuration() : 90);
            paper.setQuestions(objectMapper.writeValueAsString(vo.getSections()));
            paper.setStatus(1);
            examPaperMapper.insert(paper);
            vo.setPaperId(paper.getId());
        } catch (Exception e) {
            throw new BusinessException("\u4fdd\u5b58\u8bd5\u5377\u5931\u8d25");
        }
        return vo;
    }

    private ExamAssemblyVO buildPreview(AssembleExamDTO dto, List<QuestionBank> ordered) {
        LinkedHashMap<String, List<QuestionBank>> grouped = new LinkedHashMap<>();
        for (QuestionBank q : ordered) {
            String type = StringUtils.hasText(q.getQuestionType()) ? q.getQuestionType() : "answer";
            grouped.computeIfAbsent(type, k -> new ArrayList<>()).add(q);
        }

        List<Map<String, Object>> sections = new ArrayList<>();
        StringBuilder html = new StringBuilder();
        StringBuilder answerHtml = new StringBuilder();
        html.append("<div class=\"exam-paper-preview\">");
        answerHtml.append("<div class=\"exam-answer-preview\"><h1>\u53c2\u8003\u7b54\u6848</h1>");

        String title = StringUtils.hasText(dto.getTitle()) ? dto.getTitle() : "\u8d44\u6599\u7bee\u7ec4\u5377\u8bd5\u5377";
        html.append("<h1>").append(escapeHtml(title)).append("</h1>");

        int totalScore = 0;
        int globalNum = 1;
        for (Map.Entry<String, List<QuestionBank>> entry : grouped.entrySet()) {
            String sectionTitle = TYPE_NAMES.getOrDefault(entry.getKey(), entry.getKey());
            html.append("<h2>").append(escapeHtml(sectionTitle)).append("</h2>");
            answerHtml.append("<h2>").append(escapeHtml(sectionTitle)).append("</h2>");
            List<Map<String, Object>> sectionQuestions = new ArrayList<>();
            for (QuestionBank q : entry.getValue()) {
                int score = q.getScore() == null ? 5 : q.getScore().intValue();
                totalScore += score;
                html.append("<div class='q-item'><p><b>").append(globalNum).append(".</b> ")
                        .append("\uff08").append(score).append("\u5206\uff09").append(q.getStem()).append("</p>");
                answerHtml.append("<div class='q-item'><p><b>").append(globalNum).append(".</b> ");
                answerHtml.append(StringUtils.hasText(q.getAnswer()) ? q.getAnswer() : "\u7565");
                answerHtml.append("</p>");
                if (StringUtils.hasText(q.getAnalysis())) {
                    answerHtml.append("<p class='analysis'><i>\u89e3\u6790\uff1a</i>").append(q.getAnalysis()).append("</p>");
                }
                answerHtml.append("</div>");

                if ("choice".equals(q.getQuestionType()) && StringUtils.hasText(q.getOptionsJson())) {
                    try {
                        List<String> opts = objectMapper.readValue(q.getOptionsJson(), new TypeReference<>() {});
                        html.append("<ul>");
                        for (String o : opts) {
                            html.append("<li>").append(escapeHtml(o)).append("</li>");
                        }
                        html.append("</ul>");
                    } catch (Exception ignored) {
                    }
                }
                html.append("</div>");

                Map<String, Object> qm = new HashMap<>();
                qm.put("id", q.getId());
                qm.put("num", globalNum);
                qm.put("content", stripTags(q.getStem()));
                qm.put("score", score);
                qm.put("type", q.getQuestionType());
                qm.put("answer", stripTags(q.getAnswer()));
                sectionQuestions.add(qm);
                globalNum++;
            }
            sections.add(Map.of("title", sectionTitle, "questions", sectionQuestions));
        }
        html.append("<p class='total'>\u6ee1\u5206 ").append(totalScore).append(" \u5206</p></div>");
        answerHtml.append("</div>");

        ExamAssemblyVO vo = new ExamAssemblyVO();
        vo.setTitle(title);
        vo.setTotalScore(totalScore);
        vo.setDuration(dto.getDuration() != null ? dto.getDuration() : 90);
        vo.setQuestionCount(ordered.size());
        vo.setSections(sections);
        vo.setPreviewHtml(html.toString());
        vo.setAnswerHtml(answerHtml.toString());
        return vo;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String stripTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", "");
    }

    private static String buildSmartTitle(String gradeLevel, String subject) {
        String level = switch (gradeLevel) {
            case "primary" -> "\u5c0f\u5b66";
            case "junior" -> "\u521d\u4e2d";
            case "senior" -> "\u9ad8\u4e2d";
            default -> "";
        };
        return level + subject + "\u667a\u80fd\u7ec4\u5377\u8bd5\u5377";
    }

    private int countQuestions(String questionsJson) {
        if (!StringUtils.hasText(questionsJson)) {
            return 0;
        }
        try {
            List<Map<String, Object>> sections = objectMapper.readValue(questionsJson, new TypeReference<>() {});
            int n = 0;
            for (Map<String, Object> sec : sections) {
                Object qs = sec.get("questions");
                if (qs instanceof List<?> list) {
                    n += list.size();
                }
            }
            return n;
        } catch (Exception e) {
            return 0;
        }
    }

    private List<Long> extractQuestionIds(String questionsJson) {
        if (!StringUtils.hasText(questionsJson)) {
            return List.of();
        }
        try {
            List<Map<String, Object>> sections = objectMapper.readValue(questionsJson, new TypeReference<>() {});
            List<Long> ids = new ArrayList<>();
            for (Map<String, Object> sec : sections) {
                Object qs = sec.get("questions");
                if (qs instanceof List<?> list) {
                    for (Object o : list) {
                        if (o instanceof Map<?, ?> m && m.get("id") != null) {
                            ids.add(((Number) m.get("id")).longValue());
                        }
                    }
                }
            }
            return ids;
        } catch (Exception e) {
            return List.of();
        }
    }
}
