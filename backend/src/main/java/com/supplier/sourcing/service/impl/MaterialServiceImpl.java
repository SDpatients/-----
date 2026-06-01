package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.sourcing.entity.MaterialInfo;
import com.supplier.sourcing.mapper.MaterialInfoMapper;
import com.supplier.sourcing.query.MaterialQuery;
import com.supplier.sourcing.service.MaterialService;
import com.supplier.sourcing.vo.MaterialVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialInfoMapper materialInfoMapper;

    @Override
    public PageResult<MaterialVO> page(MaterialQuery query) {
        LambdaQueryWrapper<MaterialInfo> wrapper = new LambdaQueryWrapper<MaterialInfo>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(MaterialInfo::getMaterialCode, query.getKeyword())
                        .or()
                        .like(MaterialInfo::getMaterialName, query.getKeyword()))
                .orderByDesc(MaterialInfo::getCreateTime);
        Page<MaterialInfo> page = materialInfoMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        List<MaterialVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public MaterialVO getDetail(Long id) {
        MaterialInfo material = materialInfoMapper.selectById(id);
        if (material == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return toVO(material);
    }

    private MaterialVO toVO(MaterialInfo entity) {
        MaterialVO vo = new MaterialVO();
        vo.setId(entity.getId());
        vo.setMaterialCode(entity.getMaterialCode());
        vo.setMaterialName(entity.getMaterialName());
        vo.setSpec(entity.getSpec());
        vo.setUnit(entity.getUnit());
        vo.setCategory(entity.getCategory());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}