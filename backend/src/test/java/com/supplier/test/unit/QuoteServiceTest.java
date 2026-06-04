package com.supplier.test.unit;

import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.order.dto.OrderActionDTO;
import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.dto.PurchaseOrderDetailCreateDTO;
import com.supplier.order.service.PurchaseOrderDetailService;
import com.supplier.order.service.PurchaseOrderService;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import com.supplier.sourcing.entity.Quote;
import com.supplier.sourcing.entity.QuoteAward;
import com.supplier.sourcing.entity.QuoteItem;
import com.supplier.sourcing.entity.Rfq;
import com.supplier.sourcing.entity.RfqItem;
import com.supplier.sourcing.enums.QuoteStatusEnum;
import com.supplier.sourcing.enums.RfqStatusEnum;
import com.supplier.sourcing.mapper.QuoteAwardMapper;
import com.supplier.sourcing.mapper.QuoteItemMapper;
import com.supplier.sourcing.mapper.QuoteMapper;
import com.supplier.sourcing.mapper.RfqItemMapper;
import com.supplier.sourcing.mapper.RfqMapper;
import com.supplier.sourcing.service.impl.QuoteServiceImpl;
import com.supplier.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class QuoteServiceTest extends BaseUnitTest {

    @InjectMocks
    private QuoteServiceImpl quoteService;

    @Mock
    private QuoteMapper quoteMapper;

    @Mock
    private QuoteItemMapper quoteItemMapper;

    @Mock
    private RfqMapper rfqMapper;

    @Mock
    private RfqItemMapper rfqItemMapper;

    @Mock
    private QuoteAwardMapper quoteAwardMapper;

    @Mock
    private PurchaseOrderService purchaseOrderService;

    @Mock
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    private Quote submittedQuote;
    private Rfq quotingRfq;

    @BeforeEach
    void setUp() {
        submittedQuote = new Quote();
        submittedQuote.setId(10L);
        submittedQuote.setQuoteNo("QT20260601001");
        submittedQuote.setRfqId(1L);
        submittedQuote.setSupplierId(100L);
        submittedQuote.setCurrency("CNY");
        submittedQuote.setTotalAmount(new BigDecimal("50000.00"));
        submittedQuote.setTaxAmount(new BigDecimal("6500.00"));
        submittedQuote.setQuoteStatus(QuoteStatusEnum.SUBMITTED.getCode());
        submittedQuote.setPaymentTerms("Net30");

        quotingRfq = new Rfq();
        quotingRfq.setId(1L);
        quotingRfq.setRfqNo("RFQ20260601001");
        quotingRfq.setRfqStatus(RfqStatusEnum.QUOTING.getCode());
        quotingRfq.setCurrency("CNY");
    }

    @Nested
    @DisplayName("采纳报价 - accept()")
    class AcceptTests {

        @Test
        @DisplayName("采纳报价成功 - 完整流程验证")
        void accept_success_fullFlow() {
            // 准备数据
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);
            when(quoteMapper.updateById(any())).thenReturn(1);

            // 同询价单下其他已提交报价
            Quote siblingQuote = new Quote();
            siblingQuote.setId(20L);
            siblingQuote.setRfqId(1L);
            siblingQuote.setQuoteStatus(QuoteStatusEnum.SUBMITTED.getCode());
            siblingQuote.setSupplierId(200L);
            when(quoteMapper.selectList(any())).thenReturn(Collections.singletonList(siblingQuote));

            // 报价明细行
            QuoteItem item1 = new QuoteItem();
            item1.setId(1L);
            item1.setQuoteId(10L);
            item1.setMaterialCode("MAT001");
            item1.setMaterialName("螺丝");
            item1.setSpec("M8x20");
            item1.setUnit("PCS");
            item1.setQuantity(new BigDecimal("10000"));
            item1.setPrice(new BigDecimal("5.00"));
            item1.setTaxRate(new BigDecimal("0.13"));
            item1.setAmount(new BigDecimal("50000.00"));
            item1.setTaxAmount(new BigDecimal("6500.00"));
            item1.setDeliveryDate(LocalDate.now().plusDays(30));
            when(quoteItemMapper.selectList(any())).thenReturn(Collections.singletonList(item1));

            // 采购订单创建返回ID
            when(purchaseOrderService.create(any(PurchaseOrderCreateDTO.class))).thenReturn(100L);
            doNothing().when(purchaseOrderService).publish(eq(100L), any(OrderActionDTO.class));
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);
            when(purchaseOrderDetailService.create(any())).thenReturn(1L);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                LoginUser loginUser = mock(LoginUser.class);
                when(loginUser.getUserId()).thenReturn(1L);
                when(loginUser.getRealName()).thenReturn("采购员张三");
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);

                quoteService.accept(10L);
            }

            // 验证报价状态更新为已定价
            assertEquals(QuoteStatusEnum.PRICED.getCode(), submittedQuote.getQuoteStatus());
            verify(quoteMapper).updateById(submittedQuote);

            // 验证其他报价被拒绝
            assertEquals(QuoteStatusEnum.REJECTED.getCode(), siblingQuote.getQuoteStatus());
            verify(quoteMapper).updateById(siblingQuote);

            // 验证创建采购订单
            ArgumentCaptor<PurchaseOrderCreateDTO> orderCaptor = ArgumentCaptor.forClass(PurchaseOrderCreateDTO.class);
            verify(purchaseOrderService).create(orderCaptor.capture());
            PurchaseOrderCreateDTO orderDto = orderCaptor.getValue();
            assertEquals(100L, orderDto.getSupplierId());
            assertEquals(new BigDecimal("50000.00"), orderDto.getTotalAmount());
            assertEquals(new BigDecimal("6500.00"), orderDto.getTaxAmount());
            assertEquals(new BigDecimal("56500.00"), orderDto.getPayAmount());
            assertEquals("CNY", orderDto.getCurrency());
            assertTrue(orderDto.getRemark().contains("RFQ20260601001"));
            assertTrue(orderDto.getRemark().contains("QT20260601001"));

            // 验证自动下发订单
            verify(purchaseOrderService).publish(eq(100L), any(OrderActionDTO.class));

            // 验证 QuoteAward 记录
            ArgumentCaptor<QuoteAward> awardCaptor = ArgumentCaptor.forClass(QuoteAward.class);
            verify(quoteAwardMapper).insert(awardCaptor.capture());
            QuoteAward award = awardCaptor.getValue();
            assertEquals(1L, award.getRfqId());
            assertEquals(10L, award.getQuoteId());
            assertEquals(100L, award.getSupplierId());
            assertEquals(new BigDecimal("50000.00"), award.getAwardAmount());
            assertEquals(new BigDecimal("6500.00"), award.getAwardTaxAmount());
            assertEquals(100L, award.getOrderId());

            // 验证复制报价明细行到订单明细
            verify(purchaseOrderDetailService).create(any(PurchaseOrderDetailCreateDTO.class));

            // 验证询价单状态更新为已定价
            assertEquals(RfqStatusEnum.PRICED.getCode(), quotingRfq.getRfqStatus());
            verify(rfqMapper).updateById(quotingRfq);

            // 验证领域事件发布（采纳通知 + 拒绝通知 = 2次）
            verify(domainEventPublisher, times(2)).publish(any(), any(), any(DomainEvent.class));
        }

        @Test
        @DisplayName("采纳报价 - 报价不存在时抛出异常")
        void accept_quoteNotFound_throws() {
            when(quoteMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class, () -> quoteService.accept(999L));
        }

        @Test
        @DisplayName("采纳报价 - 非已提交状态时抛出异常")
        void accept_wrongQuoteStatus_throws() {
            submittedQuote.setQuoteStatus(QuoteStatusEnum.DRAFT.getCode());
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);

            BusinessException ex = assertThrows(BusinessException.class, () -> quoteService.accept(10L));
            assertTrue(ex.getMessage().contains("已提交"));
        }

        @Test
        @DisplayName("采纳报价 - 询价单不存在时抛出异常")
        void accept_rfqNotFound_throws() {
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class, () -> quoteService.accept(10L));
            assertTrue(ex.getMessage().contains("询价单不存在"));
        }

        @Test
        @DisplayName("采纳报价 - 询价单状态不允许时抛出异常")
        void accept_rfqWrongStatus_throws() {
            quotingRfq.setRfqStatus(RfqStatusEnum.DRAFT.getCode());
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);

            BusinessException ex = assertThrows(BusinessException.class, () -> quoteService.accept(10L));
            assertTrue(ex.getMessage().contains("询价单当前状态不允许"));
        }

        @Test
        @DisplayName("采纳报价 - 询价单已截止状态允许采纳")
        void accept_rfqClosedStatus_success() {
            quotingRfq.setRfqStatus(RfqStatusEnum.CLOSED.getCode());
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);
            when(quoteMapper.updateById(any())).thenReturn(1);
            when(quoteMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(quoteItemMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(purchaseOrderService.create(any())).thenReturn(100L);
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                quoteService.accept(10L);
            }

            assertEquals(QuoteStatusEnum.PRICED.getCode(), submittedQuote.getQuoteStatus());
            verify(purchaseOrderService).create(any(PurchaseOrderCreateDTO.class));
        }

        @Test
        @DisplayName("采纳报价 - 同询价单下多个已提交报价全部被拒绝")
        void accept_multipleSiblingsAllRejected() {
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);
            when(quoteMapper.updateById(any())).thenReturn(1);

            Quote sibling1 = new Quote();
            sibling1.setId(20L);
            sibling1.setRfqId(1L);
            sibling1.setQuoteStatus(QuoteStatusEnum.SUBMITTED.getCode());
            sibling1.setSupplierId(200L);

            Quote sibling2 = new Quote();
            sibling2.setId(30L);
            sibling2.setRfqId(1L);
            sibling2.setQuoteStatus(QuoteStatusEnum.SUBMITTED.getCode());
            sibling2.setSupplierId(300L);

            when(quoteMapper.selectList(any())).thenReturn(Arrays.asList(sibling1, sibling2));
            when(quoteItemMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(purchaseOrderService.create(any())).thenReturn(100L);
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                quoteService.accept(10L);
            }

            assertEquals(QuoteStatusEnum.REJECTED.getCode(), sibling1.getQuoteStatus());
            assertEquals(QuoteStatusEnum.REJECTED.getCode(), sibling2.getQuoteStatus());
            // 被采纳报价 + 2个被拒绝报价 = 3次 updateById 调用（报价相关）
            verify(quoteMapper, atLeast(3)).updateById(any());
        }

        @Test
        @DisplayName("采纳报价 - 无报价明细行时交期默认为订单日期+7天")
        void accept_noQuoteItems_deliveryDateDefaults() {
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);
            when(quoteMapper.updateById(any())).thenReturn(1);
            when(quoteMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(quoteItemMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(purchaseOrderService.create(any())).thenReturn(100L);
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                quoteService.accept(10L);
            }

            ArgumentCaptor<PurchaseOrderCreateDTO> orderCaptor = ArgumentCaptor.forClass(PurchaseOrderCreateDTO.class);
            verify(purchaseOrderService).create(orderCaptor.capture());
            LocalDate expectedDelivery = LocalDate.now().plusDays(7);
            assertEquals(expectedDelivery, orderCaptor.getValue().getDeliveryDate());
        }

        @Test
        @DisplayName("采纳报价 - 报价明细行有交期时取最早交期")
        void accept_quoteItemsWithDeliveryDate_earliestDelivery() {
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);
            when(quoteMapper.updateById(any())).thenReturn(1);
            when(quoteMapper.selectList(any())).thenReturn(Collections.emptyList());

            QuoteItem item1 = new QuoteItem();
            item1.setId(1L);
            item1.setQuoteId(10L);
            item1.setDeliveryDate(LocalDate.now().plusDays(20));

            QuoteItem item2 = new QuoteItem();
            item2.setId(2L);
            item2.setQuoteId(10L);
            item2.setDeliveryDate(LocalDate.now().plusDays(15));

            when(quoteItemMapper.selectList(any())).thenReturn(Arrays.asList(item1, item2));
            when(purchaseOrderService.create(any())).thenReturn(100L);
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);
            when(purchaseOrderDetailService.create(any())).thenReturn(1L);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                quoteService.accept(10L);
            }

            ArgumentCaptor<PurchaseOrderCreateDTO> orderCaptor = ArgumentCaptor.forClass(PurchaseOrderCreateDTO.class);
            verify(purchaseOrderService).create(orderCaptor.capture());
            assertEquals(LocalDate.now().plusDays(15), orderCaptor.getValue().getDeliveryDate());
        }

        @Test
        @DisplayName("采纳报价 - payAmount等于totalAmount加taxAmount")
        void accept_payAmountCalculation() {
            submittedQuote.setTotalAmount(new BigDecimal("100000.00"));
            submittedQuote.setTaxAmount(new BigDecimal("13000.00"));
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);
            when(quoteMapper.updateById(any())).thenReturn(1);
            when(quoteMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(quoteItemMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(purchaseOrderService.create(any())).thenReturn(100L);
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                quoteService.accept(10L);
            }

            ArgumentCaptor<PurchaseOrderCreateDTO> orderCaptor = ArgumentCaptor.forClass(PurchaseOrderCreateDTO.class);
            verify(purchaseOrderService).create(orderCaptor.capture());
            assertEquals(new BigDecimal("113000.00"), orderCaptor.getValue().getPayAmount());
        }

        @Test
        @DisplayName("采纳报价 - taxAmount为null时默认为ZERO")
        void accept_nullTaxAmount_defaultsToZero() {
            submittedQuote.setTaxAmount(null);
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);
            when(quoteMapper.updateById(any())).thenReturn(1);
            when(quoteMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(quoteItemMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(purchaseOrderService.create(any())).thenReturn(100L);
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                quoteService.accept(10L);
            }

            ArgumentCaptor<PurchaseOrderCreateDTO> orderCaptor = ArgumentCaptor.forClass(PurchaseOrderCreateDTO.class);
            verify(purchaseOrderService).create(orderCaptor.capture());
            assertEquals(BigDecimal.ZERO, orderCaptor.getValue().getTaxAmount());
            assertEquals(new BigDecimal("50000.00"), orderCaptor.getValue().getPayAmount());
        }

        @Test
        @DisplayName("采纳报价 - 复制明细行字段映射正确")
        void accept_copyQuoteItems_fieldMapping() {
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);
            when(quoteMapper.updateById(any())).thenReturn(1);
            when(quoteMapper.selectList(any())).thenReturn(Collections.emptyList());

            QuoteItem item = new QuoteItem();
            item.setId(1L);
            item.setQuoteId(10L);
            item.setMaterialCode("MAT001");
            item.setMaterialName("螺丝");
            item.setSpec("M8x20");
            item.setUnit("PCS");
            item.setQuantity(new BigDecimal("10000"));
            item.setPrice(new BigDecimal("5.00"));
            item.setTaxRate(new BigDecimal("0.13"));
            item.setAmount(new BigDecimal("50000.00"));
            item.setTaxAmount(new BigDecimal("6500.00"));
            item.setDeliveryDate(LocalDate.of(2026, 7, 15));
            when(quoteItemMapper.selectList(any())).thenReturn(Collections.singletonList(item));

            when(purchaseOrderService.create(any())).thenReturn(100L);
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);
            when(purchaseOrderDetailService.create(any())).thenReturn(1L);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                quoteService.accept(10L);
            }

            ArgumentCaptor<PurchaseOrderDetailCreateDTO> detailCaptor = ArgumentCaptor.forClass(PurchaseOrderDetailCreateDTO.class);
            verify(purchaseOrderDetailService).create(detailCaptor.capture());
            PurchaseOrderDetailCreateDTO detailDto = detailCaptor.getValue();
            assertEquals(100L, detailDto.getOrderId());
            assertEquals(1, detailDto.getLineNo());
            assertEquals("MAT001", detailDto.getMaterialCode());
            assertEquals("螺丝", detailDto.getMaterialName());
            assertEquals("M8x20", detailDto.getMaterialSpec());
            assertEquals("PCS", detailDto.getUnit());
            assertEquals(new BigDecimal("10000"), detailDto.getQuantity());
            assertEquals(new BigDecimal("5.00"), detailDto.getUnitPrice());
            assertEquals(new BigDecimal("0.13"), detailDto.getTaxRate());
            assertEquals(new BigDecimal("6500.00"), detailDto.getTaxAmount());
            assertEquals(new BigDecimal("50000.00"), detailDto.getAmount());
            assertEquals(LocalDate.of(2026, 7, 15), detailDto.getDeliveryDate());
        }

        @Test
        @DisplayName("采纳报价 - 询价单已定价状态不允许采纳")
        void accept_rfqPricedStatus_throws() {
            quotingRfq.setRfqStatus(RfqStatusEnum.PRICED.getCode());
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);

            assertThrows(BusinessException.class, () -> quoteService.accept(10L));
        }

        @Test
        @DisplayName("采纳报价 - 询价单已取消状态不允许采纳")
        void accept_rfqCanceledStatus_throws() {
            quotingRfq.setRfqStatus(RfqStatusEnum.CANCELED.getCode());
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);

            assertThrows(BusinessException.class, () -> quoteService.accept(10L));
        }
    }

    @Nested
    @DisplayName("拒绝报价 - reject()")
    class RejectTests {

        @Test
        @DisplayName("拒绝已提交报价成功")
        void reject_submittedQuote_success() {
            submittedQuote.setQuoteStatus(QuoteStatusEnum.SUBMITTED.getCode());
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(quoteMapper.updateById(any())).thenReturn(1);

            quoteService.reject(10L);

            assertEquals(QuoteStatusEnum.REJECTED.getCode(), submittedQuote.getQuoteStatus());
            verify(quoteMapper).updateById(submittedQuote);
        }

        @Test
        @DisplayName("拒绝非已提交状态报价抛出异常")
        void reject_wrongStatus_throws() {
            submittedQuote.setQuoteStatus(QuoteStatusEnum.DRAFT.getCode());
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);

            assertThrows(BusinessException.class, () -> quoteService.reject(10L));
        }
    }

    @Nested
    @DisplayName("提交报价 - submit()")
    class SubmitTests {

        @Test
        @DisplayName("提交草稿报价成功")
        void submit_draftQuote_success() {
            submittedQuote.setQuoteStatus(QuoteStatusEnum.DRAFT.getCode());
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(quoteMapper.updateById(any())).thenReturn(1);

            quoteService.submit(10L);

            assertEquals(QuoteStatusEnum.SUBMITTED.getCode(), submittedQuote.getQuoteStatus());
            assertNotNull(submittedQuote.getSubmitTime());
        }

        @Test
        @DisplayName("提交已撤回报价成功")
        void submit_withdrawnQuote_success() {
            submittedQuote.setQuoteStatus(QuoteStatusEnum.WITHDRAWN.getCode());
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(quoteMapper.updateById(any())).thenReturn(1);

            quoteService.submit(10L);

            assertEquals(QuoteStatusEnum.SUBMITTED.getCode(), submittedQuote.getQuoteStatus());
        }
    }

    @Nested
    @DisplayName("撤回报价 - withdraw()")
    class WithdrawTests {

        @Test
        @DisplayName("撤回已提交报价成功")
        void withdraw_submittedQuote_success() {
            submittedQuote.setQuoteStatus(QuoteStatusEnum.SUBMITTED.getCode());
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(quoteMapper.updateById(any())).thenReturn(1);

            quoteService.withdraw(10L);

            assertEquals(QuoteStatusEnum.WITHDRAWN.getCode(), submittedQuote.getQuoteStatus());
        }
    }
}
