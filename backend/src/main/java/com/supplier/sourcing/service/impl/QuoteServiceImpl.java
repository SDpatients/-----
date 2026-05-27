package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.service.PurchaseOrderService;
import com.supplier.security.model.LoginUser;
import com.supplier.sourcing.converter.QuoteConverter;
import com.supplier.sourcing.dto.BargainDTO;
import com.supplier.sourcing.dto.QuoteCreateDTO;
import com.supplier.sourcing.dto.QuotePriceDTO;
import com.supplier.sourcing.entity.Quote;
import com.supplier.sourcing.entity.QuoteAward;
import com.supplier.sourcing.entity.QuoteNegotiation;
import com.supplier.sourcing.entity.Rfq;
import com.supplier.sourcing.enums.QuoteStatusEnum;
import com.supplier.sourcing.enums.RfqStatusEnum;
import com.supplier.sourcing.mapper.QuoteAwardMapper;
import com.supplier.sourcing.mapper.QuoteMapper;
import com.supplier.sourcing.mapper.QuoteNegotiationMapper;
import com.supplier.sourcing.mapper.RfqMapper;
import com.supplier.sourcing.query.QuoteQuery;
import com.supplier.sourcing.service.ExchangeRateService;
import com.supplier.sourcing.service.QuoteService;
import com.supplier.sourcing.vo.QuoteNegotiationVO;
import com.supplier.sourcing.vo.QuoteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteMapper quoteMapper;
    private final RfqMapper rfqMapper;
    private final QuoteNegotiationMapper quoteNegotiationMapper;
    private final ExchangeRateService exchangeRateService;
    private final PurchaseOrderService purchaseOrderService;
    private final QuoteAwardMapper quoteAwardMapper;

    @Override
    public PageResult<QuoteVO> page(QuoteQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<Quote> wrapper = new LambdaQueryWrapper<Quote>()
                .eq(query.getRfqId() != null, Quote::getRfqId, query.getRfqId())
                .eq(supplierId != null, Quote::getSupplierId, supplierId)
                .eq(query.getQuoteStatus() != null, Quote::getQuoteStatus, query.getQuoteStatus())
                .like(StringUtils.hasText(query.getKeyword()), Quote::getQuoteNo, query.getKeyword())
                .orderByDesc(Quote::getCreateTime);
        Page<Quote> page = quoteMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(QuoteConverter::toVO));
    }

    @Override
    public QuoteVO getDetail(Long id) {
        Quote entity = getWithScope(id);
        return QuoteConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(QuoteCreateDTO dto) {
        Long count = quoteMapper.selectCount(new LambdaQueryWrapper<Quote>()
                .eq(Quote::getQuoteNo, dto.getQuoteNo()));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "报价单号已存在");
        }
        // 报价时锁定汇率：如果币种不是CNY且未指定汇率，则从汇率表获取
        if (dto.getExchangeRate() == null && dto.getCurrency() != null && !"CNY".equals(dto.getCurrency())) {
            BigDecimal rate = exchangeRateService.getRate(dto.getCurrency(), "CNY");
            dto.setExchangeRate(rate);
        }
        Quote entity = QuoteConverter.toEntity(dto);
        quoteMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        Quote entity = getWithScope(id);
        if (!QuoteStatusEnum.DRAFT.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿状态可提交");
        }
        // 校验询价单未截止
        checkRfqNotClosed(entity.getRfqId());
        entity.setQuoteStatus(QuoteStatusEnum.SUBMITTED.getCode());
        entity.setSubmitTime(LocalDateTime.now());
        quoteMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long id) {
        Quote entity = getWithScope(id);
        if (!QuoteStatusEnum.SUBMITTED.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交状态可撤回");
        }
        // 校验询价单未截止
        checkRfqNotClosed(entity.getRfqId());
        entity.setQuoteStatus(QuoteStatusEnum.WITHDRAWN.getCode());
        quoteMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(Long id) {
        Quote entity = quoteMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!QuoteStatusEnum.SUBMITTED.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交状态可采纳");
        }
        entity.setQuoteStatus(QuoteStatusEnum.ACCEPTED.getCode());
        quoteMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id) {
        Quote entity = quoteMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!QuoteStatusEnum.SUBMITTED.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交状态可拒绝");
        }
        entity.setQuoteStatus(QuoteStatusEnum.REJECTED.getCode());
        quoteMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Quote entity = getWithScope(id);
        if (!QuoteStatusEnum.DRAFT.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿状态可删除");
        }
        quoteMapper.deleteById(entity.getId());
    }

    private Quote getWithScope(Long id) {
        Quote entity = quoteMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && !entity.getSupplierId().equals(SecurityUtils.getSupplierId())) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return entity;
    }

    private Long resolveSupplierId(Long querySupplierId) {
        if (!SecurityUtils.isSupplierUser()) {
            return querySupplierId;
        }
        Long supplierId = SecurityUtils.getSupplierId();
        if (supplierId == null) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN.getCode(), "供应商用户未绑定供应商");
        }
        if (querySupplierId != null && !supplierId.equals(querySupplierId)) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return supplierId;
    }

    private void checkRfqNotClosed(Long rfqId) {
        if (rfqId == null) {
            return;
        }
        Rfq rfq = rfqMapper.selectById(rfqId);
        if (rfq != null && RfqStatusEnum.CLOSED.getCode().equals(rfq.getRfqStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "询价单已截止，不允许操作报价");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bargain(Long id, BargainDTO dto) {
        Quote entity = quoteMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!QuoteStatusEnum.SUBMITTED.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交状态可发起议价");
        }
        checkRfqNotClosed(entity.getRfqId());

        int nextRound = (entity.getNegotiationRound() != null ? entity.getNegotiationRound() : 0) + 1;
        entity.setNegotiationRound(nextRound);
        entity.setQuoteStatus(QuoteStatusEnum.DRAFT.getCode());
        quoteMapper.updateById(entity);

        QuoteNegotiation negotiation = new QuoteNegotiation();
        negotiation.setQuoteId(entity.getId());
        negotiation.setRfqId(entity.getRfqId());
        negotiation.setSupplierId(entity.getSupplierId());
        negotiation.setRound(nextRound);
        negotiation.setInitiator("BUYER");
        negotiation.setTargetPrice(dto.getTargetPrice());
        negotiation.setBuyerRemark(dto.getBuyerRemark());
        negotiation.setNegotiationTime(LocalDateTime.now());
        quoteNegotiationMapper.insert(negotiation);
    }

    @Override
    public List<QuoteNegotiationVO> getNegotiations(Long quoteId) {
        List<QuoteNegotiation> list = quoteNegotiationMapper.selectList(
                new LambdaQueryWrapper<QuoteNegotiation>()
                        .eq(QuoteNegotiation::getQuoteId, quoteId)
                        .orderByAsc(QuoteNegotiation::getRound)
        );
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private QuoteNegotiationVO toVO(QuoteNegotiation entity) {
        QuoteNegotiationVO vo = new QuoteNegotiationVO();
        vo.setId(entity.getId());
        vo.setQuoteId(entity.getQuoteId());
        vo.setRfqId(entity.getRfqId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setRound(entity.getRound());
        vo.setInitiator(entity.getInitiator());
        vo.setTargetPrice(entity.getTargetPrice());
        vo.setSupplierPrice(entity.getSupplierPrice());
        vo.setBuyerRemark(entity.getBuyerRemark());
        vo.setSupplierRemark(entity.getSupplierRemark());
        vo.setNegotiationTime(entity.getNegotiationTime());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    // ---- 联动1: RFQ 定价 → 自动生成采购订单 ----
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void price(Long id, QuotePriceDTO dto) {
        Quote entity = quoteMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!QuoteStatusEnum.ACCEPTED.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已采纳的报价可以进行定价");
        }
        checkRfqNotClosed(entity.getRfqId());

        // 1) 更新报价状态为已定价
        entity.setQuoteStatus(QuoteStatusEnum.PRICED.getCode());
        quoteMapper.updateById(entity);

        // 2) 创建 QuoteAward 定价记录
        QuoteAward award = new QuoteAward();
        award.setRfqId(entity.getRfqId());
        award.setQuoteId(entity.getId());
        award.setSupplierId(entity.getSupplierId());
        award.setAwardAmount(dto.getAwardAmount());
        award.setAwardTaxAmount(dto.getAwardTaxAmount() != null ? dto.getAwardTaxAmount() : BigDecimal.ZERO);
        award.setAwardCurrency(dto.getAwardCurrency() != null ? dto.getAwardCurrency() : entity.getCurrency());
        LoginUser loginUser = SecurityUtils.getLoginUser();
        award.setAwardBy(loginUser != null ? loginUser.getUserId() : null);
        award.setAwardByName(loginUser != null ? loginUser.getRealName() : null);
        award.setAwardTime(LocalDateTime.now());
        award.setRemark(dto.getRemark());

        // 3) 自动生成采购订单
        PurchaseOrderCreateDTO orderDto = new PurchaseOrderCreateDTO();
        String orderNo = generateOrderNo();
        orderDto.setOrderNo(orderNo);
        orderDto.setSupplierId(entity.getSupplierId());
        orderDto.setOrderDate(LocalDate.now());
        orderDto.setDeliveryDate(dto.getDeliveryDate());
        orderDto.setCurrency(dto.getAwardCurrency() != null ? dto.getAwardCurrency() : entity.getCurrency());
        orderDto.setTotalAmount(dto.getAwardAmount());
        orderDto.setTaxAmount(dto.getAwardTaxAmount() != null ? dto.getAwardTaxAmount() : BigDecimal.ZERO);
        orderDto.setPayAmount((dto.getAwardAmount() != null ? dto.getAwardAmount() : BigDecimal.ZERO)
                .add(dto.getAwardTaxAmount() != null ? dto.getAwardTaxAmount() : BigDecimal.ZERO));
        orderDto.setDeliveryAddress(dto.getDeliveryAddress());
        orderDto.setPaymentTerms(dto.getPaymentTerms() != null ? dto.getPaymentTerms() : entity.getPaymentTerms());
        orderDto.setRemark("由RFQ定价自动生成，报价单号: " + entity.getQuoteNo());
        Long orderId = purchaseOrderService.create(orderDto);

        // 4) 回写订单号到 QuoteAward
        award.setOrderId(orderId);
        award.setOrderNo(orderNo);
        quoteAwardMapper.insert(award);
    }

    private String generateOrderNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PO" + datePart + String.format("%04d", System.currentTimeMillis() % 10000);
    }
}