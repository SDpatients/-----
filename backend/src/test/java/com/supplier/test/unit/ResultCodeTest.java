package com.supplier.test.unit;

import com.supplier.common.result.ResultCode;
import com.supplier.test.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ResultCode 单元测试")
class ResultCodeTest extends BaseUnitTest {

    @Nested
    @DisplayName("枚举值 code 测试")
    class CodeTest {

        @Test
        @DisplayName("SUCCESS 的 code 为 200")
        void successCode() {
            assertEquals(200, ResultCode.SUCCESS.getCode());
        }

        @Test
        @DisplayName("PARAM_ERROR 的 code 为 40001")
        void paramErrorCode() {
            assertEquals(40001, ResultCode.PARAM_ERROR.getCode());
        }

        @Test
        @DisplayName("NOT_FOUND 的 code 为 40002")
        void notFoundCode() {
            assertEquals(40002, ResultCode.NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("UNAUTHORIZED 的 code 为 40101")
        void unauthorizedCode() {
            assertEquals(40101, ResultCode.UNAUTHORIZED.getCode());
        }

        @Test
        @DisplayName("TOKEN_EXPIRED 的 code 为 40102")
        void tokenExpiredCode() {
            assertEquals(40102, ResultCode.TOKEN_EXPIRED.getCode());
        }

        @Test
        @DisplayName("FORBIDDEN 的 code 为 40301")
        void forbiddenCode() {
            assertEquals(40301, ResultCode.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("DATA_FORBIDDEN 的 code 为 40302")
        void dataForbiddenCode() {
            assertEquals(40302, ResultCode.DATA_FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("STATUS_NOT_ALLOWED 的 code 为 40901")
        void statusNotAllowedCode() {
            assertEquals(40901, ResultCode.STATUS_NOT_ALLOWED.getCode());
        }

        @Test
        @DisplayName("DUPLICATE_SUBMIT 的 code 为 40902")
        void duplicateSubmitCode() {
            assertEquals(40902, ResultCode.DUPLICATE_SUBMIT.getCode());
        }

        @Test
        @DisplayName("INTERNAL_ERROR 的 code 为 50001")
        void internalErrorCode() {
            assertEquals(50001, ResultCode.INTERNAL_ERROR.getCode());
        }

        @Test
        @DisplayName("EXTERNAL_ERROR 的 code 为 50002")
        void externalErrorCode() {
            assertEquals(50002, ResultCode.EXTERNAL_ERROR.getCode());
        }

        @Test
        @DisplayName("REDIS_ERROR 的 code 为 50003")
        void redisErrorCode() {
            assertEquals(50003, ResultCode.REDIS_ERROR.getCode());
        }

        @Test
        @DisplayName("BUSINESS_ERROR 的 code 为 40900")
        void businessErrorCode() {
            assertEquals(40900, ResultCode.BUSINESS_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("枚举值 message 测试")
    class MessageTest {

        @Test
        @DisplayName("SUCCESS 的 message 为 操作成功")
        void successMessage() {
            assertEquals("操作成功", ResultCode.SUCCESS.getMessage());
        }

        @Test
        @DisplayName("PARAM_ERROR 的 message 为 参数错误")
        void paramErrorMessage() {
            assertEquals("参数错误", ResultCode.PARAM_ERROR.getMessage());
        }

        @Test
        @DisplayName("NOT_FOUND 的 message 为 资源不存在")
        void notFoundMessage() {
            assertEquals("资源不存在", ResultCode.NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("UNAUTHORIZED 的 message 为 未登录")
        void unauthorizedMessage() {
            assertEquals("未登录", ResultCode.UNAUTHORIZED.getMessage());
        }

        @Test
        @DisplayName("TOKEN_EXPIRED 的 message 为 令牌过期")
        void tokenExpiredMessage() {
            assertEquals("令牌过期", ResultCode.TOKEN_EXPIRED.getMessage());
        }

        @Test
        @DisplayName("FORBIDDEN 的 message 为 无权限")
        void forbiddenMessage() {
            assertEquals("无权限", ResultCode.FORBIDDEN.getMessage());
        }

        @Test
        @DisplayName("DATA_FORBIDDEN 的 message 为 数据越权")
        void dataForbiddenMessage() {
            assertEquals("数据越权", ResultCode.DATA_FORBIDDEN.getMessage());
        }

        @Test
        @DisplayName("STATUS_NOT_ALLOWED 的 message 为 状态不允许")
        void statusNotAllowedMessage() {
            assertEquals("状态不允许", ResultCode.STATUS_NOT_ALLOWED.getMessage());
        }

        @Test
        @DisplayName("DUPLICATE_SUBMIT 的 message 为 重复提交")
        void duplicateSubmitMessage() {
            assertEquals("重复提交", ResultCode.DUPLICATE_SUBMIT.getMessage());
        }

        @Test
        @DisplayName("INTERNAL_ERROR 的 message 为 系统异常")
        void internalErrorMessage() {
            assertEquals("系统异常", ResultCode.INTERNAL_ERROR.getMessage());
        }

        @Test
        @DisplayName("EXTERNAL_ERROR 的 message 为 外部系统异常")
        void externalErrorMessage() {
            assertEquals("外部系统异常", ResultCode.EXTERNAL_ERROR.getMessage());
        }

        @Test
        @DisplayName("REDIS_ERROR 的 message 为 缓存服务异常")
        void redisErrorMessage() {
            assertEquals("缓存服务异常", ResultCode.REDIS_ERROR.getMessage());
        }

        @Test
        @DisplayName("BUSINESS_ERROR 的 message 为 业务异常")
        void businessErrorMessage() {
            assertEquals("业务异常", ResultCode.BUSINESS_ERROR.getMessage());
        }
    }

    @Nested
    @DisplayName("枚举通用属性测试")
    class GeneralTest {

        @ParameterizedTest
        @EnumSource(ResultCode.class)
        @DisplayName("所有枚举值的 code 不为空")
        void allCodesNotNull(ResultCode resultCode) {
            assertNotNull(resultCode.getCode());
        }

        @ParameterizedTest
        @EnumSource(ResultCode.class)
        @DisplayName("所有枚举值的 message 不为空")
        void allMessagesNotNull(ResultCode resultCode) {
            assertNotNull(resultCode.getMessage());
            assertTrue(resultCode.getMessage().length() > 0);
        }

        @Test
        @DisplayName("枚举值总数为 13")
        void enumValuesCount() {
            assertEquals(13, ResultCode.values().length);
        }
    }
}
