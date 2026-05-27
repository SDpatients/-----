package com.supplier.logistics.service;

import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.ReceiptAdjustDTO;
import com.supplier.logistics.dto.ReceiptDiffApproveDTO;
import com.supplier.logistics.query.ReceiptDiffQuery;
import com.supplier.logistics.vo.ReceiptDiffVO;

public interface ReceiptDiffService {
    PageResult<ReceiptDiffVO> page(ReceiptDiffQuery query);
    ReceiptDiffVO getDetail(Long id);
    Long adjust(Long recordId, ReceiptAdjustDTO dto);

    /** 审批通过收货差异调整 */
    void approve(Long id, ReceiptDiffApproveDTO dto);

    /** 驳回收货差异调整 */
    void reject(Long id, ReceiptDiffApproveDTO dto);
}