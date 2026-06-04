package com.supplier.logistics.service;

import com.supplier.common.result.PageResult;
import com.supplier.logistics.dto.WriteOffCreateDTO;
import com.supplier.logistics.query.WriteOffQuery;
import com.supplier.logistics.vo.WriteOffVO;

public interface WriteOffService {
    PageResult<WriteOffVO> page(WriteOffQuery query);
    Long create(WriteOffCreateDTO dto);
}
