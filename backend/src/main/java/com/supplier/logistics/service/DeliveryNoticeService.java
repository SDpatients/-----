package com.supplier.logistics.service;

import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.DeliveryActionDTO;
import com.supplier.logistics.dto.DeliveryNoticeCreateDTO;
import com.supplier.logistics.query.DeliveryNoticeQuery;
import com.supplier.logistics.vo.DeliveryDetailVO;
import com.supplier.logistics.vo.DeliveryNoticeVO;

import java.util.List;

public interface DeliveryNoticeService {
    PageResult<DeliveryNoticeVO> page(DeliveryNoticeQuery query);
    DeliveryNoticeVO getDetail(Long id);
    Long create(DeliveryNoticeCreateDTO dto);
    void ship(Long id, DeliveryActionDTO dto);
    void arrive(Long id, DeliveryActionDTO dto);

    /** 查询 ASN 明细行 */
    List<DeliveryDetailVO> getLines(Long id);

    /** 确认入库 */
    void warehousing(Long id);

    /** 触发质检 */
    void triggerQuality(Long id);

    /** 扫码收货确认 */
    void scanReceive(Long id, DeliveryActionDTO dto);
}
