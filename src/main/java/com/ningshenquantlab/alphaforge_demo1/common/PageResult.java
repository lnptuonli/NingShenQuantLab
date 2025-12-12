package com.ningshenquantlab.alphaforge_demo1.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;        // 数据列表
    private Long total;          // 总数
    private Integer page;        // 当前页
    private Integer size;        // 每页大小
    private Integer totalPages;  // 总页数

    public PageResult(List<T> list, Long total, Integer page, Integer size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }
}
