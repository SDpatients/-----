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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PortalTodoServiceImpl implements PortalTodoService {
    private final PortalTodoMapper mapper;

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
        vo.setId(e.getId()); vo.setUserId(e.getUserId()); vo.setSupplierId(e.getSupplierId()); vo.setTodoType(e.getTodoType()); vo.setBusinessType(e.getBusinessType()); vo.setBusinessId(e.getBusinessId()); vo.setBusinessNo(e.getBusinessNo()); vo.setTitle(e.getTitle()); vo.setTodoStatus(e.getTodoStatus()); vo.setDueTime(e.getDueTime()); vo.setFinishTime(e.getFinishTime()); vo.setCreateTime(e.getCreateTime());
        return vo;
    }
}