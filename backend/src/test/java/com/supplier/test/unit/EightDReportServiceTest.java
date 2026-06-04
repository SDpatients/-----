package com.supplier.test.unit;

import com.supplier.common.exception.BusinessException;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.quality.dto.EightDActionDTO;
import com.supplier.quality.dto.EightDReportCreateDTO;
import com.supplier.quality.entity.EightDReport;
import com.supplier.quality.enums.EightDStepEnum;
import com.supplier.quality.mapper.EightDReportMapper;
import com.supplier.quality.service.impl.EightDReportServiceImpl;
import com.supplier.security.util.SecurityUtils;
import com.supplier.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EightDReportServiceTest extends BaseUnitTest {

    @InjectMocks
    private EightDReportServiceImpl eightDReportService;

    @Mock
    private EightDReportMapper eightDReportMapper;

    @Mock
    private MessageNoticeService messageNoticeService;

    private EightDReport report;
    private EightDReportCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        report = new EightDReport();
        report.setId(1L);
        report.setReportNo("8D202601010001");
        report.setNcrId(10L);
        report.setSupplierId(100L);
        report.setCurrentStep(1);
        report.setReportStatus(0);
        report.setStepDueDate(LocalDate.now().plusDays(5));

        createDTO = new EightDReportCreateDTO();
        createDTO.setNcrId(10L);
        createDTO.setSupplierId(100L);
        createDTO.setD2Problem("表面划伤");
    }

    @Nested
    @DisplayName("创建8D报告")
    class CreateTests {

        @Test
        @DisplayName("创建8D报告成功，默认currentStep为1，dueDate为15天后")
        void create_success() {
            when(eightDReportMapper.selectCount(any())).thenReturn(0L);
            when(eightDReportMapper.insert(any())).thenAnswer(invocation -> {
                EightDReport r = invocation.getArgument(0);
                r.setId(1L);
                return 1;
            });

            Long id = eightDReportService.create(createDTO);

            assertNotNull(id);
            ArgumentCaptor<EightDReport> captor = ArgumentCaptor.forClass(EightDReport.class);
            verify(eightDReportMapper).insert(captor.capture());
            EightDReport inserted = captor.getValue();
            assertNotNull(inserted.getReportNo());
            assertEquals(1, inserted.getCurrentStep());
            assertEquals(LocalDate.now().plusDays(15), inserted.getDueDate());
            assertEquals(0, inserted.getReportStatus());
        }
    }

    @Nested
    @DisplayName("提交8D报告")
    class SubmitTests {

        @Test
        @DisplayName("草稿状态提交成功")
        void submit_success() {
            report.setReportStatus(0);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);
            when(eightDReportMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                eightDReportService.submit(1L, new EightDActionDTO());

                assertEquals(1, report.getReportStatus());
                assertNotNull(report.getSubmitTime());
                verify(eightDReportMapper).updateById(report);
            }
        }

        @Test
        @DisplayName("非草稿状态提交失败")
        void submit_wrongStatus_throws() {
            report.setReportStatus(2);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> eightDReportService.submit(1L, new EightDActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("审核8D报告")
    class AuditTests {

        @Test
        @DisplayName("已提交状态审核成功")
        void audit_success() {
            report.setReportStatus(1);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);
            when(eightDReportMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                eightDReportService.audit(1L, new EightDActionDTO());

                assertEquals(2, report.getReportStatus());
                assertNotNull(report.getAuditTime());
                verify(eightDReportMapper).updateById(report);
            }
        }

        @Test
        @DisplayName("非已提交状态审核失败")
        void audit_wrongStatus_throws() {
            report.setReportStatus(0);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> eightDReportService.audit(1L, new EightDActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("退回8D报告")
    class RejectTests {

        @Test
        @DisplayName("审核中状态退回成功")
        void reject_success() {
            report.setReportStatus(2);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);
            when(eightDReportMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                eightDReportService.reject(1L, new EightDActionDTO());

                assertEquals(3, report.getReportStatus());
                verify(eightDReportMapper).updateById(report);
            }
        }

        @Test
        @DisplayName("非审核中状态退回失败")
        void reject_wrongStatus_throws() {
            report.setReportStatus(1);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> eightDReportService.reject(1L, new EightDActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("关闭8D报告")
    class CloseTests {

        @Test
        @DisplayName("审核中状态关闭成功")
        void close_success() {
            report.setReportStatus(2);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);
            when(eightDReportMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                eightDReportService.close(1L, new EightDActionDTO());

                assertEquals(4, report.getReportStatus());
                assertNotNull(report.getCloseTime());
                verify(eightDReportMapper).updateById(report);
            }
        }

        @Test
        @DisplayName("非审核中状态关闭失败")
        void close_wrongStatus_throws() {
            report.setReportStatus(1);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> eightDReportService.close(1L, new EightDActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("提交阶段")
    class StepSubmitTests {

        @Test
        @DisplayName("审核中状态提交阶段成功")
        void stepSubmit_fromAuditing_success() {
            report.setReportStatus(2);
            report.setCurrentStep(1);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);
            when(eightDReportMapper.updateById(any())).thenReturn(1);

            EightDActionDTO dto = new EightDActionDTO();
            dto.setCurrentStep(2);
            dto.setStepContent("问题描述内容");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                eightDReportService.stepSubmit(1L, dto);

                assertEquals(2, report.getCurrentStep());
                assertEquals(2, report.getReportStatus());
                verify(eightDReportMapper).updateById(report);
            }
        }

        @Test
        @DisplayName("退回状态提交阶段后状态变为审核中")
        void stepSubmit_fromRejected_changesToAuditing() {
            report.setReportStatus(3);
            report.setCurrentStep(3);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);
            when(eightDReportMapper.updateById(any())).thenReturn(1);

            EightDActionDTO dto = new EightDActionDTO();
            dto.setCurrentStep(4);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                eightDReportService.stepSubmit(1L, dto);

                assertEquals(2, report.getReportStatus());
                assertEquals(4, report.getCurrentStep());
            }
        }

        @Test
        @DisplayName("提交D8阶段时自动关闭")
        void stepSubmit_step8_autoClose() {
            report.setReportStatus(2);
            report.setCurrentStep(7);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);
            when(eightDReportMapper.updateById(any())).thenReturn(1);

            EightDActionDTO dto = new EightDActionDTO();
            dto.setCurrentStep(8);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                eightDReportService.stepSubmit(1L, dto);

                assertEquals(4, report.getReportStatus());
                assertEquals(8, report.getCurrentStep());
                assertNotNull(report.getCloseTime());
            }
        }

        @Test
        @DisplayName("非审核中或退回状态提交阶段失败")
        void stepSubmit_wrongStatus_throws() {
            report.setReportStatus(0);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);

            EightDActionDTO dto = new EightDActionDTO();
            dto.setCurrentStep(2);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> eightDReportService.stepSubmit(1L, dto));
            }
        }
    }

    @Nested
    @DisplayName("审核阶段")
    class StepApproveTests {

        @Test
        @DisplayName("审核中状态审核阶段成功，推进到下一阶段")
        void stepApprove_success() {
            report.setReportStatus(2);
            report.setCurrentStep(1);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);
            when(eightDReportMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                eightDReportService.stepApprove(1L, new EightDActionDTO());

                assertEquals(EightDStepEnum.D2.getCode(), report.getCurrentStep());
                assertNotNull(report.getStepDueDate());
                verify(eightDReportMapper).updateById(report);
            }
        }

        @Test
        @DisplayName("非审核中状态审核阶段失败")
        void stepApprove_wrongStatus_throws() {
            report.setReportStatus(1);
            when(eightDReportMapper.selectById(1L)).thenReturn(report);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> eightDReportService.stepApprove(1L, new EightDActionDTO()));
            }
        }
    }
}
