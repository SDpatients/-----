package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.annotation.AuditLog;
import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.order.dto.OrderActionDTO;
import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.dto.PurchaseOrderDetailCreateDTO;
import com.supplier.order.service.PurchaseOrderDetailService;
import com.supplier.order.service.PurchaseOrderService;
import com.supplier.security.model.LoginUser;
import com.supplier.sourcing.converter.QuoteConverter;
import com.supplier.sourcing.dto.BargainDTO;
import com.supplier.sourcing.dto.QuoteCreateDTO;
import com.supplier.sourcing.dto.QuoteItemCreateDTO;
import com.supplier.sourcing.dto.QuotePriceDTO;
import com.supplier.sourcing.dto.QuoteUpdateDTO;
import com.supplier.sourcing.dto.QuoteUpdateDTO.QuoteItemUpdateDTO;
import com.supplier.sourcing.entity.Quote;
import com.supplier.sourcing.entity.QuoteAward;
import com.supplier.sourcing.entity.QuoteItem;
import com.supplier.sourcing.entity.QuoteNegotiation;
import com.supplier.sourcing.entity.Rfq;
import com.supplier.sourcing.entity.RfqItem;
import com.supplier.sourcing.enums.QuoteStatusEnum;
import com.supplier.sourcing.enums.RfqStatusEnum;
import com.supplier.sourcing.mapper.QuoteAwardMapper;
import com.supplier.sourcing.mapper.QuoteItemMapper;
import com.supplier.sourcing.mapper.QuoteMapper;
import com.supplier.sourcing.mapper.QuoteNegotiationMapper;
import com.supplier.sourcing.mapper.RfqItemMapper;
import com.supplier.sourcing.mapper.RfqMapper;
import com.supplier.sourcing.query.QuoteQuery;
import com.supplier.sourcing.service.ExchangeRateService;
import com.supplier.sourcing.service.QuoteService;
import com.supplier.sourcing.service.SupplierService;
import com.supplier.sourcing.vo.BargainVO;
import com.supplier.sourcing.vo.QuoteNegotiationVO;
import com.supplier.sourcing.vo.QuoteVO;
import com.supplier.sourcing.vo.RfqSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteMapper quoteMapper;
    private final QuoteItemMapper quoteItemMapper;
    private final RfqMapper rfqMapper;
    private final RfqItemMapper rfqItemMapper;
    private final QuoteNegotiationMapper quoteNegotiationMapper;
    private final ExchangeRateService exchangeRateService;
    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderDetailService purchaseOrderDetailService;
    private final QuoteAwardMapper quoteAwardMapper;
    private final SupplierService supplierService;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public PageResult<QuoteVO> page(QuoteQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());

        // 若未指定询价单，则默认选中"最近有报价的询价单"，
        // 避免一次性返回所有报价数据。
        if (query.getRfqId() == null) {
            List<RfqSummaryVO> latest = listRfqWithQuotes();
            if (!latest.isEmpty()) {
                query.setRfqId(latest.get(0).getId());
            }
        }

        LambdaQueryWrapper<Quote> wrapper = new LambdaQueryWrapper<Quote>()
                .eq(query.getRfqId() != null, Quote::getRfqId, query.getRfqId())
                .eq(supplierId != null, Quote::getSupplierId, supplierId)
                .eq(query.getQuoteStatus() != null, Quote::getQuoteStatus, query.getQuoteStatus())
                .like(StringUtils.hasText(query.getKeyword()), Quote::getQuoteNo, query.getKeyword())
                .orderByDesc(Quote::getCreateTime);
        Page<Quote> page = quoteMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        // 批量获取 supplierName
        List<Long> supplierIds = page.getRecords().stream()
                .map(Quote::getSupplierId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> supplierNameMap = supplierIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            try {
                                return supplierService.getDetail(id).getSupplierName();
                            } catch (Exception e) {
                                return "未知供应商";
                            }
                        }
                ));

        // 批量获取 rfqNo / rfqTitle
        List<Long> rfqIds = page.getRecords().stream()
                .map(Quote::getRfqId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Rfq> rfqMap = rfqIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : rfqMapper.selectBatchIds(rfqIds).stream()
                        .collect(Collectors.toMap(Rfq::getId, r -> r));

        // 转换并填充 supplierName / rfqNo / rfqTitle
        List<QuoteVO> voList = page.getRecords().stream()
                .map(quote -> {
                    Rfq rfq = rfqMap.get(quote.getRfqId());
                    String rfqNo = rfq != null ? rfq.getRfqNo() : null;
                    String rfqTitle = rfq != null ? rfq.getRfqTitle() : null;
                    return QuoteConverter.toVO(quote, supplierNameMap.get(quote.getSupplierId()), rfqNo, rfqTitle);
                })
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public QuoteVO getDetail(Long id) {
        Quote entity = getWithScope(id);
        String supplierName = "未知供应商";
        try {
            supplierName = supplierService.getDetail(entity.getSupplierId()).getSupplierName();
        } catch (Exception ignored) {}
        Rfq rfq = entity.getRfqId() != null ? rfqMapper.selectById(entity.getRfqId()) : null;
        String rfqNo = rfq != null ? rfq.getRfqNo() : null;
        String rfqTitle = rfq != null ? rfq.getRfqTitle() : null;
        return QuoteConverter.toVO(entity, supplierName, rfqNo, rfqTitle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "报价", businessType = "quote", action = "创建报价", businessIdExpr = "#result", afterStatusExpr = "0")
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

        // 保存报价明细行
        List<QuoteItemCreateDTO> lines = dto.getLines();
        if (lines != null && !lines.isEmpty()) {
            for (QuoteItemCreateDTO line : lines) {
                QuoteItem item = new QuoteItem();
                item.setQuoteId(entity.getId());
                item.setRfqItemId(line.getRfqLineId());
                item.setMaterialCode(line.getMaterialCode());
                item.setMaterialName(line.getMaterialName());
                item.setSpec(line.getSpec());
                item.setUnit(line.getUnit());
                item.setQuantity(line.getQuantity());
                item.setPrice(line.getUnitPrice());
                item.setAmount(line.getTotalPrice());
                item.setTaxRate(line.getTaxRate());
                item.setTaxAmount(line.getTaxAmount());
                item.setDeliveryDate(line.getDeliveryDate());
                item.setPaymentTerms(line.getPaymentTerms());
                item.setRemark(line.getRemark());
                quoteItemMapper.insert(item);
            }
        }

        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "报价", businessType = "quote", action = "提交报价", businessIdExpr = "#id", afterStatusExpr = "1")
    public void submit(Long id) {
        Quote entity = getWithScope(id);
        if (!QuoteStatusEnum.DRAFT.getCode().equals(entity.getQuoteStatus())
                && !QuoteStatusEnum.WITHDRAWN.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿或已撤回状态可提交");
        }
        // 校验询价单未截止
        checkRfqNotClosed(entity.getRfqId());
        // 联动更新询价单状态：PUBLISHED → QUOTING
        syncRfqStatusToQuoting(entity.getRfqId());
        entity.setQuoteStatus(QuoteStatusEnum.SUBMITTED.getCode());
        entity.setSubmitTime(LocalDateTime.now());
        quoteMapper.updateById(entity);
    }

    /**
     * 联动更新询价单状态为"报价中(QUOTING)"
     * <p>
     * 当询价单当前状态为"已发布(PUBLISHED)"时，供应商成功提交首份报价后，
     * 自动将询价单状态由 PUBLISHED 流转为 QUOTING，
     * 以保证后续采纳、截止等操作可正常进行。
     * </p>
     *
     * @param rfqId 询价单ID
     */
    private void syncRfqStatusToQuoting(Long rfqId) {
        if (rfqId == null) {
            return;
        }
        Rfq rfq = rfqMapper.selectById(rfqId);
        if (rfq == null) {
            return;
        }
        if (RfqStatusEnum.PUBLISHED.getCode().equals(rfq.getRfqStatus())) {
            rfq.setRfqStatus(RfqStatusEnum.QUOTING.getCode());
            rfqMapper.updateById(rfq);
            log.info("[RFQ状态联动] 询价单 {} 由 PUBLISHED 流转为 QUOTING", rfq.getRfqNo());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "报价", businessType = "quote", action = "撤回报价", businessIdExpr = "#id", beforeStatusExpr = "1", afterStatusExpr = "4")
    public void withdraw(Long id) {
        Quote entity = getWithScope(id);
        if (!QuoteStatusEnum.SUBMITTED.getCode().equals(entity.getQuoteStatus())
                && !QuoteStatusEnum.REJECTED.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交或未采纳状态可撤回");
        }
        // 校验询价单未截止
        checkRfqNotClosed(entity.getRfqId());
        entity.setQuoteStatus(QuoteStatusEnum.WITHDRAWN.getCode());
        quoteMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "报价", businessType = "quote", action = "采纳报价", businessIdExpr = "#id", beforeStatusExpr = "1", afterStatusExpr = "5")
    public void accept(Long id) {
        Quote entity = quoteMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!QuoteStatusEnum.SUBMITTED.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已提交状态可采纳");
        }
        // 校验对应询价单状态：只有报价中或已截止状态可采纳报价
        Rfq rfq = rfqMapper.selectById(entity.getRfqId());
        if (rfq == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "询价单不存在");
        }
        if (!RfqStatusEnum.QUOTING.getCode().equals(rfq.getRfqStatus())
                && !RfqStatusEnum.CLOSED.getCode().equals(rfq.getRfqStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "询价单当前状态不允许采纳报价");
        }

        // 1) 当前报价直接置为"已定价"，跳过中间"已采纳"态（方案A：采纳即定价）
        entity.setQuoteStatus(QuoteStatusEnum.PRICED.getCode());
        quoteMapper.updateById(entity);

        // 2) 同询价单下其他已提交报价自动标记为"未采纳"
        List<Quote> siblingQuotes = quoteMapper.selectList(new LambdaQueryWrapper<Quote>()
                .eq(Quote::getRfqId, entity.getRfqId())
                .ne(Quote::getId, id)
                .eq(Quote::getQuoteStatus, QuoteStatusEnum.SUBMITTED.getCode()));
        for (Quote sibling : siblingQuotes) {
            sibling.setQuoteStatus(QuoteStatusEnum.REJECTED.getCode());
            quoteMapper.updateById(sibling);
        }

        // 3) 创建 QuoteAward 定价记录
        LoginUser loginUser = SecurityUtils.getLoginUser();
        QuoteAward award = new QuoteAward();
        award.setRfqId(entity.getRfqId());
        award.setQuoteId(entity.getId());
        award.setSupplierId(entity.getSupplierId());
        award.setAwardAmount(entity.getTotalAmount());
        award.setAwardTaxAmount(entity.getTaxAmount() != null ? entity.getTaxAmount() : BigDecimal.ZERO);
        award.setAwardCurrency(entity.getCurrency());
        award.setAwardBy(loginUser != null ? loginUser.getUserId() : null);
        award.setAwardByName(loginUser != null ? loginUser.getRealName() : null);
        award.setAwardTime(LocalDateTime.now());
        award.setRemark("由采购方采纳报价自动生成");

        // 4) 自动生成采购订单（含明细行）
        BigDecimal awardAmount = entity.getTotalAmount() != null ? entity.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal awardTaxAmount = entity.getTaxAmount() != null ? entity.getTaxAmount() : BigDecimal.ZERO;
        LocalDate orderDate = LocalDate.now();
        LocalDate earliestDeliveryDate = computeEarliestDeliveryDate(entity.getId());

        PurchaseOrderCreateDTO orderDto = new PurchaseOrderCreateDTO();
        orderDto.setOrderNo(generateOrderNo());
        orderDto.setSupplierId(entity.getSupplierId());
        orderDto.setOrderDate(orderDate);
        orderDto.setDeliveryDate(earliestDeliveryDate);
        orderDto.setCurrency(entity.getCurrency() != null ? entity.getCurrency() : "CNY");
        orderDto.setTotalAmount(awardAmount);
        orderDto.setTaxAmount(awardTaxAmount);
        orderDto.setPayAmount(awardAmount.add(awardTaxAmount));
        orderDto.setPaymentTerms(entity.getPaymentTerms());
        orderDto.setRemark("由RFQ" + rfq.getRfqNo() + "采纳报价自动生成，报价单号: " + entity.getQuoteNo());
        Long orderId = purchaseOrderService.create(orderDto);

        // 4.1) 立刻把订单下发到供应商（接受报价即视为已下单）：
        //      - 状态由 DRAFT 流转为 待确认
        //      - 写入供应商待办 + 站内信
        //      - 发布 order.purchase.published 领域事件
        //      这样供应商就能在 /supplier/orders 看到该订单
        OrderActionDTO publishDto = new OrderActionDTO();
        publishDto.setRemark("由RFQ" + rfq.getRfqNo() + "采纳报价自动下发，报价单号: " + entity.getQuoteNo());
        purchaseOrderService.publish(orderId, publishDto);

        // 5) 回填订单号到 QuoteAward
        award.setOrderId(orderId);
        award.setOrderNo(orderDto.getOrderNo());
        quoteAwardMapper.insert(award);

        // 6) 复制报价明细行到采购订单明细
        copyQuoteItemsToOrder(entity.getId(), orderId, earliestDeliveryDate);

        // 7) 联动更新询价单状态为已定价
        rfq.setRfqStatus(RfqStatusEnum.PRICED.getCode());
        rfqMapper.updateById(rfq);

        // 8) 发布领域事件：通知其他被采纳信息（采购订单的待办/通知已在 publish() 中发出，避免重复）
        publishAcceptEvents(entity, award, orderId, orderDto.getOrderNo());

        // 9) 发布领域事件：通知其他被拒绝的供应商
        for (Quote sibling : siblingQuotes) {
            publishRejectEvent(sibling, rfq.getRfqNo());
        }
    }

    /**
     * 计算最早交期：取所有报价明细行中最早的 deliveryDate；如无则用 RFQ 行的 targetDeliveryDate；
     * 仍无则取订单日期 + 7 天。
     */
    private LocalDate computeEarliestDeliveryDate(Long quoteId) {
        List<QuoteItem> items = quoteItemMapper.selectList(
                new LambdaQueryWrapper<QuoteItem>().eq(QuoteItem::getQuoteId, quoteId));
        LocalDate earliest = null;
        for (QuoteItem item : items) {
            if (item.getDeliveryDate() != null && (earliest == null || item.getDeliveryDate().isBefore(earliest))) {
                earliest = item.getDeliveryDate();
            }
        }
        if (earliest == null && !items.isEmpty()) {
            List<RfqItem> rfqItems = rfqItemMapper.selectList(
                    new LambdaQueryWrapper<RfqItem>().eq(RfqItem::getRfqId,
                            quoteMapper.selectById(quoteId).getRfqId()));
            for (RfqItem ri : rfqItems) {
                if (ri.getTargetDeliveryDate() != null && (earliest == null || ri.getTargetDeliveryDate().isBefore(earliest))) {
                    earliest = ri.getTargetDeliveryDate();
                }
            }
        }
        return earliest != null ? earliest : LocalDate.now().plusDays(7);
    }

    /**
     * 将报价明细行复制为采购订单明细行。
     */
    private void copyQuoteItemsToOrder(Long quoteId, Long orderId, LocalDate fallbackDeliveryDate) {
        List<QuoteItem> items = quoteItemMapper.selectList(
                new LambdaQueryWrapper<QuoteItem>().eq(QuoteItem::getQuoteId, quoteId)
                        .orderByAsc(QuoteItem::getId));
        int lineNo = 1;
        for (QuoteItem item : items) {
            PurchaseOrderDetailCreateDTO dto = new PurchaseOrderDetailCreateDTO();
            dto.setOrderId(orderId);
            dto.setLineNo(lineNo++);
            dto.setMaterialCode(item.getMaterialCode());
            dto.setMaterialName(item.getMaterialName());
            dto.setMaterialSpec(item.getSpec());
            dto.setUnit(item.getUnit());
            dto.setQuantity(item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO);
            dto.setUnitPrice(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
            dto.setTaxRate(item.getTaxRate() != null ? item.getTaxRate() : BigDecimal.ZERO);
            dto.setTaxAmount(item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO);
            dto.setAmount(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
            dto.setDeliveryDate(item.getDeliveryDate() != null ? item.getDeliveryDate() : fallbackDeliveryDate);
            purchaseOrderDetailService.create(dto);
        }
    }

    /**
     * 发布采纳成功事件：仅发送"报价已被采纳"的站内信。
     * 采购订单的待办 + 订单相关通知已在 {@link PurchaseOrderService#publish(Long, OrderActionDTO)} 中发出，
     * 这里避免重复生成同一订单的待办。
     */
    private void publishAcceptEvents(Quote accepted, QuoteAward award, Long orderId, String orderNo) {
        Map<String, Object> messageData = Map.of(
                "receiverSupplierId", accepted.getSupplierId(),
                "channel", 1,
                "title", "您的报价已被采纳",
                "content", "报价单" + accepted.getQuoteNo() + "已被采购方采纳，并已自动生成采购订单" + orderNo + "，请及时确认。",
                "businessType", "quote",
                "businessId", accepted.getId()
        );
        domainEventPublisher.publish("supplier.notice", "supplier.notice.routing",
                DomainEvent.builder()
                        .eventType("notice.message")
                        .data(messageData)
                        .build());
    }

    /**
     * 发布报价被拒绝事件：站内信给落标的供应商。
     */
    private void publishRejectEvent(Quote rejected, String rfqNo) {
        Map<String, Object> messageData = Map.of(
                "receiverSupplierId", rejected.getSupplierId(),
                "channel", 1,
                "title", "您的报价未被采纳",
                "content", "询价单" + rfqNo + "已确定中标的报价，您提交的报价" + rejected.getQuoteNo() + "未被采纳。感谢您的参与。",
                "businessType", "quote",
                "businessId", rejected.getId()
        );
        domainEventPublisher.publish("supplier.notice", "supplier.notice.routing",
                DomainEvent.builder()
                        .eventType("notice.message")
                        .data(messageData)
                        .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "报价", businessType = "quote", action = "拒绝报价", businessIdExpr = "#id", beforeStatusExpr = "1", afterStatusExpr = "3")
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
    @AuditLog(module = "报价", businessType = "quote", action = "删除报价", businessIdExpr = "#id", beforeStatusExpr = "0")
    public void delete(Long id) {
        Quote entity = getWithScope(id);
        if (!QuoteStatusEnum.DRAFT.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿状态可删除");
        }
        quoteMapper.deleteById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "报价", businessType = "quote", action = "修改报价", businessIdExpr = "#id")
    public void update(Long id, QuoteUpdateDTO dto) {
        Quote entity = getWithScope(id);
        if (!QuoteStatusEnum.DRAFT.getCode().equals(entity.getQuoteStatus())
                && !QuoteStatusEnum.WITHDRAWN.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿或已撤回状态可修改");
        }
        checkRfqNotClosed(entity.getRfqId());

        if (dto.getTotalAmount() != null) {
            entity.setTotalAmount(dto.getTotalAmount());
        }
        if (dto.getTaxAmount() != null) {
            entity.setTaxAmount(dto.getTaxAmount());
        }
        entity.setRemark(dto.getRemark());
        quoteMapper.updateById(entity);

        List<QuoteItemUpdateDTO> lines = dto.getLines();
        if (lines != null && !lines.isEmpty()) {
            quoteItemMapper.delete(new LambdaQueryWrapper<QuoteItem>().eq(QuoteItem::getQuoteId, id));
            for (QuoteItemUpdateDTO line : lines) {
                QuoteItem item = new QuoteItem();
                item.setQuoteId(entity.getId());
                item.setRfqItemId(line.getRfqLineId());
                item.setMaterialCode(line.getMaterialCode());
                item.setMaterialName(line.getMaterialName());
                item.setSpec(line.getSpec());
                item.setUnit(line.getUnit());
                item.setQuantity(line.getQuantity());
                item.setPrice(line.getUnitPrice());
                item.setAmount(line.getTotalPrice());
                item.setDeliveryDate(line.getDeliveryDate());
                item.setPaymentTerms(line.getPaymentTerms());
                item.setRemark(line.getRemark());
                quoteItemMapper.insert(item);
            }
        }
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
    @AuditLog(module = "报价", businessType = "quote", action = "发起议价", businessIdExpr = "#id", beforeStatusExpr = "1", afterStatusExpr = "0")
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

    @Override
    public List<BargainVO> getBargains(Long quoteId) {
        List<QuoteNegotiation> list = quoteNegotiationMapper.selectList(
                new LambdaQueryWrapper<QuoteNegotiation>()
                        .eq(QuoteNegotiation::getQuoteId, quoteId)
                        .orderByAsc(QuoteNegotiation::getRound)
        );
        return list.stream().map(this::toBargainVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "报价", businessType = "quote", action = "重新提交报价", businessIdExpr = "#quoteId", beforeStatusExpr = "0", afterStatusExpr = "1")
    public void resubmit(Long quoteId, QuoteUpdateDTO dto) {
        Quote entity = quoteMapper.selectById(quoteId);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (!QuoteStatusEnum.DRAFT.getCode().equals(entity.getQuoteStatus())
                && !QuoteStatusEnum.REJECTED.getCode().equals(entity.getQuoteStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有草稿或未采纳状态可重新提交");
        }
        checkRfqNotClosed(entity.getRfqId());

        // 更新报价主表金额和备注
        if (dto.getTotalAmount() != null) {
            entity.setTotalAmount(dto.getTotalAmount());
        }
        if (dto.getTaxAmount() != null) {
            entity.setTaxAmount(dto.getTaxAmount());
        }
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }

        // 更新明细行（先删后插）
        List<QuoteUpdateDTO.QuoteItemUpdateDTO> lines = dto.getLines();
        if (lines != null && !lines.isEmpty()) {
            quoteItemMapper.delete(new LambdaQueryWrapper<QuoteItem>().eq(QuoteItem::getQuoteId, quoteId));
            for (QuoteUpdateDTO.QuoteItemUpdateDTO line : lines) {
                QuoteItem item = new QuoteItem();
                item.setQuoteId(entity.getId());
                item.setRfqItemId(line.getRfqLineId());
                item.setMaterialCode(line.getMaterialCode());
                item.setMaterialName(line.getMaterialName());
                item.setSpec(line.getSpec());
                item.setUnit(line.getUnit());
                item.setQuantity(line.getQuantity());
                item.setPrice(line.getUnitPrice());
                item.setAmount(line.getTotalPrice());
                item.setDeliveryDate(line.getDeliveryDate());
                item.setPaymentTerms(line.getPaymentTerms());
                item.setRemark(line.getRemark());
                quoteItemMapper.insert(item);
            }
        }

        entity.setQuoteStatus(QuoteStatusEnum.SUBMITTED.getCode());
        entity.setSubmitTime(LocalDateTime.now());
        quoteMapper.updateById(entity);

        int round = (entity.getNegotiationRound() != null ? entity.getNegotiationRound() : 0);
        QuoteNegotiation negotiation = new QuoteNegotiation();
        negotiation.setQuoteId(entity.getId());
        negotiation.setRfqId(entity.getRfqId());
        negotiation.setSupplierId(entity.getSupplierId());
        negotiation.setRound(round);
        negotiation.setInitiator("SUPPLIER");
        negotiation.setSupplierPrice(entity.getTotalAmount());
        negotiation.setSupplierRemark(dto.getRemark());
        negotiation.setNegotiationTime(LocalDateTime.now());
        quoteNegotiationMapper.insert(negotiation);
    }

    @Override
    public List<RfqSummaryVO> listRfqWithQuotes() {
        // 供应商用户只看到自己有报价的询价单；采购方可看全部
        Long supplierId = resolveSupplierId(null);
        LambdaQueryWrapper<Quote> wrapper = new LambdaQueryWrapper<Quote>()
                .isNotNull(Quote::getRfqId)
                .eq(supplierId != null, Quote::getSupplierId, supplierId)
                .orderByDesc(Quote::getCreateTime);
        List<Quote> quotes = quoteMapper.selectList(wrapper);
        if (quotes.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 按 rfqId 分组，统计数量与最后报价时间
        Map<Long, List<Quote>> grouped = quotes.stream()
                .collect(Collectors.groupingBy(Quote::getRfqId));

        // 批量取询价单基础信息
        List<Long> rfqIds = new ArrayList<>(grouped.keySet());
        Map<Long, Rfq> rfqMap = rfqMapper.selectBatchIds(rfqIds).stream()
                .collect(Collectors.toMap(Rfq::getId, r -> r));

        List<RfqSummaryVO> result = new ArrayList<>(grouped.size());
        for (Map.Entry<Long, List<Quote>> entry : grouped.entrySet()) {
            Long rfqId = entry.getKey();
            Rfq rfq = rfqMap.get(rfqId);
            if (rfq == null) {
                continue;
            }
            List<Quote> list = entry.getValue();
            LocalDateTime latestTime = list.stream()
                    .map(Quote::getCreateTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            RfqSummaryVO vo = new RfqSummaryVO();
            vo.setId(rfqId);
            vo.setRfqNo(rfq.getRfqNo());
            vo.setRfqTitle(rfq.getRfqTitle());
            vo.setOrgId(rfq.getOrgId());
            vo.setCurrency(rfq.getCurrency());
            vo.setQuoteDeadline(rfq.getQuoteDeadline());
            vo.setRfqStatus(rfq.getRfqStatus());
            vo.setPublishTime(rfq.getPublishTime());
            vo.setCloseTime(rfq.getCloseTime());
            vo.setRemark(rfq.getRemark());
            vo.setCreateTime(rfq.getCreateTime());
            vo.setQuoteCount((long) list.size());
            vo.setLatestQuoteTime(latestTime);
            result.add(vo);
        }

        // 按最后报价时间倒序，最新报价的询价单排第一
        result.sort((a, b) -> {
            LocalDateTime la = a.getLatestQuoteTime();
            LocalDateTime lb = b.getLatestQuoteTime();
            if (la == null && lb == null) {
                return 0;
            }
            if (la == null) {
                return 1;
            }
            if (lb == null) {
                return -1;
            }
            return lb.compareTo(la);
        });
        return result;
    }

    private BargainVO toBargainVO(QuoteNegotiation entity) {
        BargainVO vo = new BargainVO();
        vo.setId(entity.getId());
        vo.setQuoteId(entity.getQuoteId());
        vo.setRfqId(entity.getRfqId());
        String initiator = entity.getInitiator();
        vo.setFromUserType("BUYER".equals(initiator) ? "buyer" : "supplier");
        vo.setFromUserName(resolveUserName(entity));
        vo.setAction("BUYER".equals(initiator) ? "request_reprice" : "resubmit");
        vo.setMessage("BUYER".equals(initiator) ? entity.getBuyerRemark() : entity.getSupplierRemark());
        vo.setTargetPrice(entity.getTargetPrice());
        vo.setSupplierPrice(entity.getSupplierPrice());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String resolveUserName(QuoteNegotiation entity) {
        if ("BUYER".equals(entity.getInitiator())) {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null && loginUser.getRealName() != null) {
                return loginUser.getRealName();
            }
            return "采购方";
        }
        try {
            return supplierService.getDetail(entity.getSupplierId()).getSupplierName();
        } catch (Exception e) {
            return "供应商";
        }
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
    /**
     * 兼容旧数据保留入口。新流程已由 {@link #accept(Long)} 一步完成采纳+定价+生成订单，
     * 客户端不再需要单独调用本方法。该方法依赖"已采纳"中间态，调用方需自行保证数据状态。
     *
     * @deprecated since 2026-06, 使用 accept(id) 替代
     */
    @Deprecated
    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(module = "报价", businessType = "quote", action = "报价定价", businessIdExpr = "#id", beforeStatusExpr = "2", afterStatusExpr = "5")
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