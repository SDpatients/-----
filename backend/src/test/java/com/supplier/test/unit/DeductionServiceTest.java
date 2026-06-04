package com.supplier.test.unit;

import com.supplier.common.event.DomainEvent;
import com.supplier.common.event.DomainEventPublisher;
import com.supplier.common.exception.BusinessException;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.dto.DeductionActionDTO;
import com.supplier.settlement.dto.DeductionCreateDTO;
import com.supplier.settlement.entity.Deduction;
import com.supplier.settlement.enums.DeductionStatusEnum;
import com.supplier.settlement.mapper.DeductionMapper;
import com.supplier.settlement.mapper.ReconciliationMapper;
import com.supplier.settlement.service.ReconciliationDetailService;
import com.supplier.settlement.service.impl.DeductionServiceImpl;
import com.supplier.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DeductionServiceTest extends BaseUnitTest {

    @InjectMocks
    private DeductionServiceImpl deductionService;

    @Mock
    private DeductionMapper deductionMapper;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private ReconciliationMapper reconciliationMapper;

    @Mock
    private ReconciliationDetailService reconciliationDetailService;

    private Deduction deduction;
    private DeductionCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        deduction = new Deduction();
        deduction.setId(1L);
        deduction.setDeductionNo("DK20260101001");
        deduction.setSupplierId(100L);
        deduction.setDeductionAmount(BigDecimal.valueOf(500));
        deduction.setDeductionReason("质量扣款");

        createDTO = new DeductionCreateDTO();
        createDTO.setDeductionNo("DK20260101001");
        createDTO.setSupplierId(100L);
        createDTO.setSourceType("NCR");
        createDTO.setDeductionType(1);
        createDTO.setDeductionAmount(BigDecimal.valueOf(500));
        createDTO.setDeductionReason("质量扣款");
    }

    @Nested
    @DisplayName("创建扣款单")
    class CreateTests {

        @Test
        @DisplayName("创建扣款单成功")
        void create_success() {
            when(deductionMapper.selectCount(any())).thenReturn(0L);
            when(deductionMapper.insert(any())).thenAnswer(invocation -> {
                Deduction d = invocation.getArgument(0);
                d.setId(1L);
                return 1;
            });

            Long id = deductionService.create(createDTO);

            assertNotNull(id);
            verify(deductionMapper).insert(any());
        }

        @Test
        @DisplayName("扣款单号重复时抛出异常")
        void create_duplicate_throws() {
            when(deductionMapper.selectCount(any())).thenReturn(1L);

            assertThrows(BusinessException.class, () -> deductionService.create(createDTO));
        }
    }

    @Nested
    @DisplayName("提交扣款单")
    class SubmitTests {

        @Test
        @DisplayName("草稿状态提交成功")
        void submit_success() {
            deduction.setDeductionStatus(DeductionStatusEnum.DRAFT.getCode());
            when(deductionMapper.selectById(1L)).thenReturn(deduction);
            when(deductionMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                deductionService.submit(1L, new DeductionActionDTO());

                assertEquals(DeductionStatusEnum.SUBMITTED.getCode(), deduction.getDeductionStatus());
                verify(deductionMapper).updateById(deduction);
            }
        }

        @Test
        @DisplayName("非草稿状态提交失败")
        void submit_wrongStatus_throws() {
            deduction.setDeductionStatus(DeductionStatusEnum.CONFIRMED.getCode());
            when(deductionMapper.selectById(1L)).thenReturn(deduction);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> deductionService.submit(1L, new DeductionActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("确认扣款单")
    class ConfirmTests {

        @Test
        @DisplayName("已提交状态确认成功")
        void confirm_success() {
            deduction.setDeductionStatus(DeductionStatusEnum.SUBMITTED.getCode());
            when(deductionMapper.selectById(1L)).thenReturn(deduction);
            when(deductionMapper.updateById(any())).thenReturn(1);
            when(reconciliationMapper.selectOne(any())).thenReturn(null);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                deductionService.confirm(1L, new DeductionActionDTO());

                assertEquals(DeductionStatusEnum.CONFIRMED.getCode(), deduction.getDeductionStatus());
                verify(domainEventPublisher).publish(eq("supplier.settlement"), eq("settlement.deduction.confirmed"), any(DomainEvent.class));
            }
        }

        @Test
        @DisplayName("非已提交状态确认失败")
        void confirm_wrongStatus_throws() {
            deduction.setDeductionStatus(DeductionStatusEnum.DRAFT.getCode());
            when(deductionMapper.selectById(1L)).thenReturn(deduction);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> deductionService.confirm(1L, new DeductionActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("异议扣款单")
    class DisputeTests {

        @Test
        @DisplayName("已提交状态提异议成功")
        void dispute_success() {
            deduction.setDeductionStatus(DeductionStatusEnum.SUBMITTED.getCode());
            when(deductionMapper.selectById(1L)).thenReturn(deduction);
            when(deductionMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                deductionService.dispute(1L, new DeductionActionDTO());

                assertEquals(DeductionStatusEnum.DISPUTED.getCode(), deduction.getDeductionStatus());
                verify(deductionMapper).updateById(deduction);
            }
        }

        @Test
        @DisplayName("非已提交状态提异议失败")
        void dispute_wrongStatus_throws() {
            deduction.setDeductionStatus(DeductionStatusEnum.CONFIRMED.getCode());
            when(deductionMapper.selectById(1L)).thenReturn(deduction);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> deductionService.dispute(1L, new DeductionActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("入账扣款单")
    class BookTests {

        @Test
        @DisplayName("已确认状态入账成功")
        void book_success() {
            deduction.setDeductionStatus(DeductionStatusEnum.CONFIRMED.getCode());
            when(deductionMapper.selectById(1L)).thenReturn(deduction);
            when(deductionMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                deductionService.book(1L, new DeductionActionDTO());

                assertEquals(DeductionStatusEnum.BOOKED.getCode(), deduction.getDeductionStatus());
                verify(deductionMapper).updateById(deduction);
            }
        }

        @Test
        @DisplayName("非已确认状态入账失败")
        void book_wrongStatus_throws() {
            deduction.setDeductionStatus(DeductionStatusEnum.SUBMITTED.getCode());
            when(deductionMapper.selectById(1L)).thenReturn(deduction);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> deductionService.book(1L, new DeductionActionDTO()));
            }
        }
    }
}
