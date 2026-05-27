package com.supplier.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.order.dto.DeliveryFeedbackCreateDTO;
import com.supplier.order.dto.DeliveryFeedbackLineDTO;
import com.supplier.order.entity.DeliveryFeedback;
import com.supplier.order.entity.DeliveryFeedbackLine;
import com.supplier.order.entity.OrderTrack;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.entity.PurchaseOrderDetail;
import com.supplier.order.mapper.DeliveryFeedbackLineMapper;
import com.supplier.order.mapper.DeliveryFeedbackMapper;
import com.supplier.order.mapper.OrderTrackMapper;
import com.supplier.order.mapper.PurchaseOrderDetailMapper;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.order.service.DeliveryFeedbackService;
import com.supplier.order.vo.DeliveryFeedbackLineVO;
import com.supplier.order.vo.DeliveryFeedbackVO;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryFeedbackServiceImpl implements DeliveryFeedbackService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_SUBMITTED = 1;
    private static final int STATUS_CONFIRMED_BY_BUYER = 2;
    private static final int STATUS_REJECTED_BY_BUYER = 3;

    private final DeliveryFeedbackMapper feedbackMapper;
    private final DeliveryFeedbackLineMapper feedbackLineMapper;
    private final PurchaseOrderMapper orderMapper;
    private final PurchaseOrderDetailMapper detailMapper;
    private final OrderTrackMapper orderTrackMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(Long orderId, DeliveryFeedbackCreateDTO dto) {
        PurchaseOrder order = getOrder(orderId);
        List<PurchaseOrderDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderDetail>().eq(PurchaseOrderDetail::getOrderId, orderId));
        if (details.isEmpty()) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "订单无明细数据");
        }

        Map<Long, PurchaseOrderDetail> detailMap = details.stream()
                .collect(Collectors.toMap(PurchaseOrderDetail::getId, d -> d));

        // 校验所有明细ID是否属于该订单
        for (DeliveryFeedbackLineDTO line : dto.getLines()) {
            if (!detailMap.containsKey(line.getOrderDetailId())) {
                throw BusinessException.of(ResultCode.PARAM_ERROR.getCode(),
                        "明细ID " + line.getOrderDetailId() + " 不属于该订单");
            }
        }

        DeliveryFeedback feedback = new DeliveryFeedback();
        feedback.setOrderId(orderId);
        feedback.setOrderNo(order.getOrderNo());
        feedback.setSupplierId(order.getSupplierId());
        feedback.setFeedbackStatus(STATUS_SUBMITTED);
        feedback.setRemark(dto.getRemark());
        feedbackMapper.insert(feedback);

        List<DeliveryFeedbackLine> lines = new ArrayList<>();
        for (DeliveryFeedbackLineDTO lineDto : dto.getLines()) {
            PurchaseOrderDetail detail = detailMap.get(lineDto.getOrderDetailId());
            DeliveryFeedbackLine line = new DeliveryFeedbackLine();
            line.setFeedbackId(feedback.getId());
            line.setOrderDetailId(lineDto.getOrderDetailId());
            line.setMaterialCode(detail.getMaterialCode());
            line.setMaterialName(detail.getMaterialName());
            line.setPromisedDeliveryDate(lineDto.getPromisedDeliveryDate());
            line.setPlannedQuantity(lineDto.getPlannedQuantity());
            line.setBatchNo(lineDto.getBatchNo());
            line.setRemark(lineDto.getRemark());
            lines.add(line);
        }
        for (DeliveryFeedbackLine line : lines) {
            feedbackLineMapper.insert(line);
        }

        writeTrack(order, STATUS_SUBMITTED, "供应商提交交期反馈");

        return feedback.getId();
    }

    @Override
    public DeliveryFeedbackVO getByOrderId(Long orderId) {
        List<DeliveryFeedback> feedbacks = feedbackMapper.selectList(
                new LambdaQueryWrapper<DeliveryFeedback>()
                        .eq(DeliveryFeedback::getOrderId, orderId)
                        .orderByDesc(DeliveryFeedback::getCreateTime));
        if (feedbacks.isEmpty()) {
            return null;
        }
        DeliveryFeedback latest = feedbacks.get(0);
        return toVO(latest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmByBuyer(Long feedbackId) {
        DeliveryFeedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "交期反馈不存在");
        }
        if (!Integer.valueOf(STATUS_SUBMITTED).equals(feedback.getFeedbackStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交状态才能确认");
        }
        feedback.setFeedbackStatus(STATUS_CONFIRMED_BY_BUYER);
        feedback.setBuyerConfirmBy(SecurityUtils.getUserId());
        feedback.setBuyerConfirmTime(LocalDateTime.now());
        feedbackMapper.updateById(feedback);

        PurchaseOrder order = orderMapper.selectById(feedback.getOrderId());
        if (order != null) {
            writeTrack(order, STATUS_CONFIRMED_BY_BUYER, "采购方确认交期反馈");
        }
    }

    private PurchaseOrder getOrder(Long orderId) {
        PurchaseOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (SecurityUtils.isSupplierUser() && !order.getSupplierId().equals(SecurityUtils.getSupplierId())) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return order;
    }

    private DeliveryFeedbackVO toVO(DeliveryFeedback feedback) {
        DeliveryFeedbackVO vo = new DeliveryFeedbackVO();
        vo.setId(feedback.getId());
        vo.setOrderId(feedback.getOrderId());
        vo.setOrderNo(feedback.getOrderNo());
        vo.setSupplierId(feedback.getSupplierId());
        vo.setFeedbackStatus(feedback.getFeedbackStatus());
        vo.setRemark(feedback.getRemark());
        vo.setBuyerConfirmBy(feedback.getBuyerConfirmBy());
        vo.setBuyerConfirmTime(feedback.getBuyerConfirmTime());

        List<DeliveryFeedbackLine> lines = feedbackLineMapper.selectList(
                new LambdaQueryWrapper<DeliveryFeedbackLine>()
                        .eq(DeliveryFeedbackLine::getFeedbackId, feedback.getId()));
        vo.setLines(lines.stream().map(l -> {
            DeliveryFeedbackLineVO lvo = new DeliveryFeedbackLineVO();
            lvo.setId(l.getId());
            lvo.setFeedbackId(l.getFeedbackId());
            lvo.setOrderDetailId(l.getOrderDetailId());
            lvo.setMaterialCode(l.getMaterialCode());
            lvo.setMaterialName(l.getMaterialName());
            lvo.setPromisedDeliveryDate(l.getPromisedDeliveryDate());
            lvo.setPlannedQuantity(l.getPlannedQuantity());
            lvo.setBatchNo(l.getBatchNo());
            lvo.setRemark(l.getRemark());
            return lvo;
        }).toList());

        return vo;
    }

    private void writeTrack(PurchaseOrder order, Integer status, String remark) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        OrderTrack track = new OrderTrack();
        track.setOrderId(order.getId());
        track.setTrackStatus(status);
        track.setTrackTime(LocalDateTime.now());
        track.setTrackRemark(remark);
        track.setOperator(loginUser == null ? null : loginUser.getUserId());
        track.setOperatorName(loginUser == null ? null : loginUser.getRealName());
        orderTrackMapper.insert(track);
    }
}