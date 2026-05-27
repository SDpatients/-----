package com.supplier.message.service;

import com.supplier.common.result.PageResult;
import com.supplier.message.dto.MessageNoticeCreateDTO;
import com.supplier.message.query.MessageNoticeQuery;
import com.supplier.message.vo.MessageNoticeVO;

public interface MessageNoticeService {
    PageResult<MessageNoticeVO> page(MessageNoticeQuery query);
    MessageNoticeVO getDetail(Long id);
    Long unreadCount();
    Long create(MessageNoticeCreateDTO dto);
    void markRead(Long id);
    void markAllRead();
}
