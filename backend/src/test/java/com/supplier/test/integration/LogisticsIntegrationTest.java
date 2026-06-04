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
        @DisplayName("分页查询VMI库存")
        void pageQueryVmiInventories() throws Exception {
            mockMvc.perform(get("/v1/vmi-inventories")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("同步VMI库存")
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
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
