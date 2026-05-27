package com.supplier.quality.dto;

import lombok.Data;

/**
 * 申诉审核 DTO
 */
@Data
public class AppealReviewDTO {

    /** 审核结果: 2=通过, 3=驳回 */
    @jakarta.validation.constraints.NotNull(message = "审核结果不能为空")
    private Integer appealStatus;

    /** 审核意见 */
    private String reviewOpinion;
}