ALTER TABLE delivery_detail
    ADD COLUMN expiry_date DATE DEFAULT NULL COMMENT '过期日期/有效期至' AFTER production_date;