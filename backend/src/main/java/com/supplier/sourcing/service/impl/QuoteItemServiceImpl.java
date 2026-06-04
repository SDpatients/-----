package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.sourcing.entity.QuoteItem;
import com.supplier.sourcing.entity.RfqItem;
import com.supplier.sourcing.mapper.QuoteItemMapper;
import com.supplier.sourcing.mapper.RfqItemMapper;
import com.supplier.sourcing.service.QuoteItemService;
import com.supplier.sourcing.vo.QuoteItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteItemServiceImpl implements QuoteItemService {

    private final QuoteItemMapper quoteItemMapper;
    private final RfqItemMapper rfqItemMapper;

    @Override
    public List<QuoteItemVO> getByQuoteId(Long quoteId) {
        List<QuoteItem> list = quoteItemMapper.selectList(
                new LambdaQueryWrapper<QuoteItem>()
                        .eq(QuoteItem::getQuoteId, quoteId)
        );
        
        // 获取所有需要的 rfqItemId
        List<Long> rfqItemIds = list.stream()
                .map(QuoteItem::getRfqItemId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询 rfqItem，构建 id -> RfqItem 的映射
        Map<Long, RfqItem> rfqItemMap = Map.of();
        if (!rfqItemIds.isEmpty()) {
            List<RfqItem> rfqItemList = rfqItemMapper.selectList(
                    new LambdaQueryWrapper<RfqItem>()
                            .in(RfqItem::getId, rfqItemIds)
            );
            rfqItemMap = rfqItemList.stream()
                    .collect(Collectors.toMap(RfqItem::getId, item -> item));
        }
        
        final Map<Long, RfqItem> finalRfqItemMap = rfqItemMap;
        return list.stream()
                .map(item -> toVO(item, finalRfqItemMap.get(item.getRfqItemId())))
                .collect(Collectors.toList());
    }

    private QuoteItemVO toVO(QuoteItem entity, RfqItem rfqItem) {
        QuoteItemVO vo = new QuoteItemVO();
        vo.setId(entity.getId());
        vo.setQuoteId(entity.getQuoteId());
        vo.setRfqItemId(entity.getRfqItemId());
        vo.setRfqLineId(entity.getRfqItemId()); // 兼容前端
        vo.setMaterialCode(entity.getMaterialCode());
        
        // 如果有 rfqItem，则从 rfqItem 获取信息，否则使用默认值
        if (rfqItem != null) {
            vo.setMaterialName(rfqItem.getMaterialName());
            vo.setSpec(rfqItem.getMaterialSpec());
            vo.setUnit(rfqItem.getUnit());
        } else {
            vo.setMaterialName(entity.getMaterialName());
            vo.setSpec(entity.getSpec());
            vo.setUnit(entity.getUnit());
        }
        
        vo.setQuantity(entity.getQuantity());
        vo.setPrice(entity.getPrice());
        vo.setUnitPrice(entity.getPrice()); // 兼容前端
        vo.setTaxPrice(entity.getTaxPrice());
        vo.setTaxRate(entity.getTaxRate());
        vo.setAmount(entity.getAmount());
        vo.setTotalPrice(entity.getAmount()); // 兼容前端
        vo.setTaxAmount(entity.getTaxAmount());
        vo.setDeliveryDate(entity.getDeliveryDate());
        vo.setPaymentTerms(entity.getPaymentTerms());
        vo.setDeliveryDays(entity.getDeliveryDays());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}