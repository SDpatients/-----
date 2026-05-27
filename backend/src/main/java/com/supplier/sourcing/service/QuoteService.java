package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.dto.BargainDTO;
import com.supplier.sourcing.dto.QuoteCreateDTO;
import com.supplier.sourcing.dto.QuotePriceDTO;
import com.supplier.sourcing.query.QuoteQuery;
import com.supplier.sourcing.vo.QuoteNegotiationVO;
import com.supplier.sourcing.vo.QuoteVO;

import java.util.List;

public interface QuoteService {

    PageResult<QuoteVO> page(QuoteQuery query);

    QuoteVO getDetail(Long id);

    Long create(QuoteCreateDTO dto);

    void submit(Long id);

    void withdraw(Long id);

    void accept(Long id);

    void reject(Long id);

    void delete(Long id);

    void bargain(Long id, BargainDTO dto);

    List<QuoteNegotiationVO> getNegotiations(Long quoteId);

    /**
     * 定价确认：采购方选中最终报价进行定价，自动生成采购订单和 QuoteAward 记录
     * 联动: sourcing → order（RFQ 定价 → 自动生成采购订单）
     */
    void price(Long id, QuotePriceDTO dto);
}