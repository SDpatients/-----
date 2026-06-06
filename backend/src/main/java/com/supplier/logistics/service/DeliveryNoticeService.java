package com.supplier.logistics.service;

import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.DeliveryActionDTO;
import com.supplier.logistics.dto.DeliveryNoticeCreateDTO;
import com.supplier.logistics.dto.DeliveryNoticeExportRequest;
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

    /** 扫码收货确认 */
    void scanReceive(Long id, DeliveryActionDTO dto);

    /** 确认收货（已送达 -> 已收货） */
    void confirmReceive(Long id, DeliveryActionDTO dto);

    /** 拒收（已送达 -> 已拒收） */
    void rejectReceive(Long id, DeliveryActionDTO dto);

    /** 导出 ASN 数据，返回导出任务ID */
    Long export(DeliveryNoticeExportRequest request);
}
