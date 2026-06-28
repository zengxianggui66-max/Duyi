package com.k12.lesson.service;

import com.k12.common.entity.LessonPlan;
import java.util.List;
import java.util.Map;

public interface LessonPlanService {
    Map<String, Object> generateLessonPlan(Long userId, com.k12.common.dto.LessonPlanDTO dto);
    List<LessonPlan> getUserPlans(Long userId);
}
