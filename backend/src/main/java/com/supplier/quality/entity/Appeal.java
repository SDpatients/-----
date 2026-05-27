package com.supplier.quality.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 质检申诉实体（联动9: 质检申诉 → 申诉通过后关联扣款调整）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("appeal")
public class Appeal extends BaseEntity {

    /** 申诉编号 */
    private String appealNo;

    /** 关联的NCR ID */
    private Long ncrId;

    /** 关联的质检单ID */
    private Long inspectionId;

    /** 关联的扣款单ID */
    private Long deductionId;

    /** 供应商ID */
    private Long supplierId;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 申诉原因 */
    private String appealReason;

    /** 申诉说明 */
    private String appealDesc;

    /** 调整金额（申诉要求调整的金额） */
    private BigDecimal adjustAmount;

    /**
     * 申诉状态:
     * 0=草稿, 1=已提交, 2=审核通过, 3=审核驳回
     */
    private Integer appealStatus;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 审核人 */
    private Long reviewer;

    /** 审核人姓名 */
    private String reviewerName;

    /** 审核时间 */
    private LocalDateTime reviewTime;

    /** 审核意见 */
    private String reviewOpinion;

    /** 备注 */
    private String remark;
}