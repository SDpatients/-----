package com.supplier.test.unit;

import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.service.PurchaseOrderService;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import com.supplier.sourcing.dto.PricingDTO;
import com.supplier.sourcing.entity.Quote;
import com.supplier.sourcing.entity.QuoteAward;
import com.supplier.sourcing.entity.Rfq;
import com.supplier.sourcing.enums.QuoteStatusEnum;
import com.supplier.sourcing.enums.RfqStatusEnum;
import com.supplier.sourcing.mapper.QuoteAwardMapper;
import com.supplier.sourcing.mapper.QuoteMapper;
import com.supplier.sourcing.mapper.RfqItemMapper;
import com.supplier.sourcing.mapper.RfqMapper;
import com.supplier.sourcing.mapper.RfqSupplierMapper;
import com.supplier.sourcing.service.impl.RfqServiceImpl;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RfqServiceTest extends BaseUnitTest {

    @InjectMocks
    private RfqServiceImpl rfqService;

    @Mock
    private RfqMapper rfqMapper;

    @Mock
    private RfqItemMapper rfqItemMapper;

    @Mock
    private RfqSupplierMapper rfqSupplierMapper;

    @Mock
    private QuoteMapper quoteMapper;

    @Mock
    private QuoteAwardMapper quoteAwardMapper;

    @Mock
    private PurchaseOrderService purchaseOrderService;

    private Rfq closedRfq;
    private Quote submittedQuote;

    @BeforeEach
    void setUp() {
        closedRfq = new Rfq();
        closedRfq.setId(1L);
        closedRfq.setRfqNo("RFQ20260601001");
        closedRfq.setRfqStatus(RfqStatusEnum.CLOSED.getCode());
        closedRfq.setCurrency("CNY");

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
    }

    @Nested
    @DisplayName("定价确认 - price()")
    class PriceTests {

        @Test
        @DisplayName("定价确认成功 - 已截止询价单")
        void price_closedRfq_success() {
            PricingDTO dto = new PricingDTO();
            dto.setQuoteId(10L);
            dto.setRemark("定价确认备注");

            when(rfqMapper.selectById(1L)).thenReturn(closedRfq);
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(quoteMapper.updateById(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);
            when(purchaseOrderService.create(any(PurchaseOrderCreateDTO.class))).thenReturn(100L);
            when(quoteAwardMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                LoginUser loginUser = mock(LoginUser.class);
                when(loginUser.getUserId()).thenReturn(1L);
                when(loginUser.getRealName()).thenReturn("采购员张三");
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);

                rfqService.price(1L, dto);
            }

            // 验证报价状态更新为已定价
            assertEquals(QuoteStatusEnum.PRICED.getCode(), submittedQuote.getQuoteStatus());
            verify(quoteMapper).updateById(submittedQuote);

            // 验证询价单状态更新为已定价
            assertEquals(RfqStatusEnum.PRICED.getCode(), closedRfq.getRfqStatus());
            verify(rfqMapper).updateById(closedRfq);

            // 验证创建采购订单
            ArgumentCaptor<PurchaseOrderCreateDTO> orderCaptor = ArgumentCaptor.forClass(PurchaseOrderCreateDTO.class);
            verify(purchaseOrderService).create(orderCaptor.capture());
            PurchaseOrderCreateDTO orderDto = orderCaptor.getValue();
            assertEquals(100L, orderDto.getSupplierId());
            assertEquals(new BigDecimal("50000.00"), orderDto.getTotalAmount());
            assertTrue(orderDto.getRemark().contains("RFQ20260601001"));

            // 验证 QuoteAward 记录
            ArgumentCaptor<QuoteAward> awardCaptor = ArgumentCaptor.forClass(QuoteAward.class);
            verify(quoteAwardMapper).insert(awardCaptor.capture());
            QuoteAward award = awardCaptor.getValue();
            assertEquals(1L, award.getRfqId());
            assertEquals(10L, award.getQuoteId());
            assertEquals(100L, award.getSupplierId());
            assertEquals("定价确认备注", award.getRemark());

            // 验证回填订单号
            verify(quoteAwardMapper).updateById(any());
        }

        @Test
        @DisplayName("定价确认成功 - 报价中状态询价单")
        void price_quotingRfq_success() {
            closedRfq.setRfqStatus(RfqStatusEnum.QUOTING.getCode());
            PricingDTO dto = new PricingDTO();
            dto.setQuoteId(10L);

            when(rfqMapper.selectById(1L)).thenReturn(closedRfq);
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(quoteMapper.updateById(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);
            when(purchaseOrderService.create(any())).thenReturn(100L);
            when(quoteAwardMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                rfqService.price(1L, dto);
            }

            assertEquals(RfqStatusEnum.PRICED.getCode(), closedRfq.getRfqStatus());
        }

        @Test
        @DisplayName("定价确认 - 询价单不存在时抛出异常")
        void price_rfqNotFound_throws() {
            when(rfqMapper.selectById(999L)).thenReturn(null);
            PricingDTO dto = new PricingDTO();
            dto.setQuoteId(10L);

            assertThrows(BusinessException.class, () -> rfqService.price(999L, dto));
        }

        @Test
        @DisplayName("定价确认 - 询价单状态不允许时抛出异常")
        void price_rfqWrongStatus_throws() {
            closedRfq.setRfqStatus(RfqStatusEnum.DRAFT.getCode());
            when(rfqMapper.selectById(1L)).thenReturn(closedRfq);
            PricingDTO dto = new PricingDTO();
            dto.setQuoteId(10L);

            BusinessException ex = assertThrows(BusinessException.class, () -> rfqService.price(1L, dto));
            assertTrue(ex.getMessage().contains("当前状态不允许定价"));
        }

        @Test
        @DisplayName("定价确认 - 报价单不存在时抛出异常")
        void price_quoteNotFound_throws() {
            PricingDTO dto = new PricingDTO();
            dto.setQuoteId(999L);

            when(rfqMapper.selectById(1L)).thenReturn(closedRfq);
            when(quoteMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class, () -> rfqService.price(1L, dto));
            assertTrue(ex.getMessage().contains("报价单不存在"));
        }

        @Test
        @DisplayName("定价确认 - 报价不属于该询价单时抛出异常")
        void price_quoteNotBelongToRfq_throws() {
            submittedQuote.setRfqId(999L); // 属于另一个询价单
            PricingDTO dto = new PricingDTO();
            dto.setQuoteId(10L);

            when(rfqMapper.selectById(1L)).thenReturn(closedRfq);
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);

            BusinessException ex = assertThrows(BusinessException.class, () -> rfqService.price(1L, dto));
            assertTrue(ex.getMessage().contains("不属于该询价单"));
        }

        @Test
        @DisplayName("定价确认 - 报价非已提交状态时抛出异常")
        void price_quoteNotSubmitted_throws() {
            submittedQuote.setQuoteStatus(QuoteStatusEnum.DRAFT.getCode());
            PricingDTO dto = new PricingDTO();
            dto.setQuoteId(10L);

            when(rfqMapper.selectById(1L)).thenReturn(closedRfq);
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);

            BusinessException ex = assertThrows(BusinessException.class, () -> rfqService.price(1L, dto));
            assertTrue(ex.getMessage().contains("已提交"));
        }

        @Test
        @DisplayName("定价确认 - 不自动下发订单（区别于accept流程）")
        void price_doesNotAutoPublish() {
            PricingDTO dto = new PricingDTO();
            dto.setQuoteId(10L);

            when(rfqMapper.selectById(1L)).thenReturn(closedRfq);
            when(quoteMapper.selectById(10L)).thenReturn(submittedQuote);
            when(quoteAwardMapper.insert(any())).thenReturn(1);
            when(quoteMapper.updateById(any())).thenReturn(1);
            when(rfqMapper.updateById(any())).thenReturn(1);
            when(purchaseOrderService.create(any())).thenReturn(100L);
            when(quoteAwardMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                rfqService.price(1L, dto);
            }

            // price() 只创建订单，不调用 publish()
            verify(purchaseOrderService).create(any(PurchaseOrderCreateDTO.class));
            verify(purchaseOrderService, never()).publish(any(), any());
        }
    }

    @Nested
    @DisplayName("发布询价单 - publish()")
    class PublishTests {

        @Test
        @DisplayName("发布草稿询价单成功")
        void publish_draftRfq_success() {
            Rfq draftRfq = new Rfq();
            draftRfq.setId(1L);
            draftRfq.setRfqStatus(RfqStatusEnum.DRAFT.getCode());
            when(rfqMapper.selectById(1L)).thenReturn(draftRfq);
            when(rfqMapper.updateById(any())).thenReturn(1);

            rfqService.publish(1L);

            assertEquals(RfqStatusEnum.PUBLISHED.getCode(), draftRfq.getRfqStatus());
            assertNotNull(draftRfq.getPublishTime());
        }

        @Test
        @DisplayName("发布非草稿状态询价单抛出异常")
        void publish_wrongStatus_throws() {
            closedRfq.setRfqStatus(RfqStatusEnum.PUBLISHED.getCode());
            when(rfqMapper.selectById(1L)).thenReturn(closedRfq);

            assertThrows(BusinessException.class, () -> rfqService.publish(1L));
        }
    }

    @Nested
    @DisplayName("截止询价 - close()")
    class CloseTests {

        @Test
        @DisplayName("截止报价中询价单成功")
        void close_quotingRfq_success() {
            Rfq quotingRfq = new Rfq();
            quotingRfq.setId(1L);
            quotingRfq.setRfqStatus(RfqStatusEnum.QUOTING.getCode());
            when(rfqMapper.selectById(1L)).thenReturn(quotingRfq);
            when(rfqMapper.updateById(any())).thenReturn(1);

            rfqService.close(1L);

            assertEquals(RfqStatusEnum.CLOSED.getCode(), quotingRfq.getRfqStatus());
            assertNotNull(quotingRfq.getCloseTime());
        }
    }

    @Nested
    @DisplayName("取消询价 - cancel()")
    class CancelTests {

        @Test
        @DisplayName("取消草稿询价单成功")
        void cancel_draftRfq_success() {
            Rfq draftRfq = new Rfq();
            draftRfq.setId(1L);
            draftRfq.setRfqStatus(RfqStatusEnum.DRAFT.getCode());
            when(rfqMapper.selectById(1L)).thenReturn(draftRfq);
            when(rfqMapper.updateById(any())).thenReturn(1);

            rfqService.cancel(1L);

            assertEquals(RfqStatusEnum.CANCELED.getCode(), draftRfq.getRfqStatus());
        }
    }
}
