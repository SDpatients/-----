package com.supplier.settlement.service;

import com.supplier.settlement.dto.ThreeWayMatchDTO;
import com.supplier.settlement.vo.ThreeWayMatchVO;

import java.util.List;

/**
 * 三单匹配服务（联动7: 三单匹配 → 自动匹配订单/收货/发票）
 */
public interface ThreeWayMatchService {

    /**
     * 执行三单匹配：自动比对采购订单、收货记录、发票
     * 匹配规则: 订单金额 >= 收货金额 >= 发票金额
     */
    List<ThreeWayMatchVO> execute(ThreeWayMatchDTO dto);
}