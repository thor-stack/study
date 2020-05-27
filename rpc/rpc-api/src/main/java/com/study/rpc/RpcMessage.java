package com.study.rpc;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcMessage<T> implements Serializable {

    public RpcMessage(T data) {
        this.data = data;
    }

    public RpcMessage(String id, T data) {
        this.id = id;
        this.data = data;
    }

    private String id;

    private T data;

}
