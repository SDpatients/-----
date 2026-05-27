package com.supplier.common.service.impl;

import com.supplier.common.entity.BizStatusTrack;
import com.supplier.common.mapper.BizStatusTrackMapper;
import com.supplier.common.service.BizStatusTrackService;
import com.supplier.security.model.LoginUser;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BizStatusTrackServiceImpl implements BizStatusTrackService {

    private final BizStatusTrackMapper bizStatusTrackMapper;

    @Override
    public void writeTrack(String businessType, Long businessId, Integer beforeStatus, Integer afterStatus, String remark) {
        BizStatusTrack track = buildTrack(businessType, businessId, afterStatus, remark);
        track.setBeforeStatus(beforeStatus);
        track.setAfterStatus(afterStatus);
        bizStatusTrackMapper.insert(track);
    }

    @Override
    public void writeTrack(String businessType, Long businessId, Integer afterStatus, String remark) {
        BizStatusTrack track = buildTrack(businessType, businessId, afterStatus, remark);
        track.setBeforeStatus(null);
        track.setAfterStatus(afterStatus);
        bizStatusTrackMapper.insert(track);
    }

    private BizStatusTrack buildTrack(String businessType, Long businessId, Integer afterStatus, String remark) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        BizStatusTrack track = new BizStatusTrack();
        track.setBusinessType(businessType);
        track.setBusinessId(businessId);
        track.setTrackRemark(remark);
        track.setOperator(loginUser == null ? null : loginUser.getUserId());
        track.setOperatorName(loginUser == null ? null : loginUser.getRealName());
        track.setOperateTime(LocalDateTime.now());
        return track;
    }
}