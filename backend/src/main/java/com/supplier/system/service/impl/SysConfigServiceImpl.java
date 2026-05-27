package com.supplier.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.supplier.system.entity.SysConfig;
import com.supplier.system.mapper.SysConfigMapper;
import com.supplier.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {
    private final SysConfigMapper mapper;

    @Override
    public List<SysConfig> listByType(Integer configType) {
        return mapper.selectList(new LambdaQueryWrapper<SysConfig>()
                .eq(configType != null, SysConfig::getConfigType, configType)
                .eq(SysConfig::getStatus, 1)
                .orderByAsc(SysConfig::getConfigKey));
    }

    @Override
    public Map<String, String> getMapByType(Integer configType) {
        return listByType(configType).stream()
                .collect(Collectors.toMap(SysConfig::getConfigKey, c -> c.getConfigValue() == null ? "" : c.getConfigValue(), (a, b) -> b, LinkedHashMap::new));
    }

    @Override
    public String getValue(String configKey) {
        SysConfig config = mapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .eq(SysConfig::getStatus, 1));
        return config == null ? null : config.getConfigValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(String configKey, String configValue, String configName, String remark) {
        SysConfig exist = mapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        if (exist != null) {
            if (!Objects.equals(exist.getConfigValue(), configValue) || !Objects.equals(exist.getConfigName(), configName)) {
                mapper.update(new LambdaUpdateWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, configKey)
                        .set(SysConfig::getConfigValue, configValue)
                        .set(SysConfig::getConfigName, configName)
                        .set(SysConfig::getRemark, remark));
            }
        } else {
            SysConfig entity = new SysConfig();
            entity.setConfigKey(configKey);
            entity.setConfigValue(configValue);
            entity.setConfigName(configName);
            entity.setConfigType(2);
            entity.setStatus(1);
            entity.setRemark(remark);
            mapper.insert(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<SysConfig> configs) {
        for (SysConfig c : configs) {
            saveOrUpdate(c.getConfigKey(), c.getConfigValue(), c.getConfigName(), c.getRemark());
        }
    }
}