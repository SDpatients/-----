package com.supplier.test.integration;

import com.supplier.test.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("待办事项模块集成测试")
class PortalTodoIntegrationTest extends BaseApiTest {

    // ==================== 创建待办 ====================

    @Nested
    @DisplayName("创建待办 - POST /v1/todos")
    class CreateTodoTests {

        @Test
        @DisplayName("创建采购方待办成功")
        void create_buyerTodo_success() throws Exception {
            String body = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"delivery_receive\"," +
                    "\"businessType\":\"delivery_notice\"," +
                    "\"businessId\":100," +
                    "\"businessNo\":\"ASN001\"," +
                    "\"title\":\"待收货通知单ASN001\"" +
                    "}";

            mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("创建供应商待办成功")
        void create_supplierTodo_success() throws Exception {
            String body = "{" +
                    "\"supplierId\":1," +
                    "\"todoType\":\"order_confirm\"," +
                    "\"businessType\":\"purchase_order\"," +
                    "\"businessId\":200," +
                    "\"businessNo\":\"PO001\"," +
                    "\"title\":\"待确认采购订单PO001\"" +
                    "}";

            mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("创建待办 - todoType为空时返回校验错误")
        void create_emptyTodoType_validationError() throws Exception {
            String body = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"\"," +
                    "\"businessType\":\"purchase_order\"," +
                    "\"businessId\":1," +
                    "\"title\":\"测试\"" +
                    "}";

            mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("创建待办 - businessType为空时返回校验错误")
        void create_emptyBusinessType_validationError() throws Exception {
            String body = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"order_confirm\"," +
                    "\"businessType\":\"\"," +
                    "\"businessId\":1," +
                    "\"title\":\"测试\"" +
                    "}";

            mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("创建待办 - businessId为空时返回校验错误")
        void create_nullBusinessId_validationError() throws Exception {
            String body = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"order_confirm\"," +
                    "\"businessType\":\"purchase_order\"," +
                    "\"title\":\"测试\"" +
                    "}";

            mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("创建待办 - title为空时返回校验错误")
        void create_emptyTitle_validationError() throws Exception {
            String body = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"order_confirm\"," +
                    "\"businessType\":\"purchase_order\"," +
                    "\"businessId\":1," +
                    "\"title\":\"\"" +
                    "}";

            mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("创建待办 - 带截止时间")
        void create_withDueTime_success() throws Exception {
            String body = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"rfq_deadline\"," +
                    "\"businessType\":\"rfq\"," +
                    "\"businessId\":300," +
                    "\"businessNo\":\"RFQ001\"," +
                    "\"title\":\"询价单即将截止RFQ001\"," +
                    "\"dueTime\":\"2026-06-10T18:00:00\"" +
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

    // ==================== 分页查询待办 ====================

    @Nested
    @DisplayName("分页查询待办 - GET /v1/todos")
    class PageTodoTests {

        @Test
        @DisplayName("分页查询 - 默认参数")
        void page_defaultParams() throws Exception {
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.total").isNumber());
        }

        @Test
        @DisplayName("分页查询 - 指定分页参数")
        void page_withPagination() throws Exception {
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("分页查询 - 按todoStatus过滤")
        void page_filterByStatus() throws Exception {
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("todoStatus", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("分页查询 - 按todoType过滤")
        void page_filterByTodoType() throws Exception {
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("todoType", "order_confirm"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("分页查询 - 按businessType过滤")
        void page_filterByBusinessType() throws Exception {
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("businessType", "purchase_order"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }
    }

    // ==================== 未读待办数 ====================

    @Nested
    @DisplayName("未读待办数 - GET /v1/todos/unread-count")
    class UnreadCountTests {

        @Test
        @DisplayName("采购方获取未读待办数")
        void unreadCount_buyer() throws Exception {
            mockMvc.perform(get("/v1/todos/unread-count")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("供应商获取未读待办数")
        void unreadCount_supplier() throws Exception {
            mockMvc.perform(get("/v1/todos/unread-count")
                            .header("Authorization", "Bearer " + supplierToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNumber());
        }
    }

    // ==================== 完成待办 ====================

    @Nested
    @DisplayName("完成待办 - POST /v1/todos/{id}/finish")
    class FinishTodoTests {

        @Test
        @DisplayName("完成待办 - 先创建再完成")
        void finish_success() throws Exception {
            // 先创建待办
            String createBody = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"order_confirm\"," +
                    "\"businessType\":\"purchase_order\"," +
                    "\"businessId\":999," +
                    "\"businessNo\":\"PO-FINISH-001\"," +
                    "\"title\":\"待完成测试订单\"" +
                    "}";

            String createResponse = mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(createBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn().getResponse().getContentAsString();

            String todoId = objectMapper.readTree(createResponse).get("data").asText();

            // 完成待办
            mockMvc.perform(post("/v1/todos/{id}/finish", todoId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证查询已完成待办
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("todoStatus", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("完成不存在的待办返回错误")
        void finish_notFound() throws Exception {
            mockMvc.perform(post("/v1/todos/{id}/finish", 999999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    // ==================== 忽略待办 ====================

    @Nested
    @DisplayName("忽略待办 - POST /v1/todos/{id}/ignore")
    class IgnoreTodoTests {

        @Test
        @DisplayName("忽略待办 - 先创建再忽略")
        void ignore_success() throws Exception {
            // 先创建待办
            String createBody = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"rfq_quote\"," +
                    "\"businessType\":\"rfq\"," +
                    "\"businessId\":888," +
                    "\"businessNo\":\"RFQ-IGNORE-001\"," +
                    "\"title\":\"待忽略测试询价单\"" +
                    "}";

            String createResponse = mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(createBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn().getResponse().getContentAsString();

            String todoId = objectMapper.readTree(createResponse).get("data").asText();

            // 忽略待办
            mockMvc.perform(post("/v1/todos/{id}/ignore", todoId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("忽略不存在的待办返回错误")
        void ignore_notFound() throws Exception {
            mockMvc.perform(post("/v1/todos/{id}/ignore", 999999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    // ==================== 数据权限隔离 ====================

    @Nested
    @DisplayName("数据权限隔离")
    class DataIsolationTests {

        @Test
        @DisplayName("供应商只能看到自己供应商的待办")
        void supplierOnlySeesOwnTodos() throws Exception {
            // 为供应商1创建待办
            String createBody = "{" +
                    "\"supplierId\":1," +
                    "\"todoType\":\"order_confirm\"," +
                    "\"businessType\":\"purchase_order\"," +
                    "\"businessId\":500," +
                    "\"businessNo\":\"PO-ISO-001\"," +
                    "\"title\":\"供应商1的待办\"" +
                    "}";

            mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(createBody))
                    .andExpect(status().isOk());

            // 供应商1查询待办，应该能看到
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + supplierToken)
                            .param("todoStatus", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("供应商2看不到供应商1的待办")
        void supplier2CannotSeeSupplier1Todos() throws Exception {
            // 获取供应商2的token
            String supplier2Token = obtainToken("supplier_user_2", "test123456");

            // 供应商2查询待办
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + supplier2Token)
                            .param("todoStatus", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }
    }

    // ==================== 认证与权限 ====================

    @Nested
    @DisplayName("认证与权限")
    class AuthTests {

        @Test
        @DisplayName("未认证访问待办列表返回401")
        void unauthenticatedAccess_returns401() throws Exception {
            mockMvc.perform(get("/v1/todos"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("未认证创建待办返回401")
        void unauthenticatedCreate_returns401() throws Exception {
            String body = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"order_confirm\"," +
                    "\"businessType\":\"purchase_order\"," +
                    "\"businessId\":1," +
                    "\"title\":\"测试\"" +
                    "}";

            mockMvc.perform(post("/v1/todos")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("未认证完成待办返回401")
        void unauthenticatedFinish_returns401() throws Exception {
            mockMvc.perform(post("/v1/todos/1/finish"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("未认证获取未读数返回401")
        void unauthenticatedUnreadCount_returns401() throws Exception {
            mockMvc.perform(get("/v1/todos/unread-count"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== 完整业务流程 ====================

    @Nested
    @DisplayName("完整业务流程")
    class FullFlowTests {

        @Test
        @DisplayName("完整流程: 创建 -> 查询 -> 完成 -> 验证状态")
        void fullFlow_create_query_finish_verify() throws Exception {
            // 1. 创建待办
            String createBody = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"delivery_receive\"," +
                    "\"businessType\":\"delivery_notice\"," +
                    "\"businessId\":777," +
                    "\"businessNo\":\"ASN-FLOW-001\"," +
                    "\"title\":\"流程测试-待收货通知单ASN-FLOW-001\"" +
                    "}";

            String createResponse = mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(createBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn().getResponse().getContentAsString();

            String todoId = objectMapper.readTree(createResponse).get("data").asText();

            // 2. 验证未读数增加
            mockMvc.perform(get("/v1/todos/unread-count")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNumber());

            // 3. 查询待办列表，能找到刚创建的
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("todoStatus", "0")
                            .param("businessType", "delivery_notice"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            // 4. 完成待办
            mockMvc.perform(post("/v1/todos/{id}/finish", todoId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 5. 验证已完成列表中包含该待办
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("todoStatus", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("完整流程: 创建 -> 忽略 -> 验证状态")
        void fullFlow_create_ignore_verify() throws Exception {
            // 1. 创建待办
            String createBody = "{" +
                    "\"userId\":1," +
                    "\"todoType\":\"quote_confirm\"," +
                    "\"businessType\":\"quote\"," +
                    "\"businessId\":666," +
                    "\"businessNo\":\"QT-FLOW-001\"," +
                    "\"title\":\"流程测试-待确认报价单QT-FLOW-001\"" +
                    "}";

            String createResponse = mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(createBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn().getResponse().getContentAsString();

            String todoId = objectMapper.readTree(createResponse).get("data").asText();

            // 2. 忽略待办
            mockMvc.perform(post("/v1/todos/{id}/ignore", todoId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 3. 验证已忽略列表中包含该待办
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("todoStatus", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("完整流程: 供应商待办创建与查询")
        void fullFlow_supplierTodo() throws Exception {
            // 1. 为供应商1创建待办
            String createBody = "{" +
                    "\"supplierId\":1," +
                    "\"todoType\":\"order_confirm\"," +
                    "\"businessType\":\"purchase_order\"," +
                    "\"businessId\":555," +
                    "\"businessNo\":\"PO-SUPPLIER-001\"," +
                    "\"title\":\"供应商流程测试-待确认采购订单PO-SUPPLIER-001\"" +
                    "}";

            mockMvc.perform(post("/v1/todos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(createBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 2. 供应商1查询待办
            mockMvc.perform(get("/v1/todos")
                            .header("Authorization", "Bearer " + supplierToken)
                            .param("todoStatus", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            // 3. 供应商1获取未读数
            mockMvc.perform(get("/v1/todos/unread-count")
                            .header("Authorization", "Bearer " + supplierToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNumber());
        }
    }
}
