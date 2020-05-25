package com.study.rpc;

import com.study.rpc.server.RpcServer;

public class Main {

    public static void main(String[] args) throws Exception {
        RpcServer rpcServer = new RpcServer("com.study.rpc.service");
        rpcServer.start();
    }
}
