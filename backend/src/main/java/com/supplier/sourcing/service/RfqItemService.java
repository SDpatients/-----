package com.supplier.sourcing.service;

import com.supplier.common.result.PageResult;
import com.supplier.sourcing.dto.RfqItemCreateDTO;
import com.supplier.sourcing.dto.RfqItemUpdateDTO;
import com.supplier.sourcing.query.RfqItemQuery;
import com.supplier.sourcing.vo.RfqItemVO;

import java.util.List;

public interface RfqItemService {

    PageResult<RfqItemVO> page(RfqItemQuery query);

    RfqItemVO getDetail(Long id);

    Long create(RfqItemCreateDTO dto);

    void update(Long id, RfqItemUpdateDTO dto);

    void delete(Long id);

    List<RfqItemVO> getByRfqId(Long rfqId);

    void saveLines(Long rfqId, List<RfqItemUpdateDTO> lines);
}