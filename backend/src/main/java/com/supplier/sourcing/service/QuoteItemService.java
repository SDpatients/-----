package com.supplier.sourcing.service;

import com.supplier.sourcing.vo.QuoteItemVO;

import java.util.List;

public interface QuoteItemService {

    List<QuoteItemVO> getByQuoteId(Long quoteId);
}