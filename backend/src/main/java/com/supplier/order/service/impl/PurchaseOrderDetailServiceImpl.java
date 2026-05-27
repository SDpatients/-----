package com.supplier.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.order.dto.PurchaseOrderDetailCreateDTO;
import com.supplier.order.dto.PurchaseOrderDetailUpdateDTO;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.entity.PurchaseOrderDetail;
import com.supplier.order.mapper.PurchaseOrderDetailMapper;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.order.query.PurchaseOrderDetailQuery;
import com.supplier.order.service.PurchaseOrderDetailService;
import com.supplier.order.vo.PurchaseOrderDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderDetailServiceImpl implements PurchaseOrderDetailService {

    private final PurchaseOrderDetailMapper detailMapper;
    private final PurchaseOrderMapper orderMapper;

    @Override
    public List<PurchaseOrderDetailVO> list(PurchaseOrderDetailQuery query) {
        return detailMapper.selectList(new LambdaQueryWrapper<PurchaseOrderDetail>()
                .eq(PurchaseOrderDetail::getOrderId, query.getOrderId())
                .orderByAsc(PurchaseOrderDetail::getLineNo))
                .stream().map(this::toVO).toList();
    }

    @Override
    public PurchaseOrderDetailVO getDetail(Long id) {
        return toVO(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PurchaseOrderDetailCreateDTO dto) {
        ensureOrderExists(dto.getOrderId());
        PurchaseOrderDetail entity = new PurchaseOrderDetail();
        entity.setOrderId(dto.getOrderId());
        entity.setLineNo(dto.getLineNo());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setMaterialSpec(dto.getMaterialSpec());
        entity.setMaterialModel(dto.getMaterialModel());
        entity.setUnit(dto.getUnit());
        entity.setQuantity(dto.getQuantity());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setTaxRate(dto.getTaxRate());
        entity.setTaxAmount(dto.getTaxAmount());
        entity.setAmount(dto.getAmount());
        entity.setDeliveryDate(dto.getDeliveryDate());
        entity.setRemark(dto.getRemark());
        detailMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PurchaseOrderDetailUpdateDTO dto) {
        PurchaseOrderDetail entity = getById(id);
        if (dto.getLineNo() != null) entity.setLineNo(dto.getLineNo());
        if (StringUtils.hasText(dto.getMaterialCode())) entity.setMaterialCode(dto.getMaterialCode());
        if (StringUtils.hasText(dto.getMaterialName())) entity.setMaterialName(dto.getMaterialName());
        if (StringUtils.hasText(dto.getMaterialSpec())) entity.setMaterialSpec(dto.getMaterialSpec());
        if (StringUtils.hasText(dto.getMaterialModel())) entity.setMaterialModel(dto.getMaterialModel());
        if (StringUtils.hasText(dto.getUnit())) entity.setUnit(dto.getUnit());
        if (dto.getQuantity() != null) entity.setQuantity(dto.getQuantity());
        if (dto.getUnitPrice() != null) entity.setUnitPrice(dto.getUnitPrice());
        if (dto.getTaxRate() != null) entity.setTaxRate(dto.getTaxRate());
        if (dto.getTaxAmount() != null) entity.setTaxAmount(dto.getTaxAmount());
        if (dto.getAmount() != null) entity.setAmount(dto.getAmount());
        if (dto.getDeliveryDate() != null) entity.setDeliveryDate(dto.getDeliveryDate());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        detailMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        detailMapper.deleteById(id);
    }

    private PurchaseOrderDetail getById(Long id) {
        PurchaseOrderDetail detail = detailMapper.selectById(id);
        if (detail == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return detail;
    }

    private void ensureOrderExists(Long orderId) {
        if (orderMapper.selectById(orderId) == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
    }

    private PurchaseOrderDetailVO toVO(PurchaseOrderDetail e) {
        PurchaseOrderDetailVO vo = new PurchaseOrderDetailVO();
        vo.setId(e.getId());
        vo.setOrderId(e.getOrderId());
        vo.setLineNo(e.getLineNo());
        vo.setMaterialCode(e.getMaterialCode());
        vo.setMaterialName(e.getMaterialName());
        vo.setMaterialSpec(e.getMaterialSpec());
        vo.setMaterialModel(e.getMaterialModel());
        vo.setUnit(e.getUnit());
        vo.setQuantity(e.getQuantity());
        vo.setUnitPrice(e.getUnitPrice());
        vo.setTaxRate(e.getTaxRate());
        vo.setTaxAmount(e.getTaxAmount());
        vo.setAmount(e.getAmount());
        vo.setDeliveredQty(e.getDeliveredQty());
        vo.setReceivedQty(e.getReceivedQty());
        vo.setQualifiedQty(e.getQualifiedQty());
        vo.setDeliveryDate(e.getDeliveryDate());
        vo.setRemark(e.getRemark());
        return vo;
    }
}
