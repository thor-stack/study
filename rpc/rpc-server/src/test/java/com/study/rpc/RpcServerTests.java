package com.study.rpc;

import com.study.rpc.server.RpcServer;
import org.junit.Test;

public class RpcServerTests {

    @Test
    public void testRpcServer() throws Exception {
        RpcServer rpcServer = new RpcServer("com.study.rpc.service");
        rpcServer.start();
        System.out.println(rpcServer.getInvokeTargets());
    }
}
