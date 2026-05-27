package com.supplier.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.common.exception.BusinessException;
import com.supplier.common.result.ResultCode;
import com.supplier.logistics.dto.DeliveryPackageCreateDTO;
import com.supplier.logistics.dto.DeliveryPackageDetailItemDTO;
import com.supplier.logistics.entity.DeliveryBarcode;
import com.supplier.logistics.entity.DeliveryPackage;
import com.supplier.logistics.entity.DeliveryPackageDetail;
import com.supplier.logistics.mapper.DeliveryBarcodeMapper;
import com.supplier.logistics.mapper.DeliveryPackageDetailMapper;
import com.supplier.logistics.mapper.DeliveryPackageMapper;
import com.supplier.logistics.service.DeliveryPackageService;
import com.supplier.logistics.vo.DeliveryBarcodeVO;
import com.supplier.logistics.vo.DeliveryPackageDetailVO;
import com.supplier.logistics.vo.DeliveryPackageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryPackageServiceImpl implements DeliveryPackageService {

    private final DeliveryPackageMapper packageMapper;
    private final DeliveryPackageDetailMapper packageDetailMapper;
    private final DeliveryBarcodeMapper barcodeMapper;

    @Override
    public List<DeliveryPackageVO> listByNoticeId(Long noticeId) {
        List<DeliveryPackage> packages = packageMapper.selectList(
                new LambdaQueryWrapper<DeliveryPackage>().eq(DeliveryPackage::getNoticeId, noticeId));
        return packages.stream().map(this::toVO).toList();
    }

    @Override
    public DeliveryPackageVO getDetail(Long id) {
        DeliveryPackage pack = packageMapper.selectById(id);
        if (pack == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        DeliveryPackageVO vo = toVO(pack);
        // 加载箱明细
        List<DeliveryPackageDetail> details = packageDetailMapper.selectList(
                new LambdaQueryWrapper<DeliveryPackageDetail>().eq(DeliveryPackageDetail::getPackageId, id));
        List<DeliveryPackageDetailVO> detailVOs = new ArrayList<>();
        for (DeliveryPackageDetail d : details) {
            DeliveryPackageDetailVO dvo = new DeliveryPackageDetailVO();
            dvo.setId(d.getId()); dvo.setPackageId(d.getPackageId());
            dvo.setMaterialCode(d.getMaterialCode()); dvo.setMaterialName(d.getMaterialName());
            dvo.setQuantity(d.getQuantity()); dvo.setUnit(d.getUnit()); dvo.setRemark(d.getRemark());
            detailVOs.add(dvo);
        }
        vo.setDetails(detailVOs);
        // 加载条码
        List<DeliveryBarcode> barcodes = barcodeMapper.selectList(
                new LambdaQueryWrapper<DeliveryBarcode>().eq(DeliveryBarcode::getPackageId, id));
        vo.setBarcodes(barcodes.stream().map(b -> {
            DeliveryBarcodeVO bvo = new DeliveryBarcodeVO();
            bvo.setId(b.getId()); bvo.setNoticeId(b.getNoticeId()); bvo.setPackageId(b.getPackageId());
            bvo.setBarcode(b.getBarcode()); bvo.setBarcodeType(b.getBarcodeType());
            bvo.setPrintCount(b.getPrintCount()); bvo.setRemark(b.getRemark());
            return bvo;
        }).toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeliveryPackageCreateDTO dto) {
        DeliveryPackage entity = new DeliveryPackage();
        entity.setNoticeId(dto.getNoticeId());
        entity.setPackageNo(dto.getPackageNo());
        entity.setPackageType(dto.getPackageType());
        entity.setWeight(dto.getWeight());
        entity.setVolume(dto.getVolume());
        entity.setRemark(dto.getRemark());
        packageMapper.insert(entity);

        // 创建箱明细
        if (!CollectionUtils.isEmpty(dto.getDetails())) {
            for (DeliveryPackageDetailItemDTO item : dto.getDetails()) {
                DeliveryPackageDetail detail = new DeliveryPackageDetail();
                detail.setPackageId(entity.getId());
                detail.setMaterialCode(item.getMaterialCode());
                detail.setMaterialName(item.getMaterialName());
                detail.setQuantity(item.getQuantity());
                detail.setUnit(item.getUnit());
                detail.setRemark(item.getRemark());
                packageDetailMapper.insert(detail);
            }
        }

        // 自动生成条码
        generateBarcodes(entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DeliveryBarcodeVO> generateBarcodes(Long packageId) {
        DeliveryPackage pack = packageMapper.selectById(packageId);
        if (pack == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND);
        }
        // 删除已有条码
        barcodeMapper.delete(new LambdaQueryWrapper<DeliveryBarcode>().eq(DeliveryBarcode::getPackageId, packageId));
        // 生成新条码 (GS1-128 格式: 前缀 + noticeId + packageNo + 时间戳)
        String barcode = "GS1" + pack.getNoticeId() + pack.getPackageNo().replaceAll("[^0-9]", "")
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));
        DeliveryBarcode bc = new DeliveryBarcode();
        bc.setNoticeId(pack.getNoticeId());
        bc.setPackageId(packageId);
        bc.setBarcode(barcode);
        bc.setBarcodeType("GS1-128");
        bc.setPrintCount(0);
        barcodeMapper.insert(bc);
        DeliveryBarcodeVO bvo = new DeliveryBarcodeVO();
        bvo.setId(bc.getId()); bvo.setNoticeId(bc.getNoticeId()); bvo.setPackageId(bc.getPackageId());
        bvo.setBarcode(bc.getBarcode()); bvo.setBarcodeType(bc.getBarcodeType());
        bvo.setPrintCount(bc.getPrintCount());
        return List.of(bvo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void printLabel(Long packageId) {
        DeliveryBarcode barcode = barcodeMapper.selectOne(
                new LambdaQueryWrapper<DeliveryBarcode>().eq(DeliveryBarcode::getPackageId, packageId));
        if (barcode == null) {
            throw BusinessException.of(ResultCode.NOT_FOUND.getCode(), "条码不存在，请先生成条码");
        }
        barcode.setPrintCount((barcode.getPrintCount() != null ? barcode.getPrintCount() : 0) + 1);
        barcodeMapper.updateById(barcode);
    }

    private DeliveryPackageVO toVO(DeliveryPackage e) {
        DeliveryPackageVO vo = new DeliveryPackageVO();
        vo.setId(e.getId()); vo.setNoticeId(e.getNoticeId()); vo.setPackageNo(e.getPackageNo());
        vo.setPackageType(e.getPackageType()); vo.setWeight(e.getWeight()); vo.setVolume(e.getVolume());
        vo.setRemark(e.getRemark());
        return vo;
    }
}