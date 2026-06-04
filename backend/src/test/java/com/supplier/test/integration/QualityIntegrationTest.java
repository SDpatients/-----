package com.supplier.test.integration;

import com.supplier.test.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("质量模块集成测试")
class QualityIntegrationTest extends BaseApiTest {

    @Nested
    @DisplayName("8D整改报告接口")
    class EightDReportTests {

        @Test
        @DisplayName("分页查询8D整改报告")
        void pageQueryEightDReports() throws Exception {
            mockMvc.perform(get("/v1/eight-d-reports")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("创建8D整改报告")
        void createEightDReport() throws Exception {
            String body = "{" +
                    "\"reportNo\":\"8D-IT-001\"," +
                    "\"ncrId\":1," +
                    "\"supplierId\":1," +
                    "\"d1Team\":\"张三,李四\"," +
                    "\"d2Problem\":\"来料尺寸超差\"," +
                    "\"d3Containment\":\"隔离库存\"," +
                    "\"d4RootCause\":\"工装磨损\"," +
                    "\"d5CorrectiveAction\":\"更换工装\"," +
                    "\"d6ValidateAction\":\"首件检验确认\"," +
                    "\"d7PreventAction\":\"定期工装点检\"," +
                    "\"d8CloseSummary\":\"已完成整改\"," +
                    "\"dueDate\":\"2026-07-01\"" +
                    "}";
            mockMvc.perform(post("/v1/eight-d-reports")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("质量申诉接口")
    class QualityAppealTests {

        @Test
        @DisplayName("分页查询质量申诉")
        void pageQueryQualityAppeals() throws Exception {
            mockMvc.perform(get("/v1/quality-appeals")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("创建质量申诉")
        void createQualityAppeal() throws Exception {
            String body = "{" +
                    "\"appealNo\":\"QA-IT-001\"," +
                    "\"ncrId\":1," +
                    "\"inspectionId\":1," +
                    "\"supplierId\":1," +
                    "\"appealReason\":\"检验标准理解不一致\"" +
                    "}";
            mockMvc.perform(post("/v1/quality-appeals")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }
    }
}
