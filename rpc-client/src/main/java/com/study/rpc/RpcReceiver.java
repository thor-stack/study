package com.study.rpc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcReceiver extends SimpleChannelInboundHandler<RpcMessage<Object>> {

    private static final Map<String, Object> resultMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage<Object> msg) throws Exception {
        resultMap.put(msg.getId(), msg.getData());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public static final Object getResult(String id) {
        return resultMap.remove(id);
    }
}
