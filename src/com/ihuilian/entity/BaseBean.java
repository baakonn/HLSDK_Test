package com.ihuilian.entity;

import java.io.Serializable;

/**
 *BaseBean
 *author:bakon(762713299@qq.com)
 *date:2016/5/12-10:51
 */
public class BaseBean<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    //对应的message
    public int status_code;
    //响应状态码
    public String message;
    //返回的数据
    public T data;

}
