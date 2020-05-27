package com.study.rpc;

import com.study.rpc.service.MyService;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        MyService service = RpcProxy.newInstance(MyService.class);
        System.out.println(service.process("Hello RPC"));
        System.out.println(service.process("Hello RPC 2"));
        System.out.println(service.hashCode());
    }
}
