package com.supplier.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_idempotent_record")
public class SysIdempotentRecord extends BaseEntity {

    private String idempotentKey;
    private String businessType;
    private Long businessId;
    private String requestHash;
    private String resultCode;
    private String resultMessage;
    private LocalDateTime expireTime;
    private Integer status;
}