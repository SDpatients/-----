package com.supplier.settlement.service;

import com.supplier.common.result.PageResult;
import com.supplier.settlement.dto.InvoiceActionDTO;
import com.supplier.settlement.dto.InvoiceCreateDTO;
import com.supplier.settlement.dto.InvoiceUploadDTO;
import com.supplier.settlement.query.InvoiceQuery;
import com.supplier.settlement.vo.InvoiceVO;

public interface InvoiceService {

    PageResult<InvoiceVO> page(InvoiceQuery query);

    InvoiceVO getDetail(Long id);

    Long create(InvoiceCreateDTO dto);

    void upload(Long id, InvoiceUploadDTO dto);

    void verify(Long id, InvoiceActionDTO dto);

    void certify(Long id, InvoiceActionDTO dto);

    void voidInvoice(Long id, InvoiceActionDTO dto);
}