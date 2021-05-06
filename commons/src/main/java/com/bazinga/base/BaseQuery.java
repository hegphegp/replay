package com.bazinga.base;


import lombok.Data;

/**
 * @author yunshan
 * @date 2019/1/9
 */
@Data
public class BaseQuery {

    private static final int DEFAULT_PAGE_SIZE = 10;

    protected int pageNo;

    protected int pageSize = DEFAULT_PAGE_SIZE;
}
