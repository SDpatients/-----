package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.sourcing.entity.QuoteItem;
import com.supplier.sourcing.mapper.QuoteItemMapper;
import com.supplier.sourcing.service.QuoteItemService;
import com.supplier.sourcing.vo.QuoteItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteItemServiceImpl implements QuoteItemService {

    private final QuoteItemMapper quoteItemMapper;

    @Override
    public List<QuoteItemVO> getByQuoteId(Long quoteId) {
        List<QuoteItem> list = quoteItemMapper.selectList(
                new LambdaQueryWrapper<QuoteItem>()
                        .eq(QuoteItem::getQuoteId, quoteId)
        );
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private QuoteItemVO toVO(QuoteItem entity) {
        QuoteItemVO vo = new QuoteItemVO();
        vo.setId(entity.getId());
        vo.setQuoteId(entity.getQuoteId());
        vo.setRfqItemId(entity.getRfqItemId());
        vo.setMaterialCode(entity.getMaterialCode());
        vo.setQuantity(entity.getQuantity());
        vo.setPrice(entity.getPrice());
        vo.setTaxPrice(entity.getTaxPrice());
        vo.setTaxRate(entity.getTaxRate());
        vo.setAmount(entity.getAmount());
        vo.setTaxAmount(entity.getTaxAmount());
        vo.setDeliveryDays(entity.getDeliveryDays());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}