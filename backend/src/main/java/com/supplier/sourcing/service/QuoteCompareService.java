package com.supplier.sourcing.service;

import com.supplier.sourcing.vo.QuoteCompareVO;

public interface QuoteCompareService {

    QuoteCompareVO compare(Long rfqId);
}