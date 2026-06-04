package com.supplier.system.service.impl;

import com.supplier.system.service.ImportTaskService;
import com.supplier.system.service.SysExportTaskService;
import com.supplier.system.service.SysFileAttachmentService;
import com.supplier.system.vo.ImportCheckResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImportTaskServiceImpl implements ImportTaskService {
    private final SysFileAttachmentService fileAttachmentService;
    private final SysExportTaskService exportTaskService;

    @Override
    public SysFileAttachmentService.FileDownload downloadTemplate(String importType) {
        String content = "字段编码,字段名称,是否必填\ncode,编码,是\nname,名称,是\nremark,备注,否\n";
        return new SysFileAttachmentService.FileDownload(importType + "_template.csv", "text/csv;charset=UTF-8", content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public ImportCheckResultVO check(String importType, MultipartFile file) throws IOException {
        Long fileId = fileAttachmentService.upload(file, "import_" + importType, null, null);
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        int total = text.isBlank() ? 0 : Math.max(0, text.split("\\R").length - 1);
        return new ImportCheckResultVO(fileId, importType, total, total, 0, null);
    }

    @Override
    public Long submit(String importType, Long fileId) {
        return exportTaskService.create(new com.supplier.system.dto.SysExportTaskCreateDTO() {{
            setTaskType("import_" + importType);
            setExportParams("{\"fileId\":" + fileId + "}");
            setTotalCount(0);
        }});
    }

    @Override
    public SysFileAttachmentService.FileDownload downloadErrors(Long fileId) throws IOException {
        return fileAttachmentService.load(fileId);
    }

    @Override
    public List<Map<String, Object>> listTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();
        String[][] templateData = {
                {"1", "订单", "purchase-order", "xlsx", "采购订单导入模板"},
                {"2", "ASN", "asn", "xlsx", "ASN明细导入模板"},
                {"3", "对账单", "reconciliation", "xlsx", "对账明细导入模板"},
                {"4", "发票", "invoice", "xlsx", "发票导入模板"},
                {"5", "付款", "payment", "xlsx", "付款导入模板"},
                {"6", "供应商", "supplier", "xlsx", "供应商导入模板"},
        };
        for (String[] data : templateData) {
            Map<String, Object> tpl = new LinkedHashMap<>();
            tpl.put("id", Long.valueOf(data[0]));
            tpl.put("module", data[1]);
            tpl.put("importType", data[2]);
            tpl.put("name", data[4]);
            tpl.put("format", data[3]);
            tpl.put("updatedAt", java.time.LocalDate.now().toString());
            templates.add(tpl);
        }
        return templates;
    }
}
