package com.gxa.result;

import lombok.Data;

/**
 * @author zxd
 * @date 2026/1/18
 */
@Data//get+set+toString方法
public class Result <T>{

    //响应的状态码
    private Integer code;

    //响应的提示信息
    private String msg;

    //响应的数据
    private T data;

    //总记录数
    private Long count;

    /**
     * 封装带分页的成功信息
     * @param data
     * @param count
     * @return
     */
    public static Result buildSuccess(Object data,Long count){
        Result result=new Result();
        result.setCode(0);
        result.setMsg("操作成功!");
        result.setData(data);
        result.setCount(count);
        return result;
    }

    /**
     * 不带分页的查询
     * @param data
     * @return
     */
    public static Result buildSuccess(Object data){
        Result result=new Result();
        result.setCode(0);
        result.setMsg("操作成功!");
        result.setData(data);
        return result;
    }

    /**
     * 普通的成功
     * @return
     */
    public static Result buildSuccess(){
        Result result=new Result();
        result.setCode(0);
        result.setMsg("操作成功!");
        return result;
    }

    /**
     * 构建失败的提示信息
     * @param code
     * @param msg
     * @return
     */
    public static Result buildFail(Integer code, String msg){
        Result result=new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

}
