package com.supplier.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.annotation.AuditLog;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.order.dto.OrderChangeApproveDTO;
import com.supplier.order.dto.OrderChangeCreateDTO;
import com.supplier.order.entity.OrderChange;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.mapper.OrderChangeMapper;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.order.query.OrderChangeQuery;
import com.supplier.order.service.OrderChangeService;
import com.supplier.order.vo.OrderChangeVO;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderChangeServiceImpl implements OrderChangeService {
    private final OrderChangeMapper orderChangeMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;

    @Override
    public PageResult<OrderChangeVO> page(OrderChangeQuery query) {
        // 供应商用户只能查看自己订单的变更记录
        List<Long> scopedOrderIds = getScopedOrderIds(query.getOrderId());
        if (scopedOrderIds.isEmpty()) {
            return PageResult.of(new Page<>());
        }
        LambdaQueryWrapper<OrderChange> wrapper = new LambdaQueryWrapper<OrderChange>()
                .in(OrderChange::getOrderId, scopedOrderIds)
                .orderByDesc(OrderChange::getCreateTime);
        Page<OrderChange> page = orderChangeMapper.selectPage(new Page<>(query.getPageNum() != null ? query.getPageNum() : 1, query.getPageSize() != null ? query.getPageSize() : 10), wrapper);
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public OrderChangeVO getDetail(Long id) {
        return toVO(getWithScope(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrderChangeCreateDTO dto) {
        // 供应商用户创建变更时校验订单归属
        checkOrderScope(dto.getOrderId());
        OrderChange entity = new OrderChange();
        entity.setOrderId(dto.getOrderId());
        entity.setOrderDetailId(dto.getOrderDetailId());
        entity.setChangeType(dto.getChangeType());
        entity.setChangeContent(dto.getChangeContent());
        entity.setBeforeValue(dto.getBeforeValue());
        entity.setAfterValue(dto.getAfterValue());
        entity.setChangeReason(dto.getChangeReason());
        entity.setApplyBy(SecurityUtils.getUserId());
        entity.setApplyTime(LocalDateTime.now());
        entity.setApproveStatus(0);
        orderChangeMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "订单变更", businessType = "order_change", action = "变更审批", businessIdExpr = "#id")
    public void approve(Long id, OrderChangeApproveDTO dto) {
        OrderChange entity = getWithScope(id);
        if (!Integer.valueOf(0).equals(entity.getApproveStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前变更记录不允许审批");
        }
        entity.setApproveStatus(dto.getApproveStatus());
        entity.setApproveRemark(dto.getApproveRemark());
        entity.setApproveBy(SecurityUtils.getUserId());
        entity.setApproveTime(LocalDateTime.now());
        orderChangeMapper.updateById(entity);
    }

    private OrderChange getWithScope(Long id) {
        OrderChange entity = orderChangeMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        checkOrderScope(entity.getOrderId());
        return entity;
    }

    /**
     * 校验订单归属：供应商用户只能操作自己供应商的订单变更
     */
    private void checkOrderScope(Long orderId) {
        if (orderId == null) {
            return;
        }
        if (!SecurityUtils.isSupplierUser()) {
            return;
        }
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "关联订单不存在");
        }
        Long supplierId = SecurityUtils.getSupplierId();
        if (order.getSupplierId() == null || !order.getSupplierId().equals(supplierId)) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
    }

    /**
     * 获取供应商用户可访问的订单变更 orderId 列表
     */
    private List<Long> getScopedOrderIds(Long queryOrderId) {
        if (!SecurityUtils.isSupplierUser()) {
            // 非供应商用户，直接按 queryOrderId 查询（如指定）
            return queryOrderId != null ? List.of(queryOrderId) : getAllOrderChangeOrderIds();
        }
        // 供应商用户：只获取自己供应商的订单
        Long supplierId = SecurityUtils.getSupplierId();
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(PurchaseOrder::getSupplierId, supplierId)
                        .eq(queryOrderId != null, PurchaseOrder::getId, queryOrderId));
        List<Long> orderIds = orders.stream().map(PurchaseOrder::getId).collect(Collectors.toList());
        if (queryOrderId != null && orderIds.isEmpty()) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return orderIds;
    }

    /**
     * 获取所有有变更记录的订单 ID（仅非供应商用户使用）
     */
    private List<Long> getAllOrderChangeOrderIds() {
        List<OrderChange> changes = orderChangeMapper.selectList(
                new LambdaQueryWrapper<OrderChange>().select(OrderChange::getOrderId));
        return changes.stream().map(OrderChange::getOrderId).distinct().collect(Collectors.toList());
    }

    private OrderChangeVO toVO(OrderChange e) {
        OrderChangeVO vo = new OrderChangeVO();
        vo.setId(e.getId());
        vo.setOrderId(e.getOrderId());
        vo.setOrderDetailId(e.getOrderDetailId());
        vo.setChangeType(e.getChangeType());
        vo.setChangeContent(e.getChangeContent());
        vo.setBeforeValue(e.getBeforeValue());
        vo.setAfterValue(e.getAfterValue());
        vo.setChangeReason(e.getChangeReason());
        vo.setApproveStatus(e.getApproveStatus());
        vo.setApproveRemark(e.getApproveRemark());
        vo.setApplyTime(e.getApplyTime());
        vo.setApproveTime(e.getApproveTime());
        return vo;
    }
}
