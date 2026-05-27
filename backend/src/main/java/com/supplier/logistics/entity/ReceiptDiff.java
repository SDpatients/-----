package com.supplier.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("receipt_diff")
public class ReceiptDiff extends BaseEntity {
    private Long recordId;
    private Long noticeId;
    private String materialCode;
    private String materialName;
    private BigDecimal planQty;
    private BigDecimal receiptQty;
    private BigDecimal diffQty;
    private String diffReason;
    /** 处理方式: 1-冲销, 2-退货, 3-让步接收 */
    private Integer handleMethod;
    private String handleRemark;
    private Integer status;
    private String remark;
}