package com.supplier.settlement.service;

import com.supplier.common.result.PageResult;
import com.supplier.settlement.dto.PaymentApprovalActionDTO;
import com.supplier.settlement.dto.PaymentApprovalSubmitDTO;
import com.supplier.settlement.query.PaymentApprovalQuery;
import com.supplier.settlement.vo.PaymentApprovalVO;

public interface PaymentApprovalService {

    PageResult<PaymentApprovalVO> page(PaymentApprovalQuery query);

    PaymentApprovalVO getDetail(Long id);

    void submit(PaymentApprovalSubmitDTO dto);

    void approve(Long id, PaymentApprovalActionDTO dto);

    void reject(Long id, PaymentApprovalActionDTO dto);
}