package com.supplier.system.service;

import com.supplier.common.result.PageResult;
import com.supplier.system.dto.SysUserCreateDTO;
import com.supplier.system.dto.SysUserPasswordDTO;
import com.supplier.system.query.SysUserQuery;
import com.supplier.system.vo.SysUserVO;

public interface SysUserService {

    PageResult<SysUserVO> page(SysUserQuery query);

    SysUserVO getDetail(Long id);

    Long create(SysUserCreateDTO dto);

    void resetPassword(Long id, SysUserPasswordDTO dto);

    void toggleStatus(Long id, Integer status);
}