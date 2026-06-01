package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.query.MaterialQuery;
import com.supplier.sourcing.vo.MaterialVO;

public interface MaterialService {

    PageResult<MaterialVO> page(MaterialQuery query);

    MaterialVO getDetail(Long id);
}