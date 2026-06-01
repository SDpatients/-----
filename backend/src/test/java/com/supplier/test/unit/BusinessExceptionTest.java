package com.supplier.test.unit;

import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.test.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("BusinessException 单元测试")
class BusinessExceptionTest extends BaseUnitTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTest {

        @Test
        @DisplayName("BusinessException 是 RuntimeException 的子类")
        void extendsRuntimeException() {
            BusinessException exception = new BusinessException("test");
            assertInstanceOf(RuntimeException.class, exception);
        }

        @Test
        @DisplayName("String 构造函数 code=600, message 为传入值")
        void constructorWithMessage() {
            String message = "业务错误";
            BusinessException exception = new BusinessException(message);

            assertEquals(600, exception.getCode());
            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("Integer + String 构造函数设置自定义 code 和 message")
        void constructorWithCodeAndMessage() {
            Integer code = 40001;
            String message = "参数错误";
            BusinessException exception = new BusinessException(code, message);

            assertEquals(code, exception.getCode());
            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("ResultCode 构造函数使用枚举的 code 和 message")
        void constructorWithResultCode() {
            BusinessException exception = new BusinessException(ResultCode.UNAUTHORIZED);

            assertEquals(ResultCode.UNAUTHORIZED.getCode(), exception.getCode());
            assertEquals(ResultCode.UNAUTHORIZED.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("of 静态工厂方法测试")
    class OfFactoryMethodTest {

        @Test
        @DisplayName("of(String) 等价于 new BusinessException(message)")
        void ofWithMessage() {
            String message = "工厂方法错误";
            BusinessException exception = BusinessException.of(message);

            assertEquals(600, exception.getCode());
            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("of(Integer, String) 等价于 new BusinessException(code, message)")
        void ofWithCodeAndMessage() {
            Integer code = 50001;
            String message = "系统异常";
            BusinessException exception = BusinessException.of(code, message);

            assertEquals(code, exception.getCode());
            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("of(ResultCode) 等价于 new BusinessException(resultCode)")
        void ofWithResultCode() {
            BusinessException exception = BusinessException.of(ResultCode.FORBIDDEN);

            assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
            assertEquals(ResultCode.FORBIDDEN.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("异常继承测试")
    class InheritanceTest {

        @Test
        @DisplayName("可以被 try-catch 作为 RuntimeException 捕获")
        void canBeCaughtAsRuntimeException() {
            RuntimeException caught = null;
            try {
                throw new BusinessException("测试异常");
            } catch (RuntimeException e) {
                caught = e;
            }

            assertInstanceOf(BusinessException.class, caught);
            assertEquals("测试异常", caught.getMessage());
        }
    }
}
