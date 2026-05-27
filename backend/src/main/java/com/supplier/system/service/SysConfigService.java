package com.supplier.system.service;

import com.supplier.system.entity.SysConfig;
import java.util.List;
import java.util.Map;

public interface SysConfigService {
    List<SysConfig> listByType(Integer configType);
    Map<String, String> getMapByType(Integer configType);
    String getValue(String configKey);
    void saveOrUpdate(String configKey, String configValue, String configName, String remark);
    void batchSave(List<SysConfig> configs);
}