package com.supplier.portal.service;

import com.supplier.common.result.PageResult;
import com.supplier.portal.dto.PortalTodoCreateDTO;
import com.supplier.portal.query.PortalTodoQuery;
import com.supplier.portal.vo.PortalTodoVO;

public interface PortalTodoService {
    PageResult<PortalTodoVO> page(PortalTodoQuery query);
    Long unreadCount();
    Long create(PortalTodoCreateDTO dto);
    void finish(Long id);
    void ignore(Long id);
}
