package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminResourceBatchResultVO {

    private int successCount;
    private int skipCount;
    private List<String> skipReasons = new ArrayList<>();
    private String message;

    public static AdminResourceBatchResultVO of(int successCount, int skipCount, List<String> skipReasons, String message) {
        AdminResourceBatchResultVO vo = new AdminResourceBatchResultVO();
        vo.setSuccessCount(successCount);
        vo.setSkipCount(skipCount);
        vo.setSkipReasons(skipReasons);
        vo.setMessage(message);
        return vo;
    }
}
