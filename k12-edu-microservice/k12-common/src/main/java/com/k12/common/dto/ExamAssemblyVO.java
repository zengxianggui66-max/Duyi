package com.k12.common.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExamAssemblyVO {
    private Long paperId;
    private String title;
    private Integer totalScore;
    private Integer duration;
    private Integer questionCount;
    /** 试卷结构 JSON，与 exam_paper.questions 一致 */
    private List<Map<String, Object>> sections;
    /** 可打印 HTML */
    private String previewHtml;
    /** 参考答案 HTML */
    private String answerHtml;
}
