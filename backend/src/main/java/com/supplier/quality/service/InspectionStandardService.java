package com.supplier.quality.service;

import com.supplier.common.result.PageResult;
import com.supplier.quality.dto.InspectionStandardCreateDTO;
import com.supplier.quality.dto.InspectionStandardItemDTO;
import com.supplier.quality.dto.InspectionStandardUpdateDTO;
import com.supplier.quality.query.InspectionStandardQuery;
import com.supplier.quality.vo.InspectionStandardItemVO;
import com.supplier.quality.vo.InspectionStandardVO;

import java.util.List;

public interface InspectionStandardService {

    PageResult<InspectionStandardVO> page(InspectionStandardQuery query);

    InspectionStandardVO getDetail(Long id);

    Long create(InspectionStandardCreateDTO dto);

    void update(InspectionStandardUpdateDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    InspectionStandardItemVO addItem(InspectionStandardItemDTO dto);

    void updateItem(InspectionStandardItemDTO dto);

    void deleteItem(Long itemId);

    List<InspectionStandardItemVO> getItemsByStandardId(Long standardId);

    InspectionStandardVO getByMaterialCode(String materialCode);
}