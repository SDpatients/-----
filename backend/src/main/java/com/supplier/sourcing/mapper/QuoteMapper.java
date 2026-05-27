package com.supplier.sourcing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supplier.sourcing.entity.Quote;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuoteMapper extends BaseMapper<Quote> {
}