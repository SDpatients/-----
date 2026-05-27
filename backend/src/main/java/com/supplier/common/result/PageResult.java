package com.supplier.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> records;
    private Long total;
    private Long pageSize;
    private Long pageNum;
    private Long pages;

    public PageResult() {
        this.records = Collections.emptyList();
        this.total = 0L;
        this.pageSize = 10L;
        this.pageNum = 1L;
        this.pages = 0L;
    }

    public PageResult(List<T> records, Long total, Long pageSize, Long pageNum) {
        this.records = records;
        this.total = total;
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.pages = pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize;
    }

    public static <T> PageResult<T> of(List<T> records, Long total, Long size, Long current) {
        return new PageResult<>(records, total, size, current);
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>();
    }
}
