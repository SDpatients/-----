package com.supplier.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supplier.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_template")
public class MessageTemplate extends BaseEntity {
    private String templateCode;
    private String templateName;
    private Integer channel;
    private String titleTemplate;
    private String contentTemplate;
    private String variables;
    private String businessType;
    private Integer status;
    private String remark;
}