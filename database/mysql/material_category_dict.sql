-- 物料分类字典（主表 + 字典项）
INSERT INTO sys_dict (id, dict_name, dict_code, description, status, create_by) VALUES
(19, '物料分类', 'material_category', '物料主数据分类', 1, 1);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort, description, status, create_by) VALUES
(19, '结构件', '结构件', 1, '结构件类物料', 1, 1),
(19, '液压件', '液压件', 2, '液压件类物料', 1, 1),
(19, '电子件', '电子件', 3, '电子件类物料', 1, 1),
(19, '传动件', '传动件', 4, '传动件类物料', 1, 1),
(19, '包装材料', '包装材料', 5, '包装材料类物料', 1, 1),
(19, '辅料', '辅料', 6, '辅料类物料', 1, 1),
(19, '紧固件', '紧固件', 7, '紧固件类物料', 1, 1),
(19, '密封件', '密封件', 8, '密封件类物料', 1, 1);
