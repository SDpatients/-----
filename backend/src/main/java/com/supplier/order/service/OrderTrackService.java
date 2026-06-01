package com.supplier.order.service;

import com.supplier.order.vo.OrderTrackVO;

import java.util.List;

/**
 * 订单交付轨迹 Service
 */
public interface OrderTrackService {

    /**
     * 按订单 ID 查询轨迹列表
     */
    List<OrderTrackVO> listByOrderId(Long orderId);
}