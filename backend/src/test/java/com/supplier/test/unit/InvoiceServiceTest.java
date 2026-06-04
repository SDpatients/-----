package com.supplier.test.unit;

import com.supplier.common.exception.BusinessException;
import com.supplier.security.util.SecurityUtils;
import com.supplier.settlement.dto.InvoiceActionDTO;
import com.supplier.settlement.dto.InvoiceCreateDTO;
import com.supplier.settlement.dto.InvoiceUploadDTO;
import com.supplier.settlement.entity.Invoice;
import com.supplier.settlement.enums.InvoiceStatusEnum;
import com.supplier.settlement.mapper.InvoiceMapper;
import com.supplier.settlement.service.impl.InvoiceServiceImpl;
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

class InvoiceServiceTest extends BaseUnitTest {

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Mock
    private InvoiceMapper invoiceMapper;

    private Invoice invoice;
    private InvoiceCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setInvoiceNo("INV20260101001");
        invoice.setSupplierId(100L);
        invoice.setInvoiceAmount(BigDecimal.valueOf(10000));
        invoice.setInvoiceStatus(InvoiceStatusEnum.PENDING.getCode());

        createDTO = new InvoiceCreateDTO();
        createDTO.setInvoiceNo("INV20260101001");
        createDTO.setSupplierId(100L);
        createDTO.setInvoiceAmount(BigDecimal.valueOf(10000));
        createDTO.setTaxRate(BigDecimal.valueOf(0.13));
        createDTO.setInvoiceDate(LocalDate.now());
    }

    @Nested
    @DisplayName("创建发票")
    class CreateTests {

        @Test
        @DisplayName("创建发票成功")
        void create_success() {
            when(invoiceMapper.selectCount(any())).thenReturn(0L);
            when(invoiceMapper.insert(any())).thenAnswer(invocation -> {
                Invoice i = invocation.getArgument(0);
                i.setId(1L);
                return 1;
            });

            Long id = invoiceService.create(createDTO);

            assertNotNull(id);
            verify(invoiceMapper).insert(any());
        }

        @Test
        @DisplayName("发票号码重复时抛出异常")
        void create_duplicate_throws() {
            when(invoiceMapper.selectCount(any())).thenReturn(1L);

            assertThrows(BusinessException.class, () -> invoiceService.create(createDTO));
        }
    }

    @Nested
    @DisplayName("上传发票")
    class UploadTests {

        @Test
        @DisplayName("待开票状态上传成功")
        void upload_success() {
            invoice.setInvoiceStatus(InvoiceStatusEnum.PENDING.getCode());
            when(invoiceMapper.selectById(1L)).thenReturn(invoice);
            when(invoiceMapper.updateById(any())).thenReturn(1);

            InvoiceUploadDTO uploadDTO = new InvoiceUploadDTO();
            uploadDTO.setFileId(100L);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                invoiceService.upload(1L, uploadDTO);

                assertEquals(InvoiceStatusEnum.UPLOADED.getCode(), invoice.getInvoiceStatus());
                assertEquals(100L, invoice.getFileId());
                assertNotNull(invoice.getReceiveTime());
                verify(invoiceMapper).updateById(invoice);
            }
        }

        @Test
        @DisplayName("非待开票状态上传失败")
        void upload_wrongStatus_throws() {
            invoice.setInvoiceStatus(InvoiceStatusEnum.VERIFIED.getCode());
            when(invoiceMapper.selectById(1L)).thenReturn(invoice);

            InvoiceUploadDTO uploadDTO = new InvoiceUploadDTO();
            uploadDTO.setFileId(100L);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> invoiceService.upload(1L, uploadDTO));
            }
        }
    }

    @Nested
    @DisplayName("验真发票")
    class VerifyTests {

        @Test
        @DisplayName("已上传状态验真成功")
        void verify_success() {
            invoice.setInvoiceStatus(InvoiceStatusEnum.UPLOADED.getCode());
            when(invoiceMapper.selectById(1L)).thenReturn(invoice);
            when(invoiceMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                invoiceService.verify(1L, new InvoiceActionDTO());

                assertEquals(InvoiceStatusEnum.VERIFIED.getCode(), invoice.getInvoiceStatus());
                verify(invoiceMapper).updateById(invoice);
            }
        }

        @Test
        @DisplayName("非已上传状态验真失败")
        void verify_wrongStatus_throws() {
            invoice.setInvoiceStatus(InvoiceStatusEnum.PENDING.getCode());
            when(invoiceMapper.selectById(1L)).thenReturn(invoice);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> invoiceService.verify(1L, new InvoiceActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("认证发票")
    class CertifyTests {

        @Test
        @DisplayName("已验真状态认证成功")
        void certify_success() {
            invoice.setInvoiceStatus(InvoiceStatusEnum.VERIFIED.getCode());
            when(invoiceMapper.selectById(1L)).thenReturn(invoice);
            when(invoiceMapper.updateById(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                invoiceService.certify(1L, new InvoiceActionDTO());

                assertEquals(InvoiceStatusEnum.CERTIFIED.getCode(), invoice.getInvoiceStatus());
                assertNotNull(invoice.getCertifyTime());
                verify(invoiceMapper).updateById(invoice);
            }
        }

        @Test
        @DisplayName("非已验真状态认证失败")
        void certify_wrongStatus_throws() {
            invoice.setInvoiceStatus(InvoiceStatusEnum.UPLOADED.getCode());
            when(invoiceMapper.selectById(1L)).thenReturn(invoice);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> invoiceService.certify(1L, new InvoiceActionDTO()));
            }
        }
    }

    @Nested
    @DisplayName("作废发票")
    class VoidInvoiceTests {

        @Test
        @DisplayName("非已作废状态作废成功")
        void voidInvoice_success() {
            invoice.setInvoiceStatus(InvoiceStatusEnum.PENDING.getCode());
            when(invoiceMapper.selectById(1L)).thenReturn(invoice);
            when(invoiceMapper.updateById(any())).thenReturn(1);

            InvoiceActionDTO dto = new InvoiceActionDTO();
            dto.setRemark("开票错误");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                invoiceService.voidInvoice(1L, dto);

                assertEquals(InvoiceStatusEnum.VOIDED.getCode(), invoice.getInvoiceStatus());
                assertNotNull(invoice.getVoidTime());
                assertEquals("开票错误", invoice.getVoidReason());
                verify(invoiceMapper).updateById(invoice);
            }
        }

        @Test
        @DisplayName("已作废状态不能再作废")
        void voidInvoice_alreadyVoided_throws() {
            invoice.setInvoiceStatus(InvoiceStatusEnum.VOIDED.getCode());
            when(invoiceMapper.selectById(1L)).thenReturn(invoice);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);

                assertThrows(BusinessException.class, () -> invoiceService.voidInvoice(1L, new InvoiceActionDTO()));
            }
        }
    }
}
