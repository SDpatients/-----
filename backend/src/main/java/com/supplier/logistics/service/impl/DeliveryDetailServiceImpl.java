package com.supplier.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.logistics.dto.DeliveryDetailCreateDTO;
import com.supplier.logistics.dto.DeliveryDetailUpdateDTO;
import com.supplier.logistics.entity.DeliveryDetail;
import com.supplier.logistics.mapper.DeliveryDetailMapper;
import com.supplier.logistics.mapper.DeliveryNoticeMapper;
import com.supplier.logistics.query.DeliveryDetailQuery;
import com.supplier.logistics.service.DeliveryDetailService;
import com.supplier.logistics.vo.DeliveryDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryDetailServiceImpl implements DeliveryDetailService {

    private final DeliveryDetailMapper deliveryDetailMapper;
    private final DeliveryNoticeMapper deliveryNoticeMapper;

    @Override
    public List<DeliveryDetailVO> list(DeliveryDetailQuery query) {
        return deliveryDetailMapper.selectList(new LambdaQueryWrapper<DeliveryDetail>()
                .eq(DeliveryDetail::getNoticeId, query.getNoticeId())
                .orderByAsc(DeliveryDetail::getId))
                .stream().map(this::toVO).toList();
    }

    @Override
    public DeliveryDetailVO getDetail(Long id) {
        return toVO(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeliveryDetailCreateDTO dto) {
        ensureNoticeExists(dto.getNoticeId());
        DeliveryDetail entity = new DeliveryDetail();
        entity.setNoticeId(dto.getNoticeId());
        entity.setOrderDetailId(dto.getOrderDetailId());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setMaterialSpec(dto.getMaterialSpec());
        entity.setUnit(dto.getUnit());
        entity.setPlanQty(dto.getPlanQty());
        entity.setActualQty(dto.getActualQty());
        entity.setReceivedQty(dto.getReceivedQty());
        entity.setQualifiedQty(dto.getQualifiedQty());
        entity.setBatchNo(dto.getBatchNo());
        entity.setProductionDate(dto.getProductionDate());
        entity.setExpiryDate(dto.getExpiryDate());
        entity.setRemark(dto.getRemark());
        deliveryDetailMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DeliveryDetailUpdateDTO dto) {
        DeliveryDetail entity = getById(id);
        if (dto.getOrderDetailId() != null) entity.setOrderDetailId(dto.getOrderDetailId());
        if (StringUtils.hasText(dto.getMaterialCode())) entity.setMaterialCode(dto.getMaterialCode());
        if (StringUtils.hasText(dto.getMaterialName())) entity.setMaterialName(dto.getMaterialName());
        if (StringUtils.hasText(dto.getMaterialSpec())) entity.setMaterialSpec(dto.getMaterialSpec());
        if (StringUtils.hasText(dto.getUnit())) entity.setUnit(dto.getUnit());
        if (dto.getPlanQty() != null) entity.setPlanQty(dto.getPlanQty());
        if (dto.getActualQty() != null) entity.setActualQty(dto.getActualQty());
        if (dto.getReceivedQty() != null) entity.setReceivedQty(dto.getReceivedQty());
        if (dto.getQualifiedQty() != null) entity.setQualifiedQty(dto.getQualifiedQty());
        if (dto.getBatchNo() != null) entity.setBatchNo(dto.getBatchNo());
        if (dto.getProductionDate() != null) entity.setProductionDate(dto.getProductionDate());
        if (dto.getExpiryDate() != null) entity.setExpiryDate(dto.getExpiryDate());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        deliveryDetailMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        deliveryDetailMapper.deleteById(id);
    }

    private DeliveryDetail getById(Long id) {
        DeliveryDetail detail = deliveryDetailMapper.selectById(id);
        if (detail == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return detail;
    }

    private void ensureNoticeExists(Long noticeId) {
        if (deliveryNoticeMapper.selectById(noticeId) == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "送货通知不存在");
        }
    }

    private DeliveryDetailVO toVO(DeliveryDetail e) {
        DeliveryDetailVO vo = new DeliveryDetailVO();
        vo.setId(e.getId()); vo.setNoticeId(e.getNoticeId()); vo.setOrderDetailId(e.getOrderDetailId()); vo.setMaterialCode(e.getMaterialCode()); vo.setMaterialName(e.getMaterialName()); vo.setMaterialSpec(e.getMaterialSpec()); vo.setUnit(e.getUnit()); vo.setPlanQty(e.getPlanQty()); vo.setActualQty(e.getActualQty()); vo.setReceivedQty(e.getReceivedQty()); vo.setQualifiedQty(e.getQualifiedQty()); vo.setBatchNo(e.getBatchNo()); vo.setProductionDate(e.getProductionDate()); vo.setExpiryDate(e.getExpiryDate()); vo.setRemark(e.getRemark());
        return vo;
    }
}
