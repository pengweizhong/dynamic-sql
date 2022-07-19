package com.pengwz.dynamic.sql;

import java.util.List;

public class PageInfo<T> {
    //当前页码,<=1 都将修正为第一页，换言之，如果pageIndex<=1,其值返回结果一定是1
    private Integer pageIndex;
    //limit的偏移量，在真实分页时使用，用于纠正pageIndex，比如：limit [offset] , [pageSize],外部调用者无需关心此属性值。
    private Integer offset;
    //当前页展示条数
    private Integer pageSize;
    //当前页实际条数，等同于 resultList.size()
    private Integer realPageSize;
    //总页码
    private Integer totalPages;
    //总行数
    private Integer totalSize;
    //结果集，没有数据为空集合
    private List<T> resultList;

    public PageInfo(Integer pageIndex, Integer pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getRealPageSize() {
        return realPageSize;
    }

    public void setRealPageSize(Integer realPageSize) {
        this.realPageSize = realPageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getResultList() {
        return resultList;
    }

    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "PageInfo{" +
                "pageIndex=" + pageIndex +
                ", pageSize=" + pageSize +
                ", realPageSize=" + realPageSize +
                ", totalPages=" + totalPages +
                ", totalSize=" + totalSize +
                ", resultList=" + resultList +
                '}';
    }
}
