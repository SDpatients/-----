package com.supplier.logistics.service;

import com.supplier.logistics.vo.DeliveryNoticeVO;

public interface DeliveryPdfService {
    byte[] generateDeliveryNotePdf(Long noticeId);
    byte[] generatePackageLabel(Long packageId);
}