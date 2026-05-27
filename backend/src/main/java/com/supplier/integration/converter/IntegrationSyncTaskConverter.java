package com.supplier.integration.converter;

import com.supplier.integration.entity.IntegrationSyncTask;
import com.supplier.integration.vo.IntegrationSyncTaskVO;

public class IntegrationSyncTaskConverter {

    public static IntegrationSyncTaskVO toVO(IntegrationSyncTask entity) {
        IntegrationSyncTaskVO vo = new IntegrationSyncTaskVO();
        vo.setId(entity.getId());
        vo.setTaskNo(entity.getTaskNo());
        vo.setSystemType(entity.getSystemType());
        vo.setTaskType(entity.getTaskType());
        vo.setExternalNo(entity.getExternalNo());
        vo.setEventType(entity.getEventType());
        vo.setPayload(entity.getPayload());
        vo.setTaskStatus(entity.getTaskStatus());
        vo.setRetryCount(entity.getRetryCount());
        vo.setNextRetryTime(entity.getNextRetryTime());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}