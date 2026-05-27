package com.supplier.integration.enums;

import lombok.Getter;

@Getter
public enum SystemTypeEnum {

    ERP("ERP", "ERP"),
    WMS("WMS", "WMS"),
    MES("MES", "MES"),
    FINANCE("FINANCE", "财务"),
    OCR("OCR", "OCR");

    private final String code;
    private final String desc;

    SystemTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}