package com.supplier.logistics.service;

import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.ReceiptAdjustDTO;
import com.supplier.logistics.dto.ReceiptConfirmDTO;
import com.supplier.logistics.dto.ReceiptCreateDTO;
import com.supplier.logistics.query.ReceiptRecordQuery;
import com.supplier.logistics.vo.ReceiptRecordVO;

public interface ReceiptRecordService {
    PageResult<ReceiptRecordVO> page(ReceiptRecordQuery query);
    ReceiptRecordVO getDetail(Long id);
    Long create(ReceiptCreateDTO dto);
    void confirm(Long id, ReceiptConfirmDTO dto);
    void reject(Long id, ReceiptConfirmDTO dto);
    /** 扫码收货 */
    ReceiptRecordVO scan(String barcode);
    /** 收货调整 */
    Long adjust(Long id, ReceiptAdjustDTO dto);
}
