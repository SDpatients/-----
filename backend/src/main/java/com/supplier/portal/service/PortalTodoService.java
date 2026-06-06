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

    /**
     * 按业务单据自动完成待办：当用户完成该业务操作时，自动将该单据关联的待办标记为已完成
     * @param businessType 业务类型（如 purchase_order, delivery_notice）
     * @param businessId 业务单据ID
     */
    void autoFinishByBusiness(String businessType, Long businessId);
}
