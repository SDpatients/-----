package com.supplier.order.service;

import com.supplier.order.dto.DeliveryFeedbackCreateDTO;
import com.supplier.order.vo.DeliveryFeedbackVO;

public interface DeliveryFeedbackService {

    /**
     * 供应商提交交期反馈
     */
    Long submit(Long orderId, DeliveryFeedbackCreateDTO dto);

    /**
     * 查询订单的交期反馈
     */
    DeliveryFeedbackVO getByOrderId(Long orderId);

    /**
     * 采购方确认交期反馈
     */
    void confirmByBuyer(Long feedbackId);
}