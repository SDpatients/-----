package com.supplier.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_status_track")
public class BizStatusTrack extends BaseEntity {
    private String businessType;
    private Long businessId;
    private Integer beforeStatus;
    private Integer afterStatus;
    private String trackRemark;
    private Long operator;
    private String operatorName;
    private LocalDateTime operateTime;
}