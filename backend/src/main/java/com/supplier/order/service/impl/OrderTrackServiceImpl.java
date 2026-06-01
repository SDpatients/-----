package com.supplier.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.order.entity.OrderTrack;
import com.supplier.order.mapper.OrderTrackMapper;
import com.supplier.order.service.OrderTrackService;
import com.supplier.order.vo.OrderTrackVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单交付轨迹 Service 实现
 */
@Service
@RequiredArgsConstructor
public class OrderTrackServiceImpl implements OrderTrackService {

    private final OrderTrackMapper orderTrackMapper;

    @Override
    public List<OrderTrackVO> listByOrderId(Long orderId) {
        List<OrderTrack> tracks = orderTrackMapper.selectList(
                new LambdaQueryWrapper<OrderTrack>()
                        .eq(OrderTrack::getOrderId, orderId)
                        .orderByAsc(OrderTrack::getTrackTime));
        return tracks.stream().map(t -> {
            OrderTrackVO vo = new OrderTrackVO();
            vo.setId(t.getId());
            vo.setOrderId(t.getOrderId());
            vo.setTrackStatus(t.getTrackStatus());
            vo.setTrackTime(t.getTrackTime());
            vo.setTrackRemark(t.getTrackRemark());
            vo.setOperator(t.getOperator());
            vo.setOperatorName(t.getOperatorName());
            return vo;
        }).toList();
    }
}