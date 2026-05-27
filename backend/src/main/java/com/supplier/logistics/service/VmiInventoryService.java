package com.supplier.logistics.service;

import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.VmiInventorySyncDTO;
import com.supplier.logistics.query.VmiInventoryQuery;
import com.supplier.logistics.vo.VmiInventoryVO;

public interface VmiInventoryService {

    PageResult<VmiInventoryVO> page(VmiInventoryQuery query);

    VmiInventoryVO getDetail(Long id);

    void sync(VmiInventorySyncDTO dto);
}