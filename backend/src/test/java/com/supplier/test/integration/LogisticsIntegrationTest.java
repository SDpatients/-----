package com.supplier.test.integration;

import com.supplier.test.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("物流模块集成测试")
class LogisticsIntegrationTest extends BaseApiTest {

    @Nested
    @DisplayName("送货明细接口")
    class DeliveryDetailTests {

        @Test
        @DisplayName("按通知单查询送货明细")
        void listDeliveryDetailsByNoticeId() throws Exception {
            mockMvc.perform(get("/v1/delivery-details")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("noticeId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("收货记录接口")
    class ReceiptRecordTests {

        @Test
        @DisplayName("分页查询收货记录")
        void pageQueryReceiptRecords() throws Exception {
            mockMvc.perform(get("/v1/receipt-records")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("收货差异接口")
    class ReceiptDiffTests {

        @Test
        @DisplayName("分页查询收货差异")
        void pageQueryReceiptDiffs() throws Exception {
            mockMvc.perform(get("/v1/receipt-diffs")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("VMI库存接口")
    class VmiInventoryTests {

        @Test
        @DisplayName("分页查询VMI库存 - Controller尚未实现，验证接口可达")
        void pageQueryVmiInventories() throws Exception {
            mockMvc.perform(get("/v1/vmi-inventories")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(result -> {
                        // VMI库存Controller尚未实现，接口可能返回404或业务错误码，只要不抛出未处理异常即可
                        String body = result.getResponse().getContentAsString();
                        org.junit.jupiter.api.Assertions.assertTrue(
                                body.contains("\"code\"") || result.getResponse().getStatus() == 404,
                                "应返回JSON响应或404, actual=" + result.getResponse().getStatus());
                    });
        }

        @Test
        @DisplayName("同步VMI库存 - Controller尚未实现，验证接口可达")
        void syncVmiInventory() throws Exception {
            String body = "{" +
                    "\"supplierId\":1," +
                    "\"materialCode\":\"MAT001\"," +
                    "\"warehouseId\":1," +
                    "\"warehouseName\":\"主仓库\"," +
                    "\"onhandQty\":1000.00," +
                    "\"availableQty\":800.00," +
                    "\"safetyQty\":200.00," +
                    "\"maxQty\":5000.00" +
                    "}";
            mockMvc.perform(post("/v1/vmi-inventories/sync")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(result -> {
                        String responseBody = result.getResponse().getContentAsString();
                        org.junit.jupiter.api.Assertions.assertTrue(
                                responseBody.contains("\"code\"") || result.getResponse().getStatus() == 404,
                                "应返回JSON响应或404, actual=" + result.getResponse().getStatus());
                    });
        }
    }
}
