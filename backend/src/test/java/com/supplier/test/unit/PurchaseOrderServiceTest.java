package com.supplier.test.unit;

import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.exception.BusinessException;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.order.dto.BuyerConfirmDTO;
import com.supplier.order.dto.OrderActionDTO;
import com.supplier.order.dto.OrderCloseDTO;
import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.entity.PurchaseOrder;
import com.supplier.order.entity.PurchaseOrderDetail;
import com.supplier.order.mapper.OrderTrackMapper;
import com.supplier.order.mapper.PurchaseOrderDetailMapper;
import com.supplier.order.mapper.PurchaseOrderMapper;
import com.supplier.order.service.impl.PurchaseOrderServiceImpl;
import com.supplier.portal.dto.PortalTodoCreateDTO;
import com.supplier.portal.service.PortalTodoService;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import com.supplier.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PurchaseOrderServiceTest extends BaseUnitTest {

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    @Mock
    private PurchaseOrderMapper purchaseOrderMapper;

    @Mock
    private OrderTrackMapper orderTrackMapper;

    @Mock
    private PurchaseOrderDetailMapper detailMapper;

    @Mock
    private PortalTodoService portalTodoService;

    @Mock
    private MessageNoticeService messageNoticeService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    private PurchaseOrder order;
    private PurchaseOrderCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        order = new PurchaseOrder();
        order.setId(1L);
        order.setOrderNo("PO20260101001");
        order.setSupplierId(100L);
        order.setDeliveryDate(LocalDate.now().plusDays(30));
        order.setTotalAmount(BigDecimal.valueOf(10000));

        createDTO = new PurchaseOrderCreateDTO();
        createDTO.setOrderNo("PO20260101001");
        createDTO.setSupplierId(100L);
        createDTO.setOrderDate(LocalDate.now());
    }

    @Nested
    @DisplayName("创建订单")
    class CreateTests {

        @Test
        @DisplayName("创建订单成功")
        void create_success() {
            when(purchaseOrderMapper.selectCount(any())).thenReturn(0L);
            when(purchaseOrderMapper.insert(any())).thenAnswer(invocation -> {
                PurchaseOrder o = invocation.getArgument(0);
                o.setId(1L);
                return 1;
            });
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                LoginUser loginUser = mock(LoginUser.class);
                when(loginUser.getUserId()).thenReturn(1L);
                when(loginUser.getRealName()).thenReturn("张三");
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                Long id = purchaseOrderService.create(createDTO);

                assertNotNull(id);
                verify(purchaseOrderMapper).insert(any());
                verify(orderTrackMapper).insert(any());
            }
        }

        @Test
        @DisplayName("订单号重复时抛出异常")
        void create_duplicateOrderNo_throws() {
            when(purchaseOrderMapper.selectCount(any())).thenReturn(1L);
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                assertThrows(BusinessException.class, () -> purchaseOrderService.create(createDTO));
            }
        }
    }

    @Nested
    @DisplayName("下发订单")
    class PublishTests {

        @Test
        @DisplayName("下发订单成功")
        void publish_success() {
            order.setOrderStatus(0);
            when(purchaseOrderMapper.selectById(1L)).thenReturn(order);
            when(purchaseOrderMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                purchaseOrderService.publish(1L, new OrderActionDTO());

                assertEquals(1, order.getOrderStatus());
                verify(portalTodoService).create(any(PortalTodoCreateDTO.class));
                verify(messageNoticeService).create(any(MessageNoticeCreateDTO.class));
                verify(domainEventPublisher).publish(eq("supplier.order"), eq("order.purchase.published"), any(DomainEvent.class));
            }
        }

        @Test
        @DisplayName("订单状态不正确时下发失败")
        void publish_wrongStatus_throws() {
            order.setOrderStatus(2);
            when(purchaseOrderMapper.selectById(1L)).thenReturn(order);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> purchaseOrderService.publish(1L, new OrderActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("供应商确认订单")
    class ConfirmTests {

        @Test
        @DisplayName("确认订单成功")
        void confirm_success() {
            order.setOrderStatus(1);
            when(purchaseOrderMapper.selectById(1L)).thenReturn(order);
            when(purchaseOrderMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                purchaseOrderService.confirm(1L, new OrderActionDTO());

                assertEquals(2, order.getOrderStatus());
                assertNotNull(order.getConfirmBy());
                assertNotNull(order.getConfirmTime());
                verify(domainEventPublisher).publish(eq("supplier.order"), eq("order.purchase.confirmed"), any(DomainEvent.class));
            }
        }

        @Test
        @DisplayName("订单状态不正确时确认失败")
        void confirm_wrongStatus_throws() {
            order.setOrderStatus(0);
            when(purchaseOrderMapper.selectById(1L)).thenReturn(order);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> purchaseOrderService.confirm(1L, new OrderActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("取消订单")
    class CancelTests {

        @Test
        @DisplayName("草稿状态取消成功")
        void cancel_fromDraft_success() {
            order.setOrderStatus(0);
            when(purchaseOrderMapper.selectById(1L)).thenReturn(order);
            when(purchaseOrderMapper.updateById(any())).thenReturn(1);

            OrderActionDTO dto = new OrderActionDTO();
            dto.setRemark("取消原因");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                purchaseOrderService.cancel(1L, dto);

                assertEquals(5, order.getOrderStatus());
                assertEquals("取消原因", order.getCancelReason());
                assertNotNull(order.getCancelTime());
                verify(domainEventPublisher).publish(eq("supplier.order"), eq("order.purchase.canceled"), any(DomainEvent.class));
            }
        }

        @Test
        @DisplayName("已确认状态取消成功")
        void cancel_fromConfirmed_success() {
            order.setOrderStatus(2);
            when(purchaseOrderMapper.selectById(1L)).thenReturn(order);
            when(purchaseOrderMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                purchaseOrderService.cancel(1L, new OrderActionDTO());

                assertEquals(5, order.getOrderStatus());
                assertNotNull(order.getCancelTime());
            }
        }

        @Test
        @DisplayName("已完成状态取消失败")
        void cancel_fromCompleted_throws() {
            order.setOrderStatus(4);
            when(purchaseOrderMapper.selectById(1L)).thenReturn(order);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> purchaseOrderService.cancel(1L, new OrderActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("关闭订单")
    class CloseTests {

        @Test
        @DisplayName("所有明细收货完成时关闭成功")
        void close_withAllReceived_success() {
            order.setOrderStatus(4);
            when(purchaseOrderMapper.selectById(1L)).thenReturn(order);
            when(purchaseOrderMapper.updateById(any())).thenReturn(1);

            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setMaterialCode("MAT001");
            detail.setQuantity(BigDecimal.TEN);
            detail.setReceivedQty(BigDecimal.TEN);
            when(detailMapper.selectList(any())).thenReturn(Collections.singletonList(detail));

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(null);

                purchaseOrderService.close(1L, new OrderCloseDTO());

                assertNotNull(order.getCompleteTime());
                verify(domainEventPublisher).publish(eq("supplier.order"), eq("order.purchase.closed"), any(DomainEvent.class));
            }
        }

        @Test
        @DisplayName("收货数量不足时关闭失败")
        void close_withInsufficientReceived_throws() {
            order.setOrderStatus(4);
            when(purchaseOrderMapper.selectById(1L)).thenReturn(order);

            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setMaterialCode("MAT001");
            detail.setQuantity(BigDecimal.TEN);
            detail.setReceivedQty(BigDecimal.ONE);
            when(detailMapper.selectList(any())).thenReturn(Collections.singletonList(detail));

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> purchaseOrderService.close(1L, new OrderCloseDTO()));
            }
        }
    }

    @Nested
    @DisplayName("校验发货数量")
    class ValidateDeliveryQuantityTests {

        @Test
        @DisplayName("发货数量在限制内校验通过")
        void validateDeliveryQuantity_withinLimit_success() {
            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setQuantity(BigDecimal.TEN);
            detail.setDeliveredQty(BigDecimal.valueOf(5));
            when(detailMapper.selectById(1L)).thenReturn(detail);

            assertDoesNotThrow(() -> purchaseOrderService.validateDeliveryQuantity(1L, BigDecimal.valueOf(3)));
        }

        @Test
        @DisplayName("发货数量超出限制抛出异常")
        void validateDeliveryQuantity_exceedsLimit_throws() {
            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setQuantity(BigDecimal.TEN);
            detail.setDeliveredQty(BigDecimal.valueOf(8));
            when(detailMapper.selectById(1L)).thenReturn(detail);

            assertThrows(BusinessException.class, () -> purchaseOrderService.validateDeliveryQuantity(1L, BigDecimal.valueOf(5)));
        }
    }
}
