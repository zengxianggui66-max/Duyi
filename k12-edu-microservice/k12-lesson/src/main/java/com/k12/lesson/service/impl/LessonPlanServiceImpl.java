package com.k12.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.dto.LessonPlanDTO;
import com.k12.common.entity.LessonPlan;
import com.k12.lesson.mapper.LessonPlanMapper;
import com.k12.lesson.service.LessonPlanService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LessonPlanServiceImpl implements LessonPlanService {

    private final LessonPlanMapper lessonPlanMapper;

    public LessonPlanServiceImpl(LessonPlanMapper lessonPlanMapper) {
        this.lessonPlanMapper = lessonPlanMapper;
    }

    @Override
    public Map<String, Object> generateLessonPlan(Long userId, LessonPlanDTO dto) {
        LessonPlan plan = new LessonPlan();
        plan.setUserId(userId);
        plan.setTopic(dto.getTopic());
        plan.setGradeLevel(dto.getGradeLevel());
        plan.setSubject(dto.getSubject());
        plan.setGrade(dto.getGrade());
        plan.setVersion(dto.getVersion());
        plan.setTypes(dto.getTypes() != null ? String.join(",", dto.getTypes()) : "courseware,lessonPlan");
        plan.setStatus(0);
        lessonPlanMapper.insert(plan);

        Map<String, Object> result = new HashMap<>();
        result.put("id", plan.getId());
        result.put("topic", dto.getTopic());
        result.put("status", "completed");

        List<Map<String, Object>> courseware = new ArrayList<>();
        courseware.add(Map.of("page", 1, "type", "title", "content", dto.getTopic()));
        courseware.add(Map.of("page", 2, "type", "objectives", "content",
                "知识与技能：理解并掌握" + dto.getTopic() + "的核心概念\n过程与方法：通过观察、比较、归纳\n情感态度：激发学习兴趣"));
        courseware.add(Map.of("page", 3, "type", "key_points", "content",
                "教学重点：" + dto.getTopic() + "的核心原理\n教学难点：从具体到抽象的思维跨越"));
        courseware.add(Map.of("page", 4, "type", "process", "content",
                "1. 情境导入(5min) -> 2. 新知探究(20min) -> 3. 巩固练习(10min) -> 4. 课堂小结(5min)"));
        courseware.add(Map.of("page", 5, "type", "summary", "content", "回顾本节课所学内容，强调核心知识要点。"));
        if (dto.getBasketResourceTitles() != null && !dto.getBasketResourceTitles().isEmpty()) {
            courseware.add(Map.of("page", 6, "type", "basket_resources", "content",
                    "引用资料篮资源：" + String.join("、", dto.getBasketResourceTitles())));
        }
        result.put("courseware", courseware);

        String basketNote = "";
        if (dto.getBasketResourceTitles() != null && !dto.getBasketResourceTitles().isEmpty()) {
            basketNote = "\n\n【资料篮引用】\n" + String.join("\n", dto.getBasketResourceTitles().stream()
                    .map(t -> "· " + t).toList());
        }
        if (dto.getBasketQuestionIds() != null && !dto.getBasketQuestionIds().isEmpty()) {
            basketNote += "\n配套练习：已关联资料篮中 " + dto.getBasketQuestionIds().size() + " 道试题，可在组卷工作台导出。";
        }

        Map<String, Object> lessonPlanContent = new HashMap<>();
        lessonPlanContent.put("objectives", "1.知识与技能：理解并掌握" + dto.getTopic() + "\n2.过程与方法：通过小组合作、自主探究\n3.情感态度：激发学习兴趣，培养学科思维");
        lessonPlanContent.put("keyPoints", "重点：" + dto.getTopic() + "的基本概念和核心原理\n难点：灵活运用知识解决综合问题");
        lessonPlanContent.put("process", "环节一：情境导入(5分钟)\n环节二：新知探究(20分钟)\n环节三：巩固练习(10分钟)\n环节四：课堂小结(5分钟)" + basketNote);
        result.put("lessonPlan", lessonPlanContent);
        if (!basketNote.isEmpty()) {
            result.put("basketContext", basketNote.trim());
        }

        plan.setStatus(1);
        lessonPlanMapper.updateById(plan);

        return result;
    }

    @Override
    public List<LessonPlan> getUserPlans(Long userId) {
        return lessonPlanMapper.selectList(
                new LambdaQueryWrapper<LessonPlan>()
                        .eq(LessonPlan::getUserId, userId)
                        .orderByDesc(LessonPlan::getCreateTime)
                        .last("LIMIT 20"));
    }
}
