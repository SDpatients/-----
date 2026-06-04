package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.dto.BargainDTO;
import com.supplier.sourcing.dto.QuoteCreateDTO;
import com.supplier.sourcing.dto.QuotePriceDTO;
import com.supplier.sourcing.dto.QuoteUpdateDTO;
import com.supplier.sourcing.query.QuoteQuery;
import com.supplier.sourcing.vo.BargainVO;
import com.supplier.sourcing.vo.QuoteNegotiationVO;
import com.supplier.sourcing.vo.QuoteVO;
import com.supplier.sourcing.vo.RfqSummaryVO;

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

    /** 更新报价单（含明细行） */
    void update(Long id, QuoteUpdateDTO dto);

    void bargain(Long id, BargainDTO dto);

    List<QuoteNegotiationVO> getNegotiations(Long quoteId);

    List<BargainVO> getBargains(Long quoteId);

    void resubmit(Long quoteId, QuoteUpdateDTO dto);

    /**
     * 列出有报价的询价单（用于报价对比页左侧列表）
     * 返回按最后报价时间倒序的询价单概要列表
     */
    List<RfqSummaryVO> listRfqWithQuotes();

    /**
     * 定价确认：采购方选中最终报价进行定价，自动生成采购订单和 QuoteAward 记录
     * 联动: sourcing → order（RFQ 定价 → 自动生成采购订单）
     */
    void price(Long id, QuotePriceDTO dto);
}