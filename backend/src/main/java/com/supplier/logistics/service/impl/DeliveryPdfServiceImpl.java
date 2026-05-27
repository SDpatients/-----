package com.supplier.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.logistics.entity.DeliveryBarcode;
import com.supplier.logistics.entity.DeliveryDetail;
import com.supplier.logistics.entity.DeliveryNotice;
import com.supplier.logistics.entity.DeliveryPackage;
import com.supplier.logistics.mapper.DeliveryBarcodeMapper;
import com.supplier.logistics.mapper.DeliveryDetailMapper;
import com.supplier.logistics.mapper.DeliveryNoticeMapper;
import com.supplier.logistics.mapper.DeliveryPackageMapper;
import com.supplier.logistics.service.DeliveryPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryPdfServiceImpl implements DeliveryPdfService {

    private final DeliveryNoticeMapper noticeMapper;
    private final DeliveryDetailMapper detailMapper;
    private final DeliveryPackageMapper packageMapper;
    private final DeliveryBarcodeMapper barcodeMapper;

    @Override
    public byte[] generateDeliveryNotePdf(Long noticeId) {
        DeliveryNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        List<DeliveryDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<DeliveryDetail>().eq(DeliveryDetail::getNoticeId, noticeId));

        // 生成简单的文本格式送货单（实际项目可集成 iText/PDFBox 生成标准 PDF）
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("            送  货  单\n");
        sb.append("========================================\n");
        sb.append("送货单号: ").append(notice.getNoticeNo()).append("\n");
        sb.append("订单号:   ").append(notice.getOrderNo()).append("\n");
        sb.append("供应商:   ").append(notice.getSupplierName()).append("\n");
        sb.append("计划日期: ").append(notice.getPlanDeliveryDate() != null
                ? notice.getPlanDeliveryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "").append("\n");
        sb.append("发货方式: ").append(notice.getDeliveryMethod() != null ? notice.getDeliveryMethod() : "").append("\n");
        sb.append("收货地址: ").append(notice.getDeliveryAddress() != null ? notice.getDeliveryAddress() : "").append("\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-20s %-10s %-10s\n", "物料名称", "单位", "实发数量"));
        sb.append("----------------------------------------\n");
        for (DeliveryDetail d : details) {
            sb.append(String.format("%-20s %-10s %-10s\n",
                    d.getMaterialName() != null ? d.getMaterialName() : "",
                    d.getUnit() != null ? d.getUnit() : "",
                    d.getActualQty() != null ? d.getActualQty().toPlainString() : "0"));
        }
        sb.append("----------------------------------------\n");
        sb.append("批次号:   ").append(notice.getBatchNo() != null ? notice.getBatchNo() : "").append("\n");
        sb.append("生产日期: ").append(notice.getProductionDate() != null
                ? notice.getProductionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "").append("\n");
        sb.append("有效期至: ").append(notice.getExpiryDate() != null
                ? notice.getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "").append("\n");
        sb.append("备注:     ").append(notice.getRemark() != null ? notice.getRemark() : "").append("\n");
        sb.append("========================================\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] generatePackageLabel(Long packageId) {
        DeliveryPackage pack = packageMapper.selectById(packageId);
        if (pack == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        DeliveryBarcode barcode = barcodeMapper.selectOne(
                new LambdaQueryWrapper<DeliveryBarcode>().eq(DeliveryBarcode::getPackageId, packageId));

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("            箱  标  签\n");
        sb.append("========================================\n");
        sb.append("箱号:     ").append(pack.getPackageNo()).append("\n");
        sb.append("箱类型:   ").append(pack.getPackageType() != null ? pack.getPackageType() : "").append("\n");
        sb.append("重量:     ").append(pack.getWeight() != null ? pack.getWeight().toPlainString() : "").append("\n");
        sb.append("体积:     ").append(pack.getVolume() != null ? pack.getVolume().toPlainString() : "").append("\n");
        sb.append("条码:     ").append(barcode != null ? barcode.getBarcode() : "未生成").append("\n");
        sb.append("条码类型: ").append(barcode != null ? barcode.getBarcodeType() : "").append("\n");
        sb.append("========================================\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}