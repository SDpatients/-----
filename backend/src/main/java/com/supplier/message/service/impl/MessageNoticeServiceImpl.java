package com.supplier.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.PageResult;
import com.supplier.common.result.ResultCode;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.entity.MessageNotice;
import com.supplier.message.mapper.MessageNoticeMapper;
import com.supplier.message.query.MessageNoticeQuery;
import com.supplier.message.service.MessageNoticeService;
import com.supplier.message.vo.MessageNoticeVO;
import com.supplier.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MessageNoticeServiceImpl implements MessageNoticeService {
    private final MessageNoticeMapper mapper;

    @Override
    public PageResult<MessageNoticeVO> page(MessageNoticeQuery query) {
        Page<MessageNotice> page = mapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), baseScope()
                .eq(query.getChannel() != null, MessageNotice::getChannel, query.getChannel())
                .eq(query.getSendStatus() != null, MessageNotice::getSendStatus, query.getSendStatus())
                .eq(query.getReadStatus() != null, MessageNotice::getReadStatus, query.getReadStatus())
                .eq(StringUtils.hasText(query.getBusinessType()), MessageNotice::getBusinessType, query.getBusinessType())
                .orderByDesc(MessageNotice::getCreateTime));
        return PageResult.of(page.convert(this::toVO));
    }

    @Override
    public MessageNoticeVO getDetail(Long id) {
        return toVO(getById(id));
    }

    @Override
    public Long unreadCount() {
        return mapper.selectCount(baseScope().eq(MessageNotice::getReadStatus, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MessageNoticeCreateDTO dto) {
        MessageNotice entity = new MessageNotice();
        entity.setNoticeNo("MSG" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        entity.setReceiverUserId(dto.getReceiverUserId());
        entity.setReceiverSupplierId(dto.getReceiverSupplierId());
        entity.setChannel(dto.getChannel());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setBusinessType(dto.getBusinessType());
        entity.setBusinessId(dto.getBusinessId());
        entity.setSendStatus(1);
        entity.setReadStatus(0);
        entity.setSendTime(LocalDateTime.now());
        entity.setRetryCount(0);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id) {
        MessageNotice entity = getById(id);
        entity.setReadStatus(1);
        entity.setReadTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        LambdaUpdateWrapper<MessageNotice> uw = new LambdaUpdateWrapper<MessageNotice>()
                .set(MessageNotice::getReadStatus, 1)
                .set(MessageNotice::getReadTime, LocalDateTime.now())
                .eq(MessageNotice::getReadStatus, 0);
        if (SecurityUtils.isSupplierUser()) {
            uw.eq(MessageNotice::getReceiverSupplierId, SecurityUtils.getSupplierId());
        } else if (SecurityUtils.getUserId() != null) {
            uw.eq(MessageNotice::getReceiverUserId, SecurityUtils.getUserId());
        }
        mapper.update(uw);
    }

    private MessageNotice getById(Long id) {
        MessageNotice entity = mapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        return entity;
    }

    private LambdaQueryWrapper<MessageNotice> baseScope() {
        LambdaQueryWrapper<MessageNotice> wrapper = new LambdaQueryWrapper<>();
        if (SecurityUtils.isSupplierUser()) {
            wrapper.eq(MessageNotice::getReceiverSupplierId, SecurityUtils.getSupplierId());
        } else if (SecurityUtils.getUserId() != null) {
            wrapper.eq(MessageNotice::getReceiverUserId, SecurityUtils.getUserId());
        }
        return wrapper;
    }

    private MessageNoticeVO toVO(MessageNotice e) {
        MessageNoticeVO vo = new MessageNoticeVO();
        vo.setId(e.getId()); vo.setNoticeNo(e.getNoticeNo()); vo.setReceiverUserId(e.getReceiverUserId()); vo.setReceiverSupplierId(e.getReceiverSupplierId()); vo.setChannel(e.getChannel()); vo.setTitle(e.getTitle()); vo.setContent(e.getContent()); vo.setBusinessType(e.getBusinessType()); vo.setBusinessId(e.getBusinessId()); vo.setSendStatus(e.getSendStatus()); vo.setReadStatus(e.getReadStatus()); vo.setSendTime(e.getSendTime()); vo.setReadTime(e.getReadTime()); vo.setRetryCount(e.getRetryCount()); vo.setErrorMessage(e.getErrorMessage());
        return vo;
    }
}