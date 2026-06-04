package com.supplier.settlement.service;

import com.supplier.common.result.PageResult;
import com.supplier.settlement.dto.PaymentActionDTO;
import com.supplier.settlement.dto.PaymentCreateDTO;
import com.supplier.settlement.dto.PaymentScheduleDTO;
import com.supplier.settlement.query.PaymentQuery;
import com.supplier.settlement.vo.InvoiceVO;
import com.supplier.settlement.vo.PaymentVO;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    PageResult<PaymentVO> page(PaymentQuery query);

    PaymentVO getDetail(Long id);

    Long create(PaymentCreateDTO dto);

    void submitForApproval(Long id);

    void schedule(Long id, PaymentScheduleDTO dto);

    void pay(Long id, PaymentActionDTO dto);

    void reject(Long id, PaymentActionDTO dto);

    void cancel(Long id, PaymentActionDTO dto);

    /** 获取付款回传状态日志 */
    List<Map<String, Object>> getCallbackLogs(Long id);

    /** 获取付款关联的发票记录 */
    List<InvoiceVO> getLinkedInvoices(Long id);
}