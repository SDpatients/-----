package com.supplier.test.unit;

import com.supplier.common.exception.BusinessException;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.dto.PaymentActionDTO;
import com.supplier.settlement.dto.PaymentCreateDTO;
import com.supplier.settlement.dto.PaymentScheduleDTO;
import com.supplier.settlement.entity.Payment;
import com.supplier.settlement.enums.PaymentStatusEnum;
import com.supplier.settlement.mapper.PaymentMapper;
import com.supplier.settlement.service.impl.PaymentServiceImpl;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest extends BaseUnitTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentMapper paymentMapper;

    private Payment payment;
    private PaymentCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setPaymentNo("PAY20260101001");
        payment.setSupplierId(100L);
        payment.setPaymentAmount(BigDecimal.valueOf(10000));
        payment.setPaymentStatus(PaymentStatusEnum.PENDING.getCode());

        createDTO = new PaymentCreateDTO();
        createDTO.setPaymentNo("PAY20260101001");
        createDTO.setSupplierId(100L);
        createDTO.setPaymentAmount(BigDecimal.valueOf(10000));
        createDTO.setScheduleDate(LocalDate.now().plusDays(30));
    }

    @Nested
    @DisplayName("创建付款单")
    class CreateTests {

        @Test
        @DisplayName("创建付款单成功")
        void create_success() {
            when(paymentMapper.selectCount(any())).thenReturn(0L);
            when(paymentMapper.insert(any())).thenAnswer(invocation -> {
                Payment p = invocation.getArgument(0);
                p.setId(1L);
                return 1;
            });

            Long id = paymentService.create(createDTO);

            assertNotNull(id);
            verify(paymentMapper).insert(any());
        }

        @Test
        @DisplayName("付款单号重复时抛出异常")
        void create_duplicate_throws() {
            when(paymentMapper.selectCount(any())).thenReturn(1L);

            assertThrows(BusinessException.class, () -> paymentService.create(createDTO));
        }
    }

    @Nested
    @DisplayName("提交审批")
    class SubmitForApprovalTests {

        @Test
        @DisplayName("待付款状态提交审批成功")
        void submitForApproval_success() {
            payment.setPaymentStatus(PaymentStatusEnum.PENDING.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);
            when(paymentMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                paymentService.submitForApproval(1L);

                assertEquals(PaymentStatusEnum.APPROVING.getCode(), payment.getPaymentStatus());
                verify(paymentMapper).updateById(payment);
            }
        }

        @Test
        @DisplayName("非待付款状态提交审批失败")
        void submitForApproval_wrongStatus_throws() {
            payment.setPaymentStatus(PaymentStatusEnum.APPROVED.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> paymentService.submitForApproval(1L));
            }
        }
    }

    @Nested
    @DisplayName("付款排期")
    class ScheduleTests {

        @Test
        @DisplayName("已审批状态排期成功")
        void schedule_success() {
            payment.setPaymentStatus(PaymentStatusEnum.APPROVED.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);
            when(paymentMapper.updateById(any())).thenReturn(1);

            PaymentScheduleDTO dto = new PaymentScheduleDTO();
            dto.setScheduleDate(LocalDate.now().plusDays(15));
            dto.setPaymentTerms("月结30天");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                paymentService.schedule(1L, dto);

                assertEquals(PaymentStatusEnum.SCHEDULED.getCode(), payment.getPaymentStatus());
                assertEquals(LocalDate.now().plusDays(15), payment.getScheduleDate());
                assertEquals("月结30天", payment.getPaymentTerms());
                verify(paymentMapper).updateById(payment);
            }
        }

        @Test
        @DisplayName("非已审批状态排期失败")
        void schedule_wrongStatus_throws() {
            payment.setPaymentStatus(PaymentStatusEnum.PENDING.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);

            PaymentScheduleDTO dto = new PaymentScheduleDTO();
            dto.setScheduleDate(LocalDate.now().plusDays(15));

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> paymentService.schedule(1L, dto));
            }
        }
    }

    @Nested
    @DisplayName("付款")
    class PayTests {

        @Test
        @DisplayName("已审批状态付款成功")
        void pay_fromApproved_success() {
            payment.setPaymentStatus(PaymentStatusEnum.APPROVED.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);
            when(paymentMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                paymentService.pay(1L, new PaymentActionDTO());

                assertEquals(PaymentStatusEnum.PAID.getCode(), payment.getPaymentStatus());
                assertNotNull(payment.getPaymentTime());
                verify(paymentMapper).updateById(payment);
            }
        }

        @Test
        @DisplayName("已排期状态付款成功")
        void pay_fromScheduled_success() {
            payment.setPaymentStatus(PaymentStatusEnum.SCHEDULED.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);
            when(paymentMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                paymentService.pay(1L, new PaymentActionDTO());

                assertEquals(PaymentStatusEnum.PAID.getCode(), payment.getPaymentStatus());
                assertNotNull(payment.getPaymentTime());
            }
        }

        @Test
        @DisplayName("非已审批或已排期状态付款失败")
        void pay_wrongStatus_throws() {
            payment.setPaymentStatus(PaymentStatusEnum.PENDING.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> paymentService.pay(1L, new PaymentActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("拒绝付款")
    class RejectTests {

        @Test
        @DisplayName("待付款状态拒绝成功")
        void reject_fromPending_success() {
            payment.setPaymentStatus(PaymentStatusEnum.PENDING.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);
            when(paymentMapper.updateById(any())).thenReturn(1);

            PaymentActionDTO dto = new PaymentActionDTO();
            dto.setRemark("资料不全");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                paymentService.reject(1L, dto);

                assertEquals(PaymentStatusEnum.REJECTED.getCode(), payment.getPaymentStatus());
                assertEquals("资料不全", payment.getRemark());
            }
        }

        @Test
        @DisplayName("审批中状态拒绝成功")
        void reject_fromApproving_success() {
            payment.setPaymentStatus(PaymentStatusEnum.APPROVING.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);
            when(paymentMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                paymentService.reject(1L, new PaymentActionDTO());

                assertEquals(PaymentStatusEnum.REJECTED.getCode(), payment.getPaymentStatus());
            }
        }

        @Test
        @DisplayName("非待付款或审批中状态拒绝失败")
        void reject_wrongStatus_throws() {
            payment.setPaymentStatus(PaymentStatusEnum.APPROVED.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> paymentService.reject(1L, new PaymentActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("取消付款")
    class CancelTests {

        @Test
        @DisplayName("待付款状态取消成功")
        void cancel_success() {
            payment.setPaymentStatus(PaymentStatusEnum.PENDING.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);
            when(paymentMapper.updateById(any())).thenReturn(1);

            PaymentActionDTO dto = new PaymentActionDTO();
            dto.setRemark("订单取消");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                paymentService.cancel(1L, dto);

                assertEquals(PaymentStatusEnum.CANCELLED.getCode(), payment.getPaymentStatus());
                assertEquals("订单取消", payment.getRemark());
            }
        }

        @Test
        @DisplayName("已付款状态不能取消")
        void cancel_paid_throws() {
            payment.setPaymentStatus(PaymentStatusEnum.PAID.getCode());
            when(paymentMapper.selectById(1L)).thenReturn(payment);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> paymentService.cancel(1L, new PaymentActionDTO()));
            }
        }
    }
}
