package com.supplier.sourcing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.sourcing.dto.MaterialCreateDTO;
import com.supplier.sourcing.dto.MaterialUpdateDTO;
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
                .eq(StringUtils.hasText(query.getCategory()), MaterialInfo::getCategory, query.getCategory())
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

    @Override
    public Long create(MaterialCreateDTO dto) {
        checkDuplicateCode(dto.getMaterialCode(), null);
        MaterialInfo entity = new MaterialInfo();
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setSpec(dto.getSpec());
        entity.setUnit(dto.getUnit());
        entity.setCategory(dto.getCategory());
        entity.setStatus(1);
        materialInfoMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(MaterialUpdateDTO dto) {
        MaterialInfo material = materialInfoMapper.selectById(dto.getId());
        if (material == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        checkDuplicateCode(dto.getMaterialCode(), dto.getId());
        material.setMaterialCode(dto.getMaterialCode());
        material.setMaterialName(dto.getMaterialName());
        material.setSpec(dto.getSpec());
        material.setUnit(dto.getUnit());
        material.setCategory(dto.getCategory());
        materialInfoMapper.updateById(material);
    }

    @Override
    public void delete(Long id) {
        MaterialInfo material = materialInfoMapper.selectById(id);
        if (material == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        materialInfoMapper.deleteById(id);
    }

    private void checkDuplicateCode(String code, Long excludeId) {
        LambdaQueryWrapper<MaterialInfo> wrapper = new LambdaQueryWrapper<MaterialInfo>()
                .eq(MaterialInfo::getMaterialCode, code);
        if (excludeId != null) {
            wrapper.ne(MaterialInfo::getId, excludeId);
        }
        Long count = materialInfoMapper.selectCount(wrapper);
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "物料编码已存在");
        }
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