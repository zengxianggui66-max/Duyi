package com.k12.prep.service;

import com.k12.common.dto.AssembleExamDTO;
import com.k12.common.dto.ExamAssemblyVO;
import com.k12.common.dto.ExamPaperSummaryVO;
import com.k12.common.dto.SmartExamDTO;

import java.util.List;

public interface ExamAssemblyService {
    ExamAssemblyVO assembleFromBasket(Long userId, AssembleExamDTO dto);

    ExamAssemblyVO previewFromBasket(Long userId);

    ExamAssemblyVO previewWithQuestionIds(Long userId, AssembleExamDTO dto);

    ExamAssemblyVO smartGenerate(Long userId, SmartExamDTO dto);

    byte[] exportWord(Long userId, AssembleExamDTO dto, boolean answerOnly);

    List<ExamPaperSummaryVO> listMyPapers(Long userId);

    ExamAssemblyVO getPaperDetail(Long userId, Long paperId);
}
