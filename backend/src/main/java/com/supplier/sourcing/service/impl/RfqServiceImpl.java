package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.RandomUtil;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.service.PurchaseOrderService;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import com.supplier.sourcing.converter.RfqConverter;
import com.supplier.sourcing.dto.PricingDTO;
import com.supplier.sourcing.dto.RfqCreateDTO;
import com.supplier.sourcing.dto.RfqUpdateDTO;
import com.supplier.sourcing.entity.Quote;
import com.supplier.sourcing.entity.QuoteAward;
import com.supplier.sourcing.entity.Rfq;
import com.supplier.sourcing.entity.RfqItem;
import com.supplier.sourcing.enums.QuoteStatusEnum;
import com.supplier.sourcing.enums.RfqStatusEnum;
import com.supplier.sourcing.mapper.QuoteAwardMapper;
import com.supplier.sourcing.mapper.QuoteMapper;
import com.supplier.sourcing.mapper.RfqItemMapper;
import com.supplier.sourcing.mapper.RfqMapper;
import com.supplier.sourcing.query.RfqQuery;
import com.supplier.sourcing.service.RfqService;
import com.supplier.sourcing.vo.RfqVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RfqServiceImpl implements RfqService {

    private final RfqMapper rfqMapper;
    private final RfqItemMapper rfqItemMapper;
    private final QuoteMapper quoteMapper;
    private final QuoteAwardMapper quoteAwardMapper;
    private final PurchaseOrderService purchaseOrderService;

    @Override
    public PageResult<RfqVO> page(RfqQuery query) {
        LambdaQueryWrapper<Rfq> wrapper = new LambdaQueryWrapper<Rfq>()
                .eq(query.getRfqStatus() != null, Rfq::getRfqStatus, query.getRfqStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Rfq::getRfqNo, query.getKeyword())
                        .or()
                        .like(Rfq::getRfqTitle, query.getKeyword()))
                .orderByDesc(Rfq::getCreateTime);
        Page<Rfq> page = rfqMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(RfqConverter::toVO));
    }

    @Override
    public RfqVO getDetail(Long id) {
        Rfq entity = rfqMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return RfqConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RfqCreateDTO dto) {
        String rfqNo = dto.getRfqNo();
        if (!StringUtils.hasText(rfqNo)) {
            rfqNo = generateRfqNo();
            dto.setRfqNo(rfqNo);
        }
        Long count = rfqMapper.selectCount(new LambdaQueryWrapper<Rfq>()
                .eq(Rfq::getRfqNo, dto.getRfqNo()));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "询价单号已存在");
        }
        Rfq entity = RfqConverter.toEntity(dto);
        rfqMapper.insert(entity);
        Long rfqId = entity.getId();
        saveLines(rfqId, dto.getLines());
        return rfqId;
    }

    private void saveLines(Long rfqId, List<RfqCreateDTO.RfqLineDTO> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        for (int i = 0; i < lines.size(); i++) {
            RfqCreateDTO.RfqLineDTO dto = lines.get(i);
            RfqItem item = new RfqItem();
            item.setRfqId(rfqId);
            item.setLineNo(dto.getLineNo() != null ? dto.getLineNo() : i + 1);
            item.setMaterialCode(dto.getMaterialCode());
            item.setMaterialName(dto.getMaterialName());
            item.setMaterialSpec(dto.getSpec());
            item.setUnit(dto.getUnit());
            item.setQuantity(dto.getQuantity());
            item.setTargetDeliveryDate(dto.getDeliveryDate());
            item.setRemark(dto.getRemark());
            rfqItemMapper.insert(item);
        }
    }

    private String generateRfqNo() {
        String no;
        do {
            no = "RFQ" + RandomUtil.randomNumbers(8);
        } while (rfqMapper.selectCount(
                new LambdaQueryWrapper<Rfq>().eq(Rfq::getRfqNo, no)) > 0);
        return no;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RfqUpdateDTO dto) {
        Rfq entity = rfqMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!RfqStatusEnum.DRAFT.getCode().equals(entity.getRfqStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿状态可编辑");
        }
        RfqConverter.updateEntity(entity, dto);
        rfqMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        Rfq entity = rfqMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!RfqStatusEnum.DRAFT.getCode().equals(entity.getRfqStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿状态可发布");
        }
        entity.setRfqStatus(RfqStatusEnum.PUBLISHED.getCode());
        entity.setPublishTime(LocalDateTime.now());
        rfqMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id) {
        Rfq entity = rfqMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!RfqStatusEnum.QUOTING.getCode().equals(entity.getRfqStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有报价中状态可截止");
        }
        entity.setRfqStatus(RfqStatusEnum.CLOSED.getCode());
        entity.setCloseTime(LocalDateTime.now());
        rfqMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Rfq entity = rfqMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (RfqStatusEnum.CLOSED.getCode().equals(entity.getRfqStatus())
                || RfqStatusEnum.PRICED.getCode().equals(entity.getRfqStatus())
                || RfqStatusEnum.CANCELED.getCode().equals(entity.getRfqStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前状态不允许取消");
        }
        entity.setRfqStatus(RfqStatusEnum.CANCELED.getCode());
        rfqMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void price(Long id, PricingDTO dto) {
        Rfq rfq = rfqMapper.selectById(id);
        if (rfq == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!RfqStatusEnum.CLOSED.getCode().equals(rfq.getRfqStatus())
                && !RfqStatusEnum.QUOTING.getCode().equals(rfq.getRfqStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "当前状态不允许定价");
        }

        Quote quote = quoteMapper.selectById(dto.getQuoteId());
        if (quote == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "报价单不存在");
        }
        if (!quote.getRfqId().equals(id)) {
            throw BusinessException.of(ResultCode.PARAM_ERROR.getCode(), "报价单不属于该询价单");
        }
        if (!QuoteStatusEnum.SUBMITTED.getCode().equals(quote.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交状态可定价");
        }

        // 1. 创建定价记录
        LoginUser loginUser = SecurityUtils.getLoginUser();
        QuoteAward award = new QuoteAward();
        award.setRfqId(rfq.getId());
        award.setQuoteId(quote.getId());
        award.setSupplierId(quote.getSupplierId());
        award.setAwardAmount(quote.getTotalAmount());
        award.setAwardTaxAmount(quote.getTaxAmount());
        award.setAwardCurrency(quote.getCurrency());
        award.setAwardBy(loginUser != null ? loginUser.getUserId() : null);
        award.setAwardByName(loginUser != null ? loginUser.getRealName() : null);
        award.setAwardTime(LocalDateTime.now());
        award.setRemark(dto.getRemark());
        quoteAwardMapper.insert(award);

        // 2. 更新报价状态为已定价
        quote.setQuoteStatus(QuoteStatusEnum.PRICED.getCode());
        quoteMapper.updateById(quote);

        // 3. 更新询价单状态为已定价
        rfq.setRfqStatus(RfqStatusEnum.PRICED.getCode());
        rfqMapper.updateById(rfq);

        // 4. 自动生成采购订单
        PurchaseOrderCreateDTO orderDTO = new PurchaseOrderCreateDTO();
        orderDTO.setOrderNo("PO" + System.currentTimeMillis());
        orderDTO.setSupplierId(quote.getSupplierId());
        orderDTO.setOrderDate(LocalDate.now());
        orderDTO.setCurrency(quote.getCurrency() != null ? quote.getCurrency() : rfq.getCurrency());
        orderDTO.setTotalAmount(quote.getTotalAmount() != null ? quote.getTotalAmount() : BigDecimal.ZERO);
        orderDTO.setTaxAmount(quote.getTaxAmount() != null ? quote.getTaxAmount() : BigDecimal.ZERO);
        orderDTO.setPayAmount(quote.getTotalAmount() != null ? quote.getTotalAmount() : BigDecimal.ZERO);
        orderDTO.setPaymentTerms(quote.getPaymentTerms());
        orderDTO.setRemark("由询价单" + rfq.getRfqNo() + "定价自动生成");
        Long orderId = purchaseOrderService.create(orderDTO);

        // 5. 回写订单号到定价记录
        award.setOrderId(orderId);
        award.setOrderNo(orderDTO.getOrderNo());
        quoteAwardMapper.updateById(award);
    }
}