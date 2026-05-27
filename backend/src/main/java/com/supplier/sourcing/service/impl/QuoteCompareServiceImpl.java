package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.settlement.entity.SupplierPerformance;
import com.supplier.settlement.mapper.SupplierPerformanceMapper;
import com.supplier.sourcing.entity.Quote;
import com.supplier.sourcing.entity.QuoteItem;
import com.supplier.sourcing.entity.Rfq;
import com.supplier.sourcing.entity.RfqItem;
import com.supplier.sourcing.enums.QuoteStatusEnum;
import com.supplier.sourcing.mapper.QuoteItemMapper;
import com.supplier.sourcing.mapper.QuoteMapper;
import com.supplier.sourcing.mapper.RfqItemMapper;
import com.supplier.sourcing.mapper.RfqMapper;
import com.supplier.sourcing.service.QuoteCompareService;
import com.supplier.sourcing.vo.QuoteCompareVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteCompareServiceImpl implements QuoteCompareService {

    private final RfqMapper rfqMapper;
    private final RfqItemMapper rfqItemMapper;
    private final QuoteMapper quoteMapper;
    private final QuoteItemMapper quoteItemMapper;
    private final SupplierPerformanceMapper supplierPerformanceMapper;

    @Override
    public QuoteCompareVO compare(Long rfqId) {
        Rfq rfq = rfqMapper.selectById(rfqId);
        if (rfq == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }

        // RFQ物料明细
        List<RfqItem> rfqItems = rfqItemMapper.selectList(
                new LambdaQueryWrapper<RfqItem>()
                        .eq(RfqItem::getRfqId, rfqId)
                        .orderByAsc(RfqItem::getLineNo)
        );

        // 已提交报价
        List<Quote> quotes = quoteMapper.selectList(
                new LambdaQueryWrapper<Quote>()
                        .eq(Quote::getRfqId, rfqId)
                        .eq(Quote::getQuoteStatus, QuoteStatusEnum.SUBMITTED.getCode())
        );

        // 报价物料明细按报价ID分组
        Map<Long, List<QuoteItem>> quoteItemMap = Map.of();
        if (!quotes.isEmpty()) {
            List<Long> quoteIds = quotes.stream().map(Quote::getId).collect(Collectors.toList());
            List<QuoteItem> allQuoteItems = quoteItemMapper.selectList(
                    new LambdaQueryWrapper<QuoteItem>()
                            .in(QuoteItem::getQuoteId, quoteIds)
            );
            quoteItemMap = allQuoteItems.stream().collect(Collectors.groupingBy(QuoteItem::getQuoteId));
        }

        // 供应商绩效
        Map<Long, BigDecimal> performanceMap = Map.of();
        if (!quotes.isEmpty()) {
            List<Long> supplierIds = quotes.stream().map(Quote::getSupplierId).distinct().collect(Collectors.toList());
            List<SupplierPerformance> performances = supplierPerformanceMapper.selectList(
                    new LambdaQueryWrapper<SupplierPerformance>()
                            .in(SupplierPerformance::getSupplierId, supplierIds)
                            .orderByDesc(SupplierPerformance::getCreateTime)
            );
            performanceMap = performances.stream()
                    .collect(Collectors.toMap(SupplierPerformance::getSupplierId,
                            p -> p.getTotalScore() != null ? p.getTotalScore() : BigDecimal.ZERO,
                            (a, b) -> a));
        }

        // 构建比价结果
        QuoteCompareVO result = new QuoteCompareVO();
        result.setRfqId(rfq.getId());
        result.setRfqNo(rfq.getRfqNo());
        result.setRfqTitle(rfq.getRfqTitle());

        List<QuoteCompareVO.MaterialCompare> materialCompares = new ArrayList<>();
        for (RfqItem rfqItem : rfqItems) {
            QuoteCompareVO.MaterialCompare mc = new QuoteCompareVO.MaterialCompare();
            mc.setRfqItemId(rfqItem.getId());
            mc.setMaterialCode(rfqItem.getMaterialCode());
            mc.setMaterialName(rfqItem.getMaterialName());
            mc.setMaterialSpec(rfqItem.getMaterialSpec());
            mc.setQuantity(rfqItem.getQuantity());

            List<QuoteCompareVO.SupplierQuoteInfo> supplierInfos = new ArrayList<>();
            for (Quote quote : quotes) {
                List<QuoteItem> qItems = quoteItemMap.getOrDefault(quote.getId(), List.of());
                QuoteItem matched = qItems.stream()
                        .filter(qi -> rfqItem.getId().equals(qi.getRfqItemId()))
                        .findFirst().orElse(null);

                QuoteCompareVO.SupplierQuoteInfo info = new QuoteCompareVO.SupplierQuoteInfo();
                info.setQuoteId(quote.getId());
                info.setQuoteNo(quote.getQuoteNo());
                info.setSupplierId(quote.getSupplierId());
                info.setSupplierPerformanceScore(
                        performanceMap.getOrDefault(quote.getSupplierId(), BigDecimal.ZERO));
                if (matched != null) {
                    info.setPrice(matched.getPrice());
                    info.setTaxPrice(matched.getTaxPrice());
                    info.setTaxRate(matched.getTaxRate());
                    info.setAmount(matched.getAmount());
                    info.setTaxAmount(matched.getTaxAmount());
                    info.setDeliveryDays(matched.getDeliveryDays());
                }
                supplierInfos.add(info);
            }
            mc.setSupplierQuotes(supplierInfos);
            materialCompares.add(mc);
        }
        result.setMaterialCompares(materialCompares);
        return result;
    }
}