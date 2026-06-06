package com.supplier.logistics.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

/**
 * ASN 导出 DTO，定义 Excel 列映射
 */
@Data
public class DeliveryNoticeExportDTO {

    @ExcelProperty("ASN号")
    @ColumnWidth(20)
    private String noticeNo;

    @ExcelProperty("订单号")
    @ColumnWidth(20)
    private String orderNo;

    @ExcelProperty("供应商")
    @ColumnWidth(20)
    private String supplierName;

    @ExcelProperty("预计交货日期")
    @ColumnWidth(16)
    private String planDeliveryDate;

    @ExcelProperty("实际交货日期")
    @ColumnWidth(16)
    private String actualDeliveryDate;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String deliveryStatusText;

    @ExcelProperty("运输方式")
    @ColumnWidth(12)
    private String deliveryMethod;

    @ExcelProperty("物流公司")
    @ColumnWidth(16)
    private String deliveryCompany;

    @ExcelProperty("物流单号")
    @ColumnWidth(20)
    private String deliveryNo;

    @ExcelProperty("收货地址")
    @ColumnWidth(30)
    private String deliveryAddress;

    @ExcelProperty("收货人")
    @ColumnWidth(12)
    private String receiver;

    @ExcelProperty("物料编码")
    @ColumnWidth(16)
    private String materialCode;

    @ExcelProperty("物料名称")
    @ColumnWidth(20)
    private String materialName;

    @ExcelProperty("规格")
    @ColumnWidth(16)
    private String materialSpec;

    @ExcelProperty("单位")
    @ColumnWidth(8)
    private String unit;

    @ExcelProperty("计划数量")
    @ColumnWidth(12)
    private BigDecimal planQty;

    @ExcelProperty("发货数量")
    @ColumnWidth(12)
    private BigDecimal actualQty;

    @ExcelProperty("已收数量")
    @ColumnWidth(12)
    private BigDecimal receivedQty;

    @ExcelProperty("合格数量")
    @ColumnWidth(12)
    private BigDecimal qualifiedQty;

    @ExcelProperty("批次号")
    @ColumnWidth(16)
    private String batchNo;

    @ExcelProperty("箱号")
    @ColumnWidth(16)
    private String caseNo;

    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;
}
