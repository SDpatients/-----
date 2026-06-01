package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.sourcing.converter.RfqItemConverter;
import com.supplier.sourcing.dto.RfqItemCreateDTO;
import com.supplier.sourcing.dto.RfqItemUpdateDTO;
import com.supplier.sourcing.entity.RfqItem;
import com.supplier.sourcing.mapper.RfqItemMapper;
import com.supplier.sourcing.query.RfqItemQuery;
import com.supplier.sourcing.service.RfqItemService;
import com.supplier.sourcing.vo.RfqItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RfqItemServiceImpl implements RfqItemService {

    private final RfqItemMapper rfqItemMapper;

    @Override
    public PageResult<RfqItemVO> page(RfqItemQuery query) {
        LambdaQueryWrapper<RfqItem> wrapper = new LambdaQueryWrapper<RfqItem>()
                .eq(query.getRfqId() != null, RfqItem::getRfqId, query.getRfqId())
                .orderByAsc(RfqItem::getLineNo);
        Page<RfqItem> page = rfqItemMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(RfqItemConverter::toVO));
    }

    @Override
    public RfqItemVO getDetail(Long id) {
        RfqItem entity = rfqItemMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return RfqItemConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RfqItemCreateDTO dto) {
        RfqItem entity = RfqItemConverter.toEntity(dto);
        rfqItemMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RfqItemUpdateDTO dto) {
        RfqItem entity = rfqItemMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        RfqItemConverter.updateEntity(entity, dto);
        rfqItemMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        RfqItem entity = rfqItemMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        rfqItemMapper.deleteById(entity.getId());
    }

    @Override
    public List<RfqItemVO> getByRfqId(Long rfqId) {
        List<RfqItem> list = rfqItemMapper.selectList(
                new LambdaQueryWrapper<RfqItem>()
                        .eq(RfqItem::getRfqId, rfqId)
                        .orderByAsc(RfqItem::getLineNo)
        );
        return list.stream().map(RfqItemConverter::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLines(Long rfqId, List<RfqItemUpdateDTO> lines) {
        rfqItemMapper.delete(new LambdaQueryWrapper<RfqItem>().eq(RfqItem::getRfqId, rfqId));
        if (lines != null && !lines.isEmpty()) {
            for (int i = 0; i < lines.size(); i++) {
                RfqItemUpdateDTO dto = lines.get(i);
                RfqItem entity = new RfqItem();
                entity.setRfqId(rfqId);
                entity.setLineNo(dto.getLineNo() != null ? dto.getLineNo() : i + 1);
                entity.setMaterialCode(dto.getMaterialCode());
                entity.setMaterialName(dto.getMaterialName());
                entity.setMaterialSpec(dto.getMaterialSpec());
                entity.setUnit(dto.getUnit());
                entity.setQuantity(dto.getQuantity());
                entity.setTargetDeliveryDate(dto.getTargetDeliveryDate());
                entity.setRemark(dto.getRemark());
                rfqItemMapper.insert(entity);
            }
        }
    }
}