package com.supplier.settlement.service;

import com.supplier.common.result.PageResult;
import com.supplier.settlement.dto.PaymentActionDTO;
import com.supplier.settlement.dto.PaymentCreateDTO;
import com.supplier.settlement.dto.PaymentScheduleDTO;
import com.supplier.settlement.query.PaymentQuery;
import com.supplier.settlement.vo.PaymentVO;

public interface PaymentService {

    PageResult<PaymentVO> page(PaymentQuery query);

    PaymentVO getDetail(Long id);

    Long create(PaymentCreateDTO dto);

    void submitForApproval(Long id);

    void schedule(Long id, PaymentScheduleDTO dto);

    void pay(Long id, PaymentActionDTO dto);

    void reject(Long id, PaymentActionDTO dto);

    void cancel(Long id, PaymentActionDTO dto);
}