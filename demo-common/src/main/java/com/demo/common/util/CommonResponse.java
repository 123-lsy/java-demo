package com.demo.common.util; /**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.demo.common.Enum.HttpStatus;

/**
 * @author wanghuihui
 */
public class CommonResponse<T> {
    private Integer code;
    private String msg;
    private T data;

    public CommonResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    //利用fastjson进行反序列化
    public <T> T getData(TypeReference<T> typeReference) {
        Object data = this.getData();	//默认是map
        String jsonString = JSON.toJSONString(data);
        T t = JSON.parseObject(jsonString, typeReference);
        return t;
    }

    public static <T> CommonResponse<T> success(T data, String msg){
        return new CommonResponse<>(HttpStatus.SUCCESS, msg, data);
    }

    public static <T> CommonResponse<T> fail(){
        return new CommonResponse<>(HttpStatus.FAILURE, null, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
