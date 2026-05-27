package com.supplier.quality.service;

import com.supplier.common.result.PageResult;
import com.supplier.quality.dto.AppealCreateDTO;
import com.supplier.quality.dto.AppealReviewDTO;
import com.supplier.quality.query.AppealQuery;
import com.supplier.quality.vo.AppealVO;

/**
 * 质检申诉服务（联动9: 质检申诉 → 申诉通过后关联扣款调整）
 */
public interface AppealService {

    PageResult<AppealVO> page(AppealQuery query);

    AppealVO getDetail(Long id);

    Long create(AppealCreateDTO dto);

    void submit(Long id);

    /**
     * 申诉审核通过：自动调整关联扣款金额
     * 联动: quality → settlement（申诉通过 → 扣款调整）
     */
    void approve(Long id, AppealReviewDTO dto);

    void reject(Long id, AppealReviewDTO dto);
}