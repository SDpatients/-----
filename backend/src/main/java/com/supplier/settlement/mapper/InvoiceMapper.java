package com.supplier.settlement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplier.settlement.entity.Invoice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InvoiceMapper extends BaseMapper<Invoice> {
}