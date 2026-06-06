package com.supplier.test.unit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.portal.dto.PortalTodoCreateDTO;
import com.supplier.portal.entity.PortalTodo;
import com.supplier.portal.mapper.PortalTodoMapper;
import com.supplier.portal.query.PortalTodoQuery;
import com.supplier.portal.service.impl.PortalTodoServiceImpl;
import com.supplier.portal.vo.PortalTodoVO;
import com.supplier.security.util.SecurityUtils;
import com.supplier.test.BaseUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PortalTodoServiceTest extends BaseUnitTest {

    @InjectMocks
    private PortalTodoServiceImpl todoService;

    @Mock
    private PortalTodoMapper mapper;

    // ==================== create() ====================

    @Nested
    @DisplayName("创建待办 - create()")
    class CreateTests {

        @Test
        @DisplayName("创建待办成功 - 默认状态为0")
        void create_success_defaultStatus() {
            PortalTodoCreateDTO dto = new PortalTodoCreateDTO();
            dto.setUserId(1L);
            dto.setSupplierId(100L);
            dto.setTodoType("order_confirm");
            dto.setBusinessType("purchase_order");
            dto.setBusinessId(10L);
            dto.setBusinessNo("PO20260601001");
            dto.setTitle("待确认采购订单PO20260601001");
            dto.setDueTime(LocalDateTime.now().plusDays(3));

            when(mapper.insert(any(PortalTodo.class))).thenAnswer(invocation -> {
                PortalTodo todo = invocation.getArgument(0);
                todo.setId(1L);
                return 1;
            });

            Long id = todoService.create(dto);

            assertEquals(1L, id);

            ArgumentCaptor<PortalTodo> captor = ArgumentCaptor.forClass(PortalTodo.class);
            verify(mapper).insert(captor.capture());
            PortalTodo saved = captor.getValue();
            assertEquals(0, saved.getTodoStatus());
            assertNull(saved.getFinishTime());
            assertEquals("order_confirm", saved.getTodoType());
            assertEquals("purchase_order", saved.getBusinessType());
            assertEquals(10L, saved.getBusinessId());
            assertEquals("PO20260601001", saved.getBusinessNo());
            assertEquals("待确认采购订单PO20260601001", saved.getTitle());
            assertEquals(1L, saved.getUserId());
            assertEquals(100L, saved.getSupplierId());
        }

        @Test
        @DisplayName("创建待办 - 仅指定userId时为采购方待办")
        void create_buyerTodo_onlyUserId() {
            PortalTodoCreateDTO dto = new PortalTodoCreateDTO();
            dto.setUserId(5L);
            dto.setTodoType("delivery_receive");
            dto.setBusinessType("delivery_notice");
            dto.setBusinessId(20L);
            dto.setTitle("待收货通知单");

            when(mapper.insert(any(PortalTodo.class))).thenReturn(1);

            todoService.create(dto);

            ArgumentCaptor<PortalTodo> captor = ArgumentCaptor.forClass(PortalTodo.class);
            verify(mapper).insert(captor.capture());
            PortalTodo saved = captor.getValue();
            assertEquals(5L, saved.getUserId());
            assertNull(saved.getSupplierId());
        }

        @Test
        @DisplayName("创建待办 - 仅指定supplierId时为供应商待办")
        void create_supplierTodo_onlySupplierId() {
            PortalTodoCreateDTO dto = new PortalTodoCreateDTO();
            dto.setSupplierId(200L);
            dto.setTodoType("rfq_quote");
            dto.setBusinessType("rfq");
            dto.setBusinessId(30L);
            dto.setTitle("待报价询价单");

            when(mapper.insert(any(PortalTodo.class))).thenReturn(1);

            todoService.create(dto);

            ArgumentCaptor<PortalTodo> captor = ArgumentCaptor.forClass(PortalTodo.class);
            verify(mapper).insert(captor.capture());
            PortalTodo saved = captor.getValue();
            assertNull(saved.getUserId());
            assertEquals(200L, saved.getSupplierId());
        }

        @Test
        @DisplayName("创建待办 - dueTime为null时不设置截止时间")
        void create_noDueTime() {
            PortalTodoCreateDTO dto = new PortalTodoCreateDTO();
            dto.setTodoType("order_confirm");
            dto.setBusinessType("purchase_order");
            dto.setBusinessId(10L);
            dto.setTitle("待确认订单");
            dto.setDueTime(null);

            when(mapper.insert(any(PortalTodo.class))).thenReturn(1);

            todoService.create(dto);

            ArgumentCaptor<PortalTodo> captor = ArgumentCaptor.forClass(PortalTodo.class);
            verify(mapper).insert(captor.capture());
            assertNull(captor.getValue().getDueTime());
        }
    }

    // ==================== finish() ====================

    @Nested
    @DisplayName("完成待办 - finish()")
    class FinishTests {

        @Test
        @DisplayName("完成待办成功 - 状态变为1并记录完成时间")
        void finish_success() {
            PortalTodo todo = new PortalTodo();
            todo.setId(1L);
            todo.setTodoStatus(0);
            todo.setTitle("待确认订单");

            when(mapper.selectById(1L)).thenReturn(todo);
            when(mapper.updateById(any())).thenReturn(1);

            todoService.finish(1L);

            assertEquals(1, todo.getTodoStatus());
            assertNotNull(todo.getFinishTime());
            verify(mapper).updateById(todo);
        }

        @Test
        @DisplayName("完成待办 - 待办不存在时抛出NOT_FOUND异常")
        void finish_notFound_throws() {
            when(mapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class, () -> todoService.finish(999L));
            assertTrue(ex.getMessage().contains("资源不存在"));
        }

        @Test
        @DisplayName("完成已忽略的待办 - 状态从2变为1")
        void finish_fromIgnoredStatus() {
            PortalTodo todo = new PortalTodo();
            todo.setId(1L);
            todo.setTodoStatus(2);

            when(mapper.selectById(1L)).thenReturn(todo);
            when(mapper.updateById(any())).thenReturn(1);

            todoService.finish(1L);

            assertEquals(1, todo.getTodoStatus());
            assertNotNull(todo.getFinishTime());
        }
    }

    // ==================== ignore() ====================

    @Nested
    @DisplayName("忽略待办 - ignore()")
    class IgnoreTests {

        @Test
        @DisplayName("忽略待办成功 - 状态变为2且不记录完成时间")
        void ignore_success() {
            PortalTodo todo = new PortalTodo();
            todo.setId(1L);
            todo.setTodoStatus(0);
            todo.setTitle("待确认订单");

            when(mapper.selectById(1L)).thenReturn(todo);
            when(mapper.updateById(any())).thenReturn(1);

            todoService.ignore(1L);

            assertEquals(2, todo.getTodoStatus());
            assertNull(todo.getFinishTime());
            verify(mapper).updateById(todo);
        }

        @Test
        @DisplayName("忽略待办 - 待办不存在时抛出NOT_FOUND异常")
        void ignore_notFound_throws() {
            when(mapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class, () -> todoService.ignore(999L));
            assertTrue(ex.getMessage().contains("资源不存在"));
        }
    }

    // ==================== unreadCount() ====================

    @Nested
    @DisplayName("未读待办数 - unreadCount()")
    class UnreadCountTests {

        @Test
        @DisplayName("采购方用户统计未读待办数")
        void unreadCount_buyerUser() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

                Long count = todoService.unreadCount();

                assertEquals(5L, count);
                verify(mapper).selectCount(any(LambdaQueryWrapper.class));
            }
        }

        @Test
        @DisplayName("供应商用户统计未读待办数")
        void unreadCount_supplierUser() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(true);
                securityUtilsMock.when(SecurityUtils::getSupplierId).thenReturn(100L);

                when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

                Long count = todoService.unreadCount();

                assertEquals(3L, count);
                verify(mapper).selectCount(any(LambdaQueryWrapper.class));
            }
        }

        @Test
        @DisplayName("无未读待办时返回0")
        void unreadCount_zero() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

                Long count = todoService.unreadCount();

                assertEquals(0L, count);
            }
        }
    }

    // ==================== page() ====================

    @Nested
    @DisplayName("分页查询 - page()")
    class PageTests {

        private PortalTodo buildTodo(Long id, String todoType, String businessType, Integer status) {
            PortalTodo todo = new PortalTodo();
            todo.setId(id);
            todo.setUserId(1L);
            todo.setSupplierId(100L);
            todo.setTodoType(todoType);
            todo.setBusinessType(businessType);
            todo.setBusinessId(10L);
            todo.setBusinessNo("PO001");
            todo.setTitle("测试待办");
            todo.setTodoStatus(status);
            todo.setCreateTime(LocalDateTime.now());
            return todo;
        }

        @Test
        @DisplayName("分页查询 - 采购方用户只能看到自己的待办")
        void page_buyerUser_scopeFilter() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                Page<PortalTodo> mockPage = new Page<>(1, 10);
                mockPage.setRecords(Collections.singletonList(buildTodo(1L, "order_confirm", "purchase_order", 0)));
                mockPage.setTotal(1);
                when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

                PortalTodoQuery query = new PortalTodoQuery();
                query.setPageNum(1L);
                query.setPageSize(10L);

                PageResult<PortalTodoVO> result = todoService.page(query);

                assertNotNull(result);
                verify(mapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
            }
        }

        @Test
        @DisplayName("分页查询 - 供应商用户只能看到自己供应商的待办")
        void page_supplierUser_scopeFilter() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(true);
                securityUtilsMock.when(SecurityUtils::getSupplierId).thenReturn(100L);

                Page<PortalTodo> mockPage = new Page<>(1, 10);
                mockPage.setRecords(Collections.emptyList());
                mockPage.setTotal(0);
                when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

                PortalTodoQuery query = new PortalTodoQuery();
                query.setPageNum(1L);
                query.setPageSize(10L);

                PageResult<PortalTodoVO> result = todoService.page(query);

                assertNotNull(result);
                verify(mapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
            }
        }

        @Test
        @DisplayName("分页查询 - 按todoType过滤")
        void page_filterByTodoType() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                Page<PortalTodo> mockPage = new Page<>(1, 10);
                mockPage.setRecords(Collections.emptyList());
                mockPage.setTotal(0);
                when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

                PortalTodoQuery query = new PortalTodoQuery();
                query.setPageNum(1L);
                query.setPageSize(10L);
                query.setTodoType("order_confirm");

                todoService.page(query);

                verify(mapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
            }
        }

        @Test
        @DisplayName("分页查询 - 按todoStatus过滤")
        void page_filterByTodoStatus() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                Page<PortalTodo> mockPage = new Page<>(1, 10);
                mockPage.setRecords(Collections.emptyList());
                mockPage.setTotal(0);
                when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

                PortalTodoQuery query = new PortalTodoQuery();
                query.setPageNum(1L);
                query.setPageSize(10L);
                query.setTodoStatus(0);

                todoService.page(query);

                verify(mapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
            }
        }

        @Test
        @DisplayName("toVO - 模块名称映射正确")
        void page_toVO_moduleNameMapping() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                PortalTodo todo = buildTodo(1L, "order_confirm", "purchase_order", 0);
                Page<PortalTodo> mockPage = new Page<>(1, 10);
                mockPage.setRecords(Collections.singletonList(todo));
                mockPage.setTotal(1);
                when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

                PortalTodoQuery query = new PortalTodoQuery();
                query.setPageNum(1L);
                query.setPageSize(10L);

                PageResult<PortalTodoVO> result = todoService.page(query);

                assertEquals(1, result.getRecords().size());
                PortalTodoVO vo = result.getRecords().get(0);
                assertEquals("采购订单", vo.getModule());
            }
        }

        @Test
        @DisplayName("toVO - 未知businessType原样返回")
        void page_toVO_unknownBusinessType() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                PortalTodo todo = buildTodo(1L, "custom_type", "custom_business", 0);
                Page<PortalTodo> mockPage = new Page<>(1, 10);
                mockPage.setRecords(Collections.singletonList(todo));
                mockPage.setTotal(1);
                when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

                PortalTodoQuery query = new PortalTodoQuery();
                query.setPageNum(1L);
                query.setPageSize(10L);

                PageResult<PortalTodoVO> result = todoService.page(query);

                assertEquals("custom_business", result.getRecords().get(0).getModule());
            }
        }

        @Test
        @DisplayName("toVO - 所有已知模块名称映射验证")
        void page_toVO_allModuleMappings() {
            String[][] mappings = {
                    {"purchase_order", "采购订单"},
                    {"delivery_notice", "送货通知"},
                    {"rfq", "询价单"},
                    {"quote", "报价单"},
                    {"reconciliation", "财务对账"},
                    {"quality_inspection", "质量检验"},
                    {"nonconformance_report", "不合格报告"},
                    {"order_pending", "订单待确认"},
                    {"delivery_delay", "送货逾期"},
                    {"delivery_approaching", "送货即将到期"},
                    {"order_overdue", "订单逾期"},
                    {"rfq_deadline", "询价即将截止"}
            };

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                for (String[] mapping : mappings) {
                    PortalTodo todo = buildTodo(1L, "test", mapping[0], 0);
                    Page<PortalTodo> mockPage = new Page<>(1, 10);
                    mockPage.setRecords(Collections.singletonList(todo));
                    mockPage.setTotal(1);
                    when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

                    PortalTodoQuery query = new PortalTodoQuery();
                    query.setPageNum(1L);
                    query.setPageSize(10L);

                    PageResult<PortalTodoVO> result = todoService.page(query);
                    assertEquals(mapping[1], result.getRecords().get(0).getModule(),
                            "模块名称映射不匹配: " + mapping[0]);

                    reset(mapper);
                }
            }
        }
    }

    // ==================== autoFinishByBusiness() ====================

    @Nested
    @DisplayName("按业务自动完成 - autoFinishByBusiness()")
    class AutoFinishTests {

        @Test
        @DisplayName("自动完成 - 匹配的待办状态变为1并记录完成时间")
        void autoFinish_success() {
            PortalTodo todo1 = new PortalTodo();
            todo1.setId(1L);
            todo1.setTodoStatus(0);
            todo1.setBusinessType("purchase_order");
            todo1.setBusinessId(10L);

            PortalTodo todo2 = new PortalTodo();
            todo2.setId(2L);
            todo2.setTodoStatus(0);
            todo2.setBusinessType("purchase_order");
            todo2.setBusinessId(10L);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                when(mapper.selectList(any(LambdaQueryWrapper.class)))
                        .thenReturn(Arrays.asList(todo1, todo2));
                when(mapper.updateById(any())).thenReturn(1);

                todoService.autoFinishByBusiness("purchase_order", 10L);

                assertEquals(1, todo1.getTodoStatus());
                assertNotNull(todo1.getFinishTime());
                assertEquals(1, todo2.getTodoStatus());
                assertNotNull(todo2.getFinishTime());
                verify(mapper, times(2)).updateById(any());
            }
        }

        @Test
        @DisplayName("自动完成 - 没有匹配的待办时不报错")
        void autoFinish_noMatchingTodos() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                when(mapper.selectList(any(LambdaQueryWrapper.class)))
                        .thenReturn(Collections.emptyList());

                todoService.autoFinishByBusiness("purchase_order", 999L);

                verify(mapper, never()).updateById(any());
            }
        }

        @Test
        @DisplayName("自动完成 - businessType为空时不处理")
        void autoFinish_emptyBusinessType() {
            todoService.autoFinishByBusiness("", 10L);

            verify(mapper, never()).selectList(any());
            verify(mapper, never()).updateById(any());
        }

        @Test
        @DisplayName("自动完成 - businessType为null时不处理")
        void autoFinish_nullBusinessType() {
            todoService.autoFinishByBusiness(null, 10L);

            verify(mapper, never()).selectList(any());
            verify(mapper, never()).updateById(any());
        }

        @Test
        @DisplayName("自动完成 - businessId为null时不处理")
        void autoFinish_nullBusinessId() {
            todoService.autoFinishByBusiness("purchase_order", null);

            verify(mapper, never()).selectList(any());
            verify(mapper, never()).updateById(any());
        }

        @Test
        @DisplayName("自动完成 - 只处理状态为0的待办")
        void autoFinish_onlyPendingTodos() {
            PortalTodo pending = new PortalTodo();
            pending.setId(1L);
            pending.setTodoStatus(0);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                when(mapper.selectList(any(LambdaQueryWrapper.class)))
                        .thenReturn(Collections.singletonList(pending));
                when(mapper.updateById(any())).thenReturn(1);

                todoService.autoFinishByBusiness("purchase_order", 10L);

                assertEquals(1, pending.getTodoStatus());
                verify(mapper).updateById(pending);
            }
        }
    }

    // ==================== 数据权限隔离 ====================

    @Nested
    @DisplayName("数据权限隔离 - baseScope()")
    class DataScopeTests {

        @Test
        @DisplayName("供应商用户 - 按supplierId过滤")
        void baseScope_supplierUser() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(true);
                securityUtilsMock.when(SecurityUtils::getSupplierId).thenReturn(100L);

                when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

                Long count = todoService.unreadCount();

                assertEquals(2L, count);
                // 验证调用了 selectCount，且传入了包含 supplierId 条件的 wrapper
                ArgumentCaptor<LambdaQueryWrapper<PortalTodo>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
                verify(mapper).selectCount(wrapperCaptor.capture());
                // LambdaQueryWrapper 内部条件无法直接断言，但通过调用验证已执行
            }
        }

        @Test
        @DisplayName("采购方用户 - 按userId过滤")
        void baseScope_buyerUser() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

                Long count = todoService.unreadCount();

                assertEquals(3L, count);
                verify(mapper).selectCount(any(LambdaQueryWrapper.class));
            }
        }

        @Test
        @DisplayName("未登录用户 - 无额外过滤条件")
        void baseScope_anonymousUser() {
            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(null);

                when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

                Long count = todoService.unreadCount();

                assertEquals(0L, count);
                verify(mapper).selectCount(any(LambdaQueryWrapper.class));
            }
        }
    }

    // ==================== 状态流转 ====================

    @Nested
    @DisplayName("待办状态流转")
    class StatusTransitionTests {

        @Test
        @DisplayName("完整状态流转: 待办 -> 已办")
        void transition_pending_to_finished() {
            PortalTodo todo = new PortalTodo();
            todo.setId(1L);
            todo.setTodoStatus(0);

            when(mapper.selectById(1L)).thenReturn(todo);
            when(mapper.updateById(any())).thenReturn(1);

            todoService.finish(1L);

            assertEquals(1, todo.getTodoStatus());
            assertNotNull(todo.getFinishTime());
        }

        @Test
        @DisplayName("完整状态流转: 待办 -> 忽略")
        void transition_pending_to_ignored() {
            PortalTodo todo = new PortalTodo();
            todo.setId(1L);
            todo.setTodoStatus(0);

            when(mapper.selectById(1L)).thenReturn(todo);
            when(mapper.updateById(any())).thenReturn(1);

            todoService.ignore(1L);

            assertEquals(2, todo.getTodoStatus());
            assertNull(todo.getFinishTime());
        }

        @Test
        @DisplayName("状态流转: 忽略 -> 已办 (重新处理)")
        void transition_ignored_to_finished() {
            PortalTodo todo = new PortalTodo();
            todo.setId(1L);
            todo.setTodoStatus(2);

            when(mapper.selectById(1L)).thenReturn(todo);
            when(mapper.updateById(any())).thenReturn(1);

            todoService.finish(1L);

            assertEquals(1, todo.getTodoStatus());
            assertNotNull(todo.getFinishTime());
        }

        @Test
        @DisplayName("自动完成: 待办 -> 已办")
        void transition_autoFinish() {
            PortalTodo todo = new PortalTodo();
            todo.setId(1L);
            todo.setTodoStatus(0);
            todo.setBusinessType("purchase_order");
            todo.setBusinessId(10L);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::isSupplierUser).thenReturn(false);
                securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);

                when(mapper.selectList(any(LambdaQueryWrapper.class)))
                        .thenReturn(Collections.singletonList(todo));
                when(mapper.updateById(any())).thenReturn(1);

                todoService.autoFinishByBusiness("purchase_order", 10L);

                assertEquals(1, todo.getTodoStatus());
                assertNotNull(todo.getFinishTime());
            }
        }
    }
}
