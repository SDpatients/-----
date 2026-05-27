package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.sourcing.converter.ExchangeRateConverter;
import com.supplier.sourcing.dto.ExchangeRateCreateDTO;
import com.supplier.sourcing.dto.ExchangeRateUpdateDTO;
import com.supplier.sourcing.entity.ExchangeRate;
import com.supplier.sourcing.mapper.ExchangeRateMapper;
import com.supplier.sourcing.query.ExchangeRateQuery;
import com.supplier.sourcing.service.ExchangeRateService;
import com.supplier.sourcing.vo.ExchangeRateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateMapper exchangeRateMapper;

    @Override
    public PageResult<ExchangeRateVO> page(ExchangeRateQuery query) {
        LambdaQueryWrapper<ExchangeRate> wrapper = new LambdaQueryWrapper<ExchangeRate>()
                .eq(query.getFromCurrency() != null, ExchangeRate::getFromCurrency, query.getFromCurrency())
                .eq(query.getToCurrency() != null, ExchangeRate::getToCurrency, query.getToCurrency())
                .orderByDesc(ExchangeRate::getEffectiveDate);
        Page<ExchangeRate> page = exchangeRateMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(ExchangeRateConverter::toVO));
    }

    @Override
    public ExchangeRateVO getDetail(Long id) {
        ExchangeRate entity = exchangeRateMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return ExchangeRateConverter.toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ExchangeRateCreateDTO dto) {
        ExchangeRate entity = ExchangeRateConverter.toEntity(dto);
        exchangeRateMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ExchangeRateUpdateDTO dto) {
        ExchangeRate entity = exchangeRateMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        ExchangeRateConverter.updateEntity(entity, dto);
        exchangeRateMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ExchangeRate entity = exchangeRateMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        exchangeRateMapper.deleteById(entity.getId());
    }

    @Override
    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        ExchangeRate entity = exchangeRateMapper.selectOne(
                new LambdaQueryWrapper<ExchangeRate>()
                        .eq(ExchangeRate::getFromCurrency, fromCurrency)
                        .eq(ExchangeRate::getToCurrency, toCurrency)
                        .le(ExchangeRate::getEffectiveDate, LocalDate.now())
                        .orderByDesc(ExchangeRate::getEffectiveDate)
                        .last("LIMIT 1")
        );
        return entity != null ? entity.getRate() : BigDecimal.ONE;
    }
}