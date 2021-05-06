package com.bazinga.base;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yunshan
 * @date 2018/12/20
 */
public class PagingQuery {
    public static int DEFAULT_LIMIT = 5000;
    private int offset;
    private int limit;
    private List<Sort> sorts;
    private Long lastMaxId;

    public PagingQuery() {
        this(DEFAULT_LIMIT);
    }

    public PagingQuery(int limit) {
        this(0, limit);
    }

    public PagingQuery(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }



    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset < 0 ? 0 : offset;
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<Sort> getSorts() {
        return this.sorts;
    }

    public Long getLastMaxId() {
        return this.lastMaxId;
    }

    public void setLastMaxId(Long lastMaxId) {
        this.lastMaxId = lastMaxId;
    }

    public void addOrderBy(String column, Sort.SortType sortType) {
        if (column != null && column.trim().length() != 0) {
            if (sortType == null) {
                throw new IllegalArgumentException("排序方式不能为空");
            } else {
                if (this.sorts == null) {
                    this.sorts = new ArrayList(2);
                }

                column = column.replaceAll(";", "");
                this.sorts.add(new Sort(column, sortType));
            }
        } else {
            throw new IllegalArgumentException("列名不能为空");
        }
    }



    public static void main(String[] args) {
        PagingQuery pagingQuery = new PagingQuery();
        pagingQuery.setOffset(0);
        pagingQuery.setLimit(100);
        pagingQuery.addOrderBy("id", Sort.SortType.ASC);
        pagingQuery.addOrderBy("create_time", Sort.SortType.DESC);
        System.out.println(pagingQuery);
    }
}
