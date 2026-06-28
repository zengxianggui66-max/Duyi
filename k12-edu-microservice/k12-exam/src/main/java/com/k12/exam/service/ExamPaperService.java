package com.k12.exam.service;

import com.k12.common.dto.ExamPaperDTO;
import com.k12.common.entity.ExamPaper;
import java.util.List;
import java.util.Map;

public interface ExamPaperService {
    Map<String, Object> generateExamPaper(Long userId, ExamPaperDTO dto);
    List<ExamPaper> getUserPapers(Long userId);
    ExamPaper getDetail(Long id);
}
