package com.study.dubbo.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcMessage<T> implements Serializable {

    private String id;

    private String error;

    private T data;

    public RpcMessage(String id){
        this(id, null);
    }

    public RpcMessage(String id, T data) {
        this.id = id;
        this.data = data;
    }



}
