package com.supplier.test.integration;

import com.supplier.test.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("结算模块集成测试")
class SettlementIntegrationTest extends BaseApiTest {

    @Nested
    @DisplayName("扣款单接口")
    class DeductionTests {

        @Test
        @DisplayName("分页查询扣款单")
        void pageQueryDeductions() throws Exception {
            mockMvc.perform(get("/v1/deductions")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("查询不存在的扣款单详情应返回错误")
        void getDeductionDetail_notFound() throws Exception {
            mockMvc.perform(get("/v1/deductions/999999")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("创建扣款单")
        void createDeduction() throws Exception {
            String body = "{" +
                    "\"deductionNo\":\"DED-IT-001\"," +
                    "\"supplierId\":1," +
                    "\"sourceType\":\"NCR\"," +
                    "\"sourceId\":1," +
                    "\"deductionType\":1," +
                    "\"deductionAmount\":500.00," +
                    "\"deductionReason\":\"质量不合格扣款\"" +
                    "}";
            mockMvc.perform(post("/v1/deductions")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("提交扣款单")
        void submitDeduction() throws Exception {
            String createBody = "{" +
                    "\"deductionNo\":\"DED-IT-SUB-001\"," +
                    "\"supplierId\":1," +
                    "\"sourceType\":\"NCR\"," +
                    "\"sourceId\":1," +
                    "\"deductionType\":1," +
                    "\"deductionAmount\":300.00," +
                    "\"deductionReason\":\"延迟交货扣款\"" +
                    "}";
            String response = mockMvc.perform(post("/v1/deductions")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(createBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn().getResponse().getContentAsString();

            Long id = objectMapper.readTree(response).get("data").asLong();
            mockMvc.perform(post("/v1/deductions/" + id + "/submit")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("发票接口")
    class InvoiceTests {

        @Test
        @DisplayName("创建发票")
        void createInvoice() throws Exception {
            String body = "{" +
                    "\"invoiceNo\":\"INV-IT-001\"," +
                    "\"invoiceCode\":\"CODE001\"," +
                    "\"invoiceType\":1," +
                    "\"supplierId\":1," +
                    "\"supplierName\":\"测试供应商\"," +
                    "\"taxNumber\":\"91110000MA01\"," +
                    "\"invoiceAmount\":10000.00," +
                    "\"taxAmount\":1300.00," +
                    "\"taxRate\":13.00," +
                    "\"invoiceDate\":\"2026-06-01\"" +
                    "}";
            mockMvc.perform(post("/v1/invoices")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("分页查询发票")
        void pageQueryInvoices() throws Exception {
            mockMvc.perform(get("/v1/invoices")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("付款单接口")
    class PaymentTests {

        @Test
        @DisplayName("创建付款单")
        void createPayment() throws Exception {
            String body = "{" +
                    "\"paymentNo\":\"PAY-IT-001\"," +
                    "\"supplierId\":1," +
                    "\"supplierName\":\"测试供应商\"," +
                    "\"paymentAmount\":10000.00," +
                    "\"paymentMethod\":1," +
                    "\"paymentAccount\":\"62220000001\"," +
                    "\"paymentBank\":\"工商银行\"," +
                    "\"receiveAccount\":\"62220000002\"," +
                    "\"receiveBank\":\"建设银行\"," +
                    "\"scheduleDate\":\"2026-06-15\"" +
                    "}";
            mockMvc.perform(post("/v1/payments")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("分页查询付款单")
        void pageQueryPayments() throws Exception {
            mockMvc.perform(get("/v1/payments")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("提交付款审批")
        void submitPaymentApproval() throws Exception {
            String createBody = "{" +
                    "\"paymentNo\":\"PAY-IT-APP-001\"," +
                    "\"supplierId\":1," +
                    "\"supplierName\":\"测试供应商\"," +
                    "\"paymentAmount\":5000.00," +
                    "\"paymentMethod\":1," +
                    "\"paymentAccount\":\"62220000001\"," +
                    "\"paymentBank\":\"工商银行\"," +
                    "\"receiveAccount\":\"62220000002\"," +
                    "\"receiveBank\":\"建设银行\"," +
                    "\"scheduleDate\":\"2026-06-20\"" +
                    "}";
            String response = mockMvc.perform(post("/v1/payments")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(createBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn().getResponse().getContentAsString();

            Long id = objectMapper.readTree(response).get("data").asLong();
            mockMvc.perform(post("/v1/payments/" + id + "/submit-approval")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
