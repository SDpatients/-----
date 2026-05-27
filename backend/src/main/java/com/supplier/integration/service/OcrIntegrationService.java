package com.supplier.integration.service;

/**
 * OCR 识别集成服务
 * 负责发票识别和营业执照识别
 */
public interface OcrIntegrationService {

    /**
     * 发票 OCR 识别
     * @param fileUrl 发票图片URL或base64
     * @return OCR 识别结果的 JSON
     */
    String recognizeInvoice(String fileUrl);

    /**
     * 营业执照 OCR 识别
     * @param fileUrl 营业执照图片URL或base64
     * @return OCR 识别结果的 JSON
     */
    String recognizeBusinessLicense(String fileUrl);
}