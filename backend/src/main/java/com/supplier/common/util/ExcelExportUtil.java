package com.supplier.common.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Excel 导出工具类，基于 EasyExcel
 */
@Slf4j
public class ExcelExportUtil {

    /**
     * 将数据列表导出为 Excel 字节数组
     *
     * @param dataClass 数据类型（需使用 @ExcelProperty 注解定义列）
     * @param dataList  数据列表
     * @param sheetName Sheet 名称
     * @return Excel 文件字节数组
     */
    public static <T> byte[] export(Class<T> dataClass, List<T> dataList, String sheetName) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, dataClass)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(sheetName)
                .doWrite(dataList);
        return out.toByteArray();
    }
}
