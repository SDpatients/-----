package com.supplier.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.logistics.converter.ForecastDemandConverter;
import com.supplier.logistics.dto.DemandActionDTO;
import com.supplier.logistics.dto.ForecastDemandCreateDTO;
import com.supplier.logistics.entity.ForecastDemand;
import com.supplier.logistics.mapper.ForecastDemandMapper;
import com.supplier.logistics.query.ForecastDemandQuery;
import com.supplier.logistics.service.ForecastDemandService;
import com.supplier.logistics.vo.ForecastDemandVO;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ForecastDemandServiceImpl implements ForecastDemandService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int STATUS_RESPONDED = 2;
    private static final int STATUS_CLOSED = 3;

    private final ForecastDemandMapper forecastDemandMapper;

    @Override
    public PageResult<ForecastDemandVO> page(ForecastDemandQuery query) {
        Long supplierId = resolveSupplierId(query.getSupplierId());
        LambdaQueryWrapper<ForecastDemand> wrapper = new LambdaQueryWrapper<ForecastDemand>()
                .eq(supplierId != null, ForecastDemand::getSupplierId, supplierId)
                .eq(StringUtils.hasText(query.getMaterialCode()), ForecastDemand::getMaterialCode, query.getMaterialCode())
                .eq(query.getDemandType() != null, ForecastDemand::getDemandType, query.getDemandType())
                .eq(query.getDemandStatus() != null, ForecastDemand::getDemandStatus, query.getDemandStatus())
                .ge(query.getStartDate() != null, ForecastDemand::getDemandDate, query.getStartDate())
                .le(query.getEndDate() != null, ForecastDemand::getDemandDate, query.getEndDate())
                .orderByDesc(ForecastDemand::getDemandDate)
                .orderByDesc(ForecastDemand::getCreateTime);
        Page<ForecastDemand> page = forecastDemandMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(ForecastDemandConverter::toVO));
    }

    @Override
    public ForecastDemandVO getDetail(Long id) {
        ForecastDemand demand = getDemandWithDataScope(id);
        return ForecastDemandConverter.toVO(demand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ForecastDemandCreateDTO dto) {
        ForecastDemand demand = ForecastDemandConverter.toEntity(dto);
        demand.setDemandStatus(STATUS_PENDING);
        forecastDemandMapper.insert(demand);
        return demand.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id, DemandActionDTO dto) {
        dto = normalizeAction(dto);
        ForecastDemand demand = getDemandWithDataScope(id);
        if (!Integer.valueOf(STATUS_PENDING).equals(demand.getDemandStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有待处理的需求可以发布");
        }
        demand.setDemandStatus(STATUS_PUBLISHED);
        forecastDemandMapper.updateById(demand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void respond(Long id, DemandActionDTO dto) {
        dto = normalizeAction(dto);
        ForecastDemand demand = getDemandWithDataScope(id);
        if (!Integer.valueOf(STATUS_PUBLISHED).equals(demand.getDemandStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "只有已发布的需求可以响应");
        }
        demand.setDemandStatus(STATUS_RESPONDED);
        forecastDemandMapper.updateById(demand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id, DemandActionDTO dto) {
        dto = normalizeAction(dto);
        ForecastDemand demand = getDemandWithDataScope(id);
        if (Integer.valueOf(STATUS_CLOSED).equals(demand.getDemandStatus())) {
            throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "需求已关闭");
        }
        demand.setDemandStatus(STATUS_CLOSED);
        forecastDemandMapper.updateById(demand);
    }

    private ForecastDemand getDemandWithDataScope(Long id) {
        ForecastDemand demand = forecastDemandMapper.selectById(id);
        if (demand == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        if (SecurityUtils.isSupplierUser() && !demand.getSupplierId().equals(SecurityUtils.getSupplierId())) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return demand;
    }

    private Long resolveSupplierId(Long querySupplierId) {
        if (!SecurityUtils.isSupplierUser()) {
            return querySupplierId;
        }
        Long supplierId = SecurityUtils.getSupplierId();
        if (supplierId == null) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN.getCode(), "供应商用户未绑定供应商");
        }
        if (querySupplierId != null && !supplierId.equals(querySupplierId)) {
            throw BusinessException.of(ResultCode.DATA_FORBIDDEN);
        }
        return supplierId;
    }

    private DemandActionDTO normalizeAction(DemandActionDTO dto) {
        return dto == null ? new DemandActionDTO() : dto;
    }
}