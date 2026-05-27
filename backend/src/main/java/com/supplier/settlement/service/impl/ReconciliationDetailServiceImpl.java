package com.supplier.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.settlement.dto.ReconciliationDetailCreateDTO;
import com.supplier.settlement.dto.ReconciliationDetailUpdateDTO;
import com.supplier.settlement.entity.Reconciliation;
import com.supplier.settlement.entity.ReconciliationDetail;
import com.supplier.settlement.mapper.ReconciliationDetailMapper;
import com.supplier.settlement.mapper.ReconciliationMapper;
import com.supplier.settlement.query.ReconciliationDetailQuery;
import com.supplier.settlement.service.ReconciliationDetailService;
import com.supplier.settlement.vo.ReconciliationDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReconciliationDetailServiceImpl implements ReconciliationDetailService {

    private final ReconciliationDetailMapper detailMapper;
    private final ReconciliationMapper reconciliationMapper;

    @Override
    public List<ReconciliationDetailVO> list(ReconciliationDetailQuery query) {
        return detailMapper.selectList(new LambdaQueryWrapper<ReconciliationDetail>()
                .eq(ReconciliationDetail::getReconId, query.getReconId())
                .orderByAsc(ReconciliationDetail::getId))
                .stream().map(this::toVO).toList();
    }

    @Override
    public ReconciliationDetailVO getDetail(Long id) {
        return toVO(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ReconciliationDetailCreateDTO dto) {
        ensureReconExists(dto.getReconId());
        ReconciliationDetail entity = new ReconciliationDetail();
        entity.setReconId(dto.getReconId());
        entity.setOrderId(dto.getOrderId());
        entity.setOrderNo(dto.getOrderNo());
        entity.setDeliveryId(dto.getDeliveryId());
        entity.setDeliveryNo(dto.getDeliveryNo());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setQuantity(dto.getQuantity());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setOrderAmount(dto.getOrderAmount());
        entity.setConfirmedAmount(dto.getConfirmedAmount());
        entity.setDiffAmount(dto.getDiffAmount());
        entity.setDiffReason(dto.getDiffReason());
        entity.setConfirmStatus(dto.getConfirmStatus());
        entity.setConfirmRemark(dto.getConfirmRemark());
        entity.setRemark(dto.getRemark());
        detailMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ReconciliationDetailUpdateDTO dto) {
        ReconciliationDetail entity = getById(id);
        if (dto.getOrderId() != null) entity.setOrderId(dto.getOrderId());
        if (dto.getOrderNo() != null) entity.setOrderNo(dto.getOrderNo());
        if (dto.getDeliveryId() != null) entity.setDeliveryId(dto.getDeliveryId());
        if (dto.getDeliveryNo() != null) entity.setDeliveryNo(dto.getDeliveryNo());
        if (StringUtils.hasText(dto.getMaterialCode())) entity.setMaterialCode(dto.getMaterialCode());
        if (StringUtils.hasText(dto.getMaterialName())) entity.setMaterialName(dto.getMaterialName());
        if (dto.getQuantity() != null) entity.setQuantity(dto.getQuantity());
        if (dto.getUnitPrice() != null) entity.setUnitPrice(dto.getUnitPrice());
        if (dto.getOrderAmount() != null) entity.setOrderAmount(dto.getOrderAmount());
        if (dto.getConfirmedAmount() != null) entity.setConfirmedAmount(dto.getConfirmedAmount());
        if (dto.getDiffAmount() != null) entity.setDiffAmount(dto.getDiffAmount());
        if (dto.getDiffReason() != null) entity.setDiffReason(dto.getDiffReason());
        if (dto.getConfirmStatus() != null) entity.setConfirmStatus(dto.getConfirmStatus());
        if (dto.getConfirmRemark() != null) entity.setConfirmRemark(dto.getConfirmRemark());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        detailMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        detailMapper.deleteById(id);
    }

    private ReconciliationDetail getById(Long id) {
        ReconciliationDetail entity = detailMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return entity;
    }

    private void ensureReconExists(Long reconId) {
        Reconciliation reconciliation = reconciliationMapper.selectById(reconId);
        if (reconciliation == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "对账单不存在");
        }
    }

    private ReconciliationDetailVO toVO(ReconciliationDetail e) {
        ReconciliationDetailVO vo = new ReconciliationDetailVO();
        vo.setId(e.getId()); vo.setReconId(e.getReconId()); vo.setOrderId(e.getOrderId()); vo.setOrderNo(e.getOrderNo()); vo.setDeliveryId(e.getDeliveryId()); vo.setDeliveryNo(e.getDeliveryNo()); vo.setMaterialCode(e.getMaterialCode()); vo.setMaterialName(e.getMaterialName()); vo.setQuantity(e.getQuantity()); vo.setUnitPrice(e.getUnitPrice()); vo.setOrderAmount(e.getOrderAmount()); vo.setConfirmedAmount(e.getConfirmedAmount()); vo.setDiffAmount(e.getDiffAmount()); vo.setDiffReason(e.getDiffReason()); vo.setConfirmStatus(e.getConfirmStatus()); vo.setConfirmTime(e.getConfirmTime()); vo.setConfirmRemark(e.getConfirmRemark()); vo.setRemark(e.getRemark());
        return vo;
    }
}
