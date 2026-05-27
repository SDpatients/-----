package com.supplier.sourcing.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.sourcing.converter.SupplierConverter;
import com.supplier.sourcing.dto.SupplierCreateDTO;
import com.supplier.sourcing.dto.SupplierRegisterDTO;
import com.supplier.sourcing.entity.SupplierBlacklist;
import com.supplier.sourcing.entity.SupplierInfo;
import com.supplier.sourcing.enums.SupplierStatusEnum;
import com.supplier.sourcing.mapper.SupplierBlacklistMapper;
import com.supplier.sourcing.mapper.SupplierInfoMapper;
import com.supplier.sourcing.query.SupplierQuery;
import com.supplier.sourcing.service.SupplierService;
import com.supplier.sourcing.vo.SupplierVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierInfoMapper supplierInfoMapper;
    private final SupplierBlacklistMapper supplierBlacklistMapper;

    @Override
    public PageResult<SupplierVO> page(SupplierQuery query) {
        LambdaQueryWrapper<SupplierInfo> wrapper = new LambdaQueryWrapper<SupplierInfo>()
                .eq(query.getStatus() != null, SupplierInfo::getStatus, query.getStatus())
                .eq(query.getSupplierType() != null, SupplierInfo::getSupplierType, query.getSupplierType())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(SupplierInfo::getSupplierName, query.getKeyword())
                        .or()
                        .like(SupplierInfo::getSupplierCode, query.getKeyword())
                        .or()
                        .like(SupplierInfo::getCreditCode, query.getKeyword()))
                .orderByDesc(SupplierInfo::getCreateTime);
        Page<SupplierInfo> page = supplierInfoMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.convert(SupplierConverter::toVO));
    }

    @Override
    public SupplierVO getDetail(Long id) {
        SupplierInfo supplier = supplierInfoMapper.selectById(id);
        if (supplier == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return SupplierConverter.toVO(supplier);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SupplierCreateDTO dto) {
        checkDuplicateCode(dto.getSupplierCode());
        checkDuplicateCreditCode(dto.getCreditCode());
        SupplierInfo entity = SupplierConverter.toEntity(dto);
        supplierInfoMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(SupplierRegisterDTO dto) {
        checkDuplicateCreditCode(dto.getCreditCode());
        checkBlacklistByCreditCode(dto.getCreditCode());

        SupplierInfo entity = new SupplierInfo();
        entity.setSupplierCode(generateSupplierCode());
        entity.setSupplierName(dto.getSupplierName());
        entity.setSupplierShortName(dto.getSupplierShortName());
        entity.setCreditCode(dto.getCreditCode());
        entity.setLegalPerson(dto.getLegalPerson());
        entity.setBusinessScope(dto.getBusinessScope());
        entity.setProvince(dto.getProvince());
        entity.setCity(dto.getCity());
        entity.setDistrict(dto.getDistrict());
        entity.setAddress(dto.getAddress());
        entity.setContactName(dto.getContactName());
        entity.setContactPhone(dto.getContactPhone());
        entity.setContactEmail(dto.getContactEmail());
        entity.setBankName(dto.getBankName());
        entity.setBankAccount(dto.getBankAccount());
        entity.setTaxNumber(dto.getTaxNumber());
        entity.setRemark(dto.getRemark());
        entity.setStatus(SupplierStatusEnum.APPROVED.getCode());
        entity.setRating(0);
        supplierInfoMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, Integer status) {
        SupplierInfo supplier = getSupplierOrThrow(id);
        supplier.setStatus(status);
        supplierInfoMapper.updateById(supplier);
    }

    // ==================== 内部私有方法 ====================

    private void checkDuplicateCode(String supplierCode) {
        Long count = supplierInfoMapper.selectCount(new LambdaQueryWrapper<SupplierInfo>()
                .eq(SupplierInfo::getSupplierCode, supplierCode));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "供应商编码已存在");
        }
    }

    private void checkDuplicateCreditCode(String creditCode) {
        if (StringUtils.hasText(creditCode)) {
            Long count = supplierInfoMapper.selectCount(new LambdaQueryWrapper<SupplierInfo>()
                    .eq(SupplierInfo::getCreditCode, creditCode));
            if (count > 0) {
                throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "统一社会信用代码已存在");
            }
        }
    }

    private void checkBlacklistByCreditCode(String creditCode) {
        if (StringUtils.hasText(creditCode)) {
            Long count = supplierBlacklistMapper.selectCount(
                    new LambdaQueryWrapper<SupplierBlacklist>()
                            .eq(SupplierBlacklist::getCreditCode, creditCode)
                            .eq(SupplierBlacklist::getStatus, 1));
            if (count > 0) {
                throw BusinessException.of(ResultCode.STATUS_NOT_ALLOWED.getCode(), "该供应商信用代码在黑名单中");
            }
        }
    }

    private String generateSupplierCode() {
        String code;
        do {
            code = "S" + RandomUtil.randomNumbers(6);
        } while (supplierInfoMapper.selectCount(
                new LambdaQueryWrapper<SupplierInfo>().eq(SupplierInfo::getSupplierCode, code)) > 0);
        return code;
    }

    private SupplierInfo getSupplierOrThrow(Long id) {
        SupplierInfo supplier = supplierInfoMapper.selectById(id);
        if (supplier == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return supplier;
    }
}