package com.supplier.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.portal.dto.PortalTodoCreateDTO;
import com.supplier.portal.entity.PortalTodo;
import com.supplier.portal.mapper.PortalTodoMapper;
import com.supplier.portal.query.PortalTodoQuery;
import com.supplier.portal.service.PortalTodoService;
import com.supplier.portal.vo.PortalTodoVO;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalTodoServiceImpl implements PortalTodoService {
    private final PortalTodoMapper mapper;

    private static final Map<String, String> MODULE_NAME_MAP = new HashMap<>();
    static {
        MODULE_NAME_MAP.put("purchase_order", "采购订单");
        MODULE_NAME_MAP.put("delivery_notice", "送货通知");
        MODULE_NAME_MAP.put("rfq", "询价单");
        MODULE_NAME_MAP.put("quote", "报价单");
        MODULE_NAME_MAP.put("reconciliation", "财务对账");
        MODULE_NAME_MAP.put("quality_inspection", "质量检验");
        MODULE_NAME_MAP.put("nonconformance_report", "不合格报告");
        MODULE_NAME_MAP.put("order_pending", "订单待确认");
        MODULE_NAME_MAP.put("delivery_delay", "送货逾期");
        MODULE_NAME_MAP.put("delivery_approaching", "送货即将到期");
        MODULE_NAME_MAP.put("order_overdue", "订单逾期");
        MODULE_NAME_MAP.put("rfq_deadline", "询价即将截止");
    }

    @Override
    public PageResult<PortalTodoVO> page(PortalTodoQuery query) {
        Page<PortalTodo> page = mapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), baseScope()
                .eq(StringUtils.hasText(query.getTodoType()), PortalTodo::getTodoType, query.getTodoType())
                .eq(StringUtils.hasText(query.getBusinessType()), PortalTodo::getBusinessType, query.getBusinessType())
                .eq(query.getTodoStatus() != null, PortalTodo::getTodoStatus, query.getTodoStatus())
                .orderByDesc(PortalTodo::getCreateTime));
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public Long unreadCount() {
        return mapper.selectCount(baseScope().eq(PortalTodo::getTodoStatus, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PortalTodoCreateDTO dto) {
        PortalTodo todo = new PortalTodo();
        todo.setUserId(dto.getUserId());
        todo.setSupplierId(dto.getSupplierId());
        todo.setTodoType(dto.getTodoType());
        todo.setBusinessType(dto.getBusinessType());
        todo.setBusinessId(dto.getBusinessId());
        todo.setBusinessNo(dto.getBusinessNo());
        todo.setTitle(dto.getTitle());
        todo.setTodoStatus(0);
        todo.setDueTime(dto.getDueTime());
        todo.setFinishTime(null);
        mapper.insert(todo);
        return todo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long id) {
        changeStatus(id, 1, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignore(Long id) {
        changeStatus(id, 2, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoFinishByBusiness(String businessType, Long businessId) {
        if (!StringUtils.hasText(businessType) || businessId == null) {
            return;
        }
        // 查找所有关联的未完成待办并自动完成
        LambdaQueryWrapper<PortalTodo> wrapper = baseScope()
                .eq(PortalTodo::getBusinessType, businessType)
                .eq(PortalTodo::getBusinessId, businessId)
                .eq(PortalTodo::getTodoStatus, 0);
        List<PortalTodo> todos = mapper.selectList(wrapper);
        LocalDateTime now = LocalDateTime.now();
        for (PortalTodo todo : todos) {
            todo.setTodoStatus(1);
            todo.setFinishTime(now);
            mapper.updateById(todo);
        }
        if (!todos.isEmpty()) {
            log.info("自动完成待办: businessType={}, businessId={}, count={}", businessType, businessId, todos.size());
        }
    }

    private void changeStatus(Long id, Integer status, boolean setFinishTime) {
        PortalTodo todo = mapper.selectById(id);
        if (todo == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        todo.setTodoStatus(status);
        if (setFinishTime) {
            todo.setFinishTime(LocalDateTime.now());
        }
        mapper.updateById(todo);
    }

    private LambdaQueryWrapper<PortalTodo> baseScope() {
        LambdaQueryWrapper<PortalTodo> wrapper = new LambdaQueryWrapper<>();
        if (SecurityUtils.isSupplierUser()) {
            wrapper.eq(PortalTodo::getSupplierId, SecurityUtils.getSupplierId());
        } else if (SecurityUtils.getUserId() != null) {
            wrapper.eq(PortalTodo::getUserId, SecurityUtils.getUserId());
        }
        return wrapper;
    }

    private PortalTodoVO toVO(PortalTodo e) {
        PortalTodoVO vo = new PortalTodoVO();
        vo.setId(e.getId()); vo.setUserId(e.getUserId()); vo.setSupplierId(e.getSupplierId()); vo.setTodoType(e.getTodoType()); vo.setBusinessType(e.getBusinessType()); vo.setBusinessId(e.getBusinessId()); vo.setBusinessNo(e.getBusinessNo()); vo.setTitle(e.getTitle()); vo.setModule(MODULE_NAME_MAP.getOrDefault(e.getBusinessType(), e.getBusinessType())); vo.setTodoStatus(e.getTodoStatus()); vo.setDueTime(e.getDueTime()); vo.setFinishTime(e.getFinishTime()); vo.setCreateTime(e.getCreateTime());
        return vo;
    }
}