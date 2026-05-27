package com.supplier.common.service;

public interface BizStatusTrackService {
    void writeTrack(String businessType, Long businessId, Integer beforeStatus, Integer afterStatus, String remark);
    void writeTrack(String businessType, Long businessId, Integer afterStatus, String remark);
}