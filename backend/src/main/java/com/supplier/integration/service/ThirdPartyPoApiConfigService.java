package com.supplier.integration.service;

import com.supplier.integration.dto.ThirdPartyPoApiConfigCreateDTO;
import com.supplier.integration.dto.ThirdPartyPoApiConfigUpdateDTO;
import com.supplier.integration.vo.ThirdPartyPoApiConfigVO;

import java.util.List;

public interface ThirdPartyPoApiConfigService {

    List<ThirdPartyPoApiConfigVO> list(String apiType);

    ThirdPartyPoApiConfigVO getDetail(Long id);

    Long create(ThirdPartyPoApiConfigCreateDTO dto);

    void update(Long id, ThirdPartyPoApiConfigUpdateDTO dto);

    void delete(Long id);

    void enable(Long id);

    void disable(Long id);
}