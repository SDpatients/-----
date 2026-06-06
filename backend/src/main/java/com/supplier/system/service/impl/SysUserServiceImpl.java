package com.supplier.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.security.util.SecurityUtils;
import com.supplier.system.dto.SysUserCreateDTO;
import com.supplier.system.dto.SysUserPasswordDTO;
import com.supplier.system.entity.SysUser;
import com.supplier.system.mapper.SysUserMapper;
import com.supplier.system.query.SysUserQuery;
import com.supplier.system.service.SysUserService;
import com.supplier.system.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<SysUserVO> page(SysUserQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getSupplierId, query.getSupplierId())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(SysUser::getUsername, query.getKeyword())
                        .or()
                        .like(SysUser::getRealName, query.getKeyword())
                        .or()
                        .like(SysUser::getPhone, query.getKeyword()))
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        List<SysUserVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public SysUserVO getDetail(Long id) {
        SysUser user = getById(id);
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SysUserCreateDTO dto) {
        checkDuplicateUsername(dto.getUsername());

        SysUser entity = new SysUser();
        entity.setUsername(dto.getUsername());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setRealName(dto.getRealName());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setSupplierId(dto.getSupplierId());
        entity.setUserType(dto.getUserType() != null ? dto.getUserType() : 2);
        entity.setStatus(1);
        entity.setRemark(dto.getRemark());
        entity.setTokenVersion(0);
        sysUserMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, SysUserPasswordDTO dto) {
        SysUser user = getById(id);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setTokenVersion((user.getTokenVersion() != null ? user.getTokenVersion() : 0) + 1);
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, Integer status) {
        SysUser user = getById(id);
        user.setStatus(status);
        user.setTokenVersion((user.getTokenVersion() != null ? user.getTokenVersion() : 0) + 1);
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysUser user = getById(id);

        // 供应商用户只能删除自己供应商下的账号
        if (SecurityUtils.isSupplierUser()) {
            Long currentSupplierId = SecurityUtils.getSupplierId();
            if (!user.getSupplierId().equals(currentSupplierId)) {
                throw BusinessException.of(ResultCode.FORBIDDEN.getCode(), "无权限删除该账号");
            }
        }

        // 检查是否至少保留一个账号
        Long supplierId = user.getSupplierId();
        LambdaQueryWrapper<SysUser> countWrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getSupplierId, supplierId);
        Long accountCount = sysUserMapper.selectCount(countWrapper);
        if (accountCount <= 1) {
            throw BusinessException.of(ResultCode.BUSINESS_ERROR.getCode(), "至少需要保留一个供应商账号");
        }

        sysUserMapper.deleteById(id);
    }

    private SysUser getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "账号不存在");
        }
        return user;
    }

    private void checkDuplicateUsername(String username) {
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (count > 0) {
            throw BusinessException.of(ResultCode.DUPLICATE_SUBMIT.getCode(), "用户名已存在");
        }
    }

    private SysUserVO toVO(SysUser entity) {
        SysUserVO vo = new SysUserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setRealName(entity.getRealName());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setUserType(entity.getUserType());
        vo.setSupplierId(entity.getSupplierId());
        vo.setStatus(entity.getStatus());
        vo.setLastLoginTime(entity.getLastLoginTime());
        vo.setCreateTime(entity.getCreateTime());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}