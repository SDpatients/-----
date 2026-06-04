package com.supplier.test.integration;

import com.supplier.test.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("门户与消息模块集成测试")
class PortalMessageIntegrationTest extends BaseApiTest {

    @Nested
    @DisplayName("仪表盘接口")
    class DashboardTests {

        @Test
        @DisplayName("获取仪表盘指标")
        void getDashboardMetrics() throws Exception {
            mockMvc.perform(get("/v1/dashboard/metrics")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("获取趋势数据")
        void getDashboardTrends() throws Exception {
            mockMvc.perform(get("/v1/dashboard/trends")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("获取风险数据")
        void getDashboardRisks() throws Exception {
            mockMvc.perform(get("/v1/dashboard/risks")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("待办接口")
    class TodoTests {

        @Test
        @DisplayName("分页查询待办")
        void pageQueryTodos() throws Exception {
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("获取未读待办数")
        void getUnreadCount() throws Exception {
            mockMvc.perform(get("/v1/todos/unread-count")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("创建待办")
        void createTodo() throws Exception {
            String body = "{" +
                    "\"userId\":1," +
                    "\"supplierId\":1," +
                    "\"todoType\":\"ORDER\"," +
                    "\"businessType\":\"PURCHASE_ORDER\"," +
                    "\"businessId\":1," +
                    "\"businessNo\":\"PO-IT-001\"," +
                    "\"title\":\"新订单待确认\"" +
                    "}";
            mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("消息接口")
    class MessageTests {

        @Test
        @DisplayName("分页查询消息")
        void pageQueryMessages() throws Exception {
            mockMvc.perform(get("/v1/messages")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("创建消息")
        void createMessage() throws Exception {
            String body = "{" +
                    "\"receiverUserId\":1," +
                    "\"receiverSupplierId\":1," +
                    "\"channel\":1," +
                    "\"title\":\"订单状态变更通知\"," +
                    "\"content\":\"您的订单PO-001已确认\"," +
                    "\"businessType\":\"PURCHASE_ORDER\"," +
                    "\"businessId\":1" +
                    "}";
            mockMvc.perform(post("/v1/messages")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("获取未读消息数")
        void getUnreadCount() throws Exception {
            mockMvc.perform(get("/v1/messages/unread-count")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("消息模板接口")
    class MessageTemplateTests {

        @Test
        @DisplayName("分页查询消息模板")
        void pageQueryMessageTemplates() throws Exception {
            mockMvc.perform(get("/v1/message-templates")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }
}
