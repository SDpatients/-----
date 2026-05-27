package com.supplier.logistics.service;

import com.supplier.logistics.dto.DeliveryPackageCreateDTO;
import com.supplier.logistics.vo.DeliveryBarcodeVO;
import com.supplier.logistics.vo.DeliveryPackageVO;

import java.util.List;

public interface DeliveryPackageService {
    List<DeliveryPackageVO> listByNoticeId(Long noticeId);
    DeliveryPackageVO getDetail(Long id);
    Long create(DeliveryPackageCreateDTO dto);
    List<DeliveryBarcodeVO> generateBarcodes(Long packageId);
    void printLabel(Long packageId);
}