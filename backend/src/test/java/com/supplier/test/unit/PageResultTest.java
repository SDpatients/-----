package com.supplier.test.unit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplier.common.result.PageResult;
import com.supplier.test.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("PageResult 单元测试")
class PageResultTest extends BaseUnitTest {

    @Nested
    @DisplayName("默认构造函数测试")
    class DefaultConstructorTest {

        @Test
        @DisplayName("默认构造函数初始化默认值")
        void defaultConstructorValues() {
            PageResult<String> result = new PageResult<>();

            assertTrue(result.getRecords().isEmpty());
            assertEquals(0L, result.getTotal());
            assertEquals(10L, result.getPageSize());
            assertEquals(1L, result.getPageNum());
            assertEquals(0L, result.getPages());
        }
    }

    @Nested
    @DisplayName("带参构造函数测试")
    class CustomConstructorTest {

        @Test
        @DisplayName("自定义构造函数正确计算页数")
        void customConstructorCalculatesPages() {
            List<String> records = Arrays.asList("a", "b", "c");
            PageResult<String> result = new PageResult<>(records, 25L, 10L, 1L);

            assertEquals(records, result.getRecords());
            assertEquals(25L, result.getTotal());
            assertEquals(10L, result.getPageSize());
            assertEquals(1L, result.getPageNum());
            assertEquals(3L, result.getPages());
        }

        @Test
        @DisplayName("总记录数不能整除页大小时向上取整")
        void pagesRoundedUp() {
            PageResult<String> result = new PageResult<>(Collections.singletonList("a"), 11L, 10L, 2L);

            assertEquals(2L, result.getPages());
        }

        @Test
        @DisplayName("总记录数为0时页数为0")
        void zeroTotalGivesZeroPages() {
            PageResult<String> result = new PageResult<>(Collections.emptyList(), 0L, 10L, 1L);

            assertEquals(0L, result.getPages());
        }
    }

    @Nested
    @DisplayName("of 工厂方法测试")
    class OfFactoryMethodTest {

        @Test
        @DisplayName("of(List, total, size, current) 工厂方法")
        void ofWithListAndPagination() {
            List<Integer> records = Arrays.asList(1, 2, 3);
            PageResult<Integer> result = PageResult.of(records, 30L, 10L, 1L);

            assertEquals(records, result.getRecords());
            assertEquals(30L, result.getTotal());
            assertEquals(10L, result.getPageSize());
            assertEquals(1L, result.getPageNum());
            assertEquals(3L, result.getPages());
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("of(IPage) 工厂方法")
        void ofWithIPage() {
            IPage<String> page = mock(IPage.class);
            List<String> records = Arrays.asList("x", "y");
            when(page.getRecords()).thenReturn(records);
            when(page.getTotal()).thenReturn(20L);
            when(page.getSize()).thenReturn(10L);
            when(page.getCurrent()).thenReturn(1L);

            PageResult<String> result = PageResult.of(page);

            assertEquals(records, result.getRecords());
            assertEquals(20L, result.getTotal());
            assertEquals(10L, result.getPageSize());
            assertEquals(1L, result.getPageNum());
            assertEquals(2L, result.getPages());
        }
    }

    @Nested
    @DisplayName("empty 工厂方法测试")
    class EmptyFactoryMethodTest {

        @Test
        @DisplayName("empty() 返回空分页结果")
        void emptyReturnsEmptyResult() {
            PageResult<String> result = PageResult.empty();

            assertNotNull(result);
            assertTrue(result.getRecords().isEmpty());
            assertEquals(0L, result.getTotal());
            assertEquals(10L, result.getPageSize());
            assertEquals(1L, result.getPageNum());
            assertEquals(0L, result.getPages());
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("pageSize=0 时不会除零，pages=0")
        void pageSizeZeroDoesNotDivideByZero() {
            PageResult<String> result = new PageResult<>(Collections.emptyList(), 10L, 0L, 1L);

            assertEquals(0L, result.getPages());
        }

        @Test
        @DisplayName("单条记录单页时 pages=1")
        void singleRecordSinglePage() {
            PageResult<String> result = new PageResult<>(Collections.singletonList("a"), 1L, 10L, 1L);

            assertEquals(1L, result.getPages());
        }
    }
}
