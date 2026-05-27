package com.supplier.integration.enums;

import lombok.Getter;

@Getter
public enum IntegrationModeEnum {

    REST("REST", "REST"),
    MQ("MQ", "MQ"),
    FILE("FILE", "文件"),
    DB("DB", "数据库"),
    EDI("EDI", "EDI");

    private final String code;
    private final String desc;

    IntegrationModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}