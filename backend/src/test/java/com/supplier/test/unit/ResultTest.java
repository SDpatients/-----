package com.supplier.test.unit;

import com.supplier.common.result.Result;
import com.supplier.common.result.ResultCode;
import com.supplier.test.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Result 单元测试")
class ResultTest extends BaseUnitTest {

    @Nested
    @DisplayName("success 方法测试")
    class SuccessTest {

        @Test
        @DisplayName("success() 返回 code=200, message=操作成功, data=null")
        void successWithoutData() {
            Result<Void> result = Result.success();

            assertEquals(200, result.getCode());
            assertEquals("操作成功", result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("success(data) 返回 code=200, data 为传入数据")
        void successWithData() {
            String data = "test data";
            Result<String> result = Result.success(data);

            assertEquals(200, result.getCode());
            assertEquals("操作成功", result.getMessage());
            assertEquals(data, result.getData());
        }

        @Test
        @DisplayName("success(message, data) 返回 code=200, 自定义 message 和 data")
        void successWithMessageAndData() {
            String message = "自定义消息";
            Integer data = 42;
            Result<Integer> result = Result.success(message, data);

            assertEquals(200, result.getCode());
            assertEquals(message, result.getMessage());
            assertEquals(data, result.getData());
        }
    }

    @Nested
    @DisplayName("error 方法测试")
    class ErrorTest {

        @Test
        @DisplayName("error(message) 返回 code=500, 自定义 message")
        void errorWithMessage() {
            String message = "系统错误";
            Result<Void> result = Result.error(message);

            assertEquals(500, result.getCode());
            assertEquals(message, result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("error(code, message) 返回自定义 code 和 message")
        void errorWithCodeAndMessage() {
            Integer code = 40001;
            String message = "参数错误";
            Result<Void> result = Result.error(code, message);

            assertEquals(code, result.getCode());
            assertEquals(message, result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("error(ResultCode) 返回枚举中的 code 和 message")
        void errorWithResultCode() {
            Result<Void> result = Result.error(ResultCode.BUSINESS_ERROR);

            assertEquals(ResultCode.BUSINESS_ERROR.getCode(), result.getCode());
            assertEquals(ResultCode.BUSINESS_ERROR.getMessage(), result.getMessage());
            assertNull(result.getData());
        }
    }

    @Nested
    @DisplayName("isSuccess 方法测试")
    class IsSuccessTest {

        @Test
        @DisplayName("code=200 时 isSuccess 返回 true")
        void isSuccessReturnsTrueFor200() {
            Result<Void> result = Result.success();
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("code!=200 时 isSuccess 返回 false")
        void isSuccessReturnsFalseForNon200() {
            Result<Void> result = Result.error("错误");
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("code=null 时 isSuccess 返回 false")
        void isSuccessReturnsFalseForNull() {
            Result<Void> result = new Result<>();
            assertFalse(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("自动填充字段测试")
    class AutoPopulatedFieldsTest {

        @Test
        @DisplayName("timestamp 自动填充为当前时间")
        void timestampIsAutoPopulated() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            Result<Void> result = Result.success();
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(result.getTimestamp());
            assertTrue(result.getTimestamp().isAfter(before));
            assertTrue(result.getTimestamp().isBefore(after));
        }

        @Test
        @DisplayName("traceId 自动填充为非空字符串")
        void traceIdIsAutoPopulated() {
            Result<Void> result = Result.success();

            assertNotNull(result.getTraceId());
            assertFalse(result.getTraceId().isEmpty());
        }

        @Test
        @DisplayName("traceId 不包含连字符")
        void traceIdHasNoDashes() {
            Result<Void> result = Result.success();

            assertNotNull(result.getTraceId());
            assertFalse(result.getTraceId().contains("-"));
        }
    }
}
