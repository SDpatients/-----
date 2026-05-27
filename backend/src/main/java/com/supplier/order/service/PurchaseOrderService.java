package com.supplier.order.service;

import com.supplier.common.result.PageResult;
import com.supplier.order.dto.BuyerConfirmDTO;
import com.supplier.order.dto.OrderActionDTO;
import com.supplier.order.dto.OrderCloseDTO;
import com.supplier.order.dto.PurchaseOrderCreateDTO;
import com.supplier.order.query.PurchaseOrderQuery;
import com.supplier.order.vo.PurchaseOrderVO;

import java.math.BigDecimal;

public interface PurchaseOrderService {

    PageResult<PurchaseOrderVO> page(PurchaseOrderQuery query);

    PurchaseOrderVO getDetail(Long id);

    Long create(PurchaseOrderCreateDTO dto);

    void publish(Long id, OrderActionDTO dto);

    void confirm(Long id, OrderActionDTO dto);

    void reject(Long id, OrderActionDTO dto);

    void cancel(Long id, OrderActionDTO dto);

    /**
     * 采购方确认供应商接单结果
     */
    void confirmByBuyer(Long id, BuyerConfirmDTO dto);

    /**
     * 关闭采购订单（手动/自动）
     */
    void close(Long id, OrderCloseDTO dto);

    /**
     * 校验订单明细某次收货数量是否合法（deliveredQty + 本次数量 <= quantity）
     */
    void validateDeliveryQuantity(Long orderDetailId, BigDecimal newDeliveryQty);
}
