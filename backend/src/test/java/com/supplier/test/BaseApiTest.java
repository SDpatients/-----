package com.supplier.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API 测试基类
 * <p>
 * 所有 API 集成测试继承此类，自动配置 MockMvc 和 H2 测试环境。
 * 提供 JWT Token 获取、通用断言等工具方法。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMockConfig.class)
public abstract class BaseApiTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String adminToken;
    protected String supplierToken;

    /**
     * 每次测试前重新获取 token（public 允许跨包子类重写）
     */
    @BeforeEach
    public void setUpBase() throws Exception {
        adminToken = obtainToken("admin_test", "test123456");
        supplierToken = obtainToken("supplier_user_1", "test123456");
    }

    /**
     * 通过用户名密码登录获取 JWT Token
     */
    protected String obtainToken(String username, String password) throws Exception {
        String loginBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        String response = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("data").get("token").asText();
    }

    /**
     * 断言响应 code=200 成功
     */
    protected void assertSuccess(String responseJson) throws Exception {
        int code = objectMapper.readTree(responseJson).get("code").asInt();
        if (code != 200) {
            String message = objectMapper.readTree(responseJson).get("message").asText();
            throw new AssertionError("Expected code 200 but got " + code + ": " + message);
        }
    }

    /**
     * 从响应 JSON 中提取 data 字段
     */
    protected String extractData(String responseJson) throws Exception {
        return objectMapper.readTree(responseJson).get("data").toString();
    }

    /**
     * 从响应 JSON 中提取嵌套字段值
     */
    protected String extractField(String responseJson, String fieldPath) throws Exception {
        String[] parts = fieldPath.split("\\.");
        var node = objectMapper.readTree(responseJson);
        for (String part : parts) {
            node = node.get(part);
            if (node == null) return null;
        }
        return node.asText();
    }
}