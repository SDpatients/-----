-- ============================================
-- 消息模板表 & 系统配置初始数据
-- ============================================

DROP TABLE IF EXISTS message_template;

CREATE TABLE message_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    template_code VARCHAR(50) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    channel TINYINT NOT NULL DEFAULT 1 COMMENT '推送渠道(1站内信,2邮件,3短信,4企业微信)',
    title_template VARCHAR(500) DEFAULT NULL COMMENT '标题模板',
    content_template TEXT DEFAULT NULL COMMENT '内容模板',
    variables VARCHAR(500) DEFAULT NULL COMMENT '变量列表(逗号分隔)',
    business_type VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0停用,1启用)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),

    UNIQUE KEY uk_template_code (template_code),
    KEY idx_channel_status (channel,status),
    KEY idx_business_type (business_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息模板表';

-- 初始化消息通道配置
INSERT IGNORE INTO sys_config (config_name, config_key, config_value, config_type, encrypted, status, remark) VALUES
('站内信通道', 'message.channel.web.enabled', 'true', 2, 0, 1, '是否启用站内信推送'),
('邮件通道', 'message.channel.mail.enabled', 'true', 2, 0, 1, '是否启用邮件推送'),
('短信通道', 'message.channel.sms.enabled', 'false', 2, 0, 1, '是否启用短信推送'),
('企业微信通道', 'message.channel.wecom.enabled', 'false', 2, 0, 1, '是否启用企业微信推送');

-- 初始化示例消息模板
INSERT IGNORE INTO message_template (template_code, template_name, channel, title_template, content_template, variables, business_type, status, remark) VALUES
('ORDER_PUBLISH', '订单下发通知', 1, '新采购订单待确认', '采购订单${orderNo}已下发，金额￥${totalAmount}，请及时确认。', 'orderNo,totalAmount', 'purchase_order', 1, '订单下发时推送供应商'),
('ORDER_PUBLISH_MAIL', '订单下发通知(邮件)', 2, '新采购订单待确认', '尊敬的供应商：\n\n采购订单${orderNo}已下发，金额￥${totalAmount}，交货日期${deliveryDate}，请登录系统确认。', 'orderNo,totalAmount,deliveryDate', 'purchase_order', 1, '订单下发邮件通知'),
('RECON_SEND', '对账单发送通知', 1, '新对账单待确认', '对账单${reconNo}（周期${reconPeriod}）已发送，金额￥${totalAmount}，请及时确认。', 'reconNo,reconPeriod,totalAmount', 'reconciliation', 1, '对账发送时通知供应商'),
('QUALITY_UNQUALIFIED', '质检不合格通知', 1, '质量检验不合格通知', '物料${materialName}(${materialCode})检验不合格，不合格数量${unqualifiedQty}，请关注后续处理。', 'materialName,materialCode,unqualifiedQty', 'quality_inspection', 1, '检验不合格通知供应商'),
('ORDER_CONFIRMED', '订单已确认通知(邮件)', 2, '订单确认回执', '订单${orderNo}已被供应商确认，预计交付日期${deliveryDate}。', 'orderNo,deliveryDate', 'purchase_order', 1, '供应商确认订单后通知采购方'),
('DELIVERY_RECEIVED', '收货通知', 1, '送货已签收', 'ASN单号${noticeNo}的货物已签收，签收数量${receiptQty}。', 'noticeNo,receiptQty', 'delivery_notice', 0, '暂未启用');