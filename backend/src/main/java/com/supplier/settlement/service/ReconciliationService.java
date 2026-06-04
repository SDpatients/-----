package com.supplier.settlement.service;

import com.supplier.common.result.PageResult;
import com.supplier.settlement.dto.ReconciliationConfirmDTO;
import com.supplier.settlement.dto.ReconciliationCreateDTO;
import com.supplier.settlement.query.ReconciliationQuery;
import com.supplier.settlement.vo.ReconciliationDetailVO;
import com.supplier.settlement.vo.ReconciliationVO;
import com.supplier.settlement.vo.ThreeWayMatchVO;

import java.util.List;
import java.util.Map;

public interface ReconciliationService {
    PageResult<ReconciliationVO> page(ReconciliationQuery query);
    ReconciliationVO getDetail(Long id);
    Long create(ReconciliationCreateDTO dto);
    void send(Long id);
    void confirm(Long id, ReconciliationConfirmDTO dto);

    /** 查询对账单明细行 */
    List<ReconciliationDetailVO> getLines(Long id);

    /** 冻结对账单 */
    void freeze(Long id, Map<String, Object> data);

    /** 解冻对账单 */
    void unfreeze(Long id, Map<String, Object> data);

    /** 三单匹配数据 */
    List<ThreeWayMatchVO> getThreeWayMatch(Long id);

    /** 查询可开票金额 */
    Map<String, Object> getInvoicableAmount(Long reconId);
}
