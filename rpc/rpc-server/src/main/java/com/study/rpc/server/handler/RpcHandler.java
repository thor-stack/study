package com.study.rpc.server.handler;

import com.study.rpc.Invocation;
import com.study.rpc.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class RpcHandler extends SimpleChannelInboundHandler<RpcMessage<Invocation>> {

    private Map<String, Object> invokeTargets;

    public RpcHandler(Map<String, Object> invokeTargets) {
        this.invokeTargets = invokeTargets;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage<Invocation> message) throws Exception {
        Invocation msg = message.getData();
        String interfaceName = msg.getInterfaceName();
        Object target = invokeTargets.get(interfaceName);
        if (target == null) {
            ctx.writeAndFlush("没有找到远程调用的实例");
            return;
        }
        Object result = doInvoke(msg, target);
        if (result == null) {
            ctx.writeAndFlush("远程调用没有返回值");
            return;
        }
        ctx.writeAndFlush(new RpcMessage<Object>(message.getId(), result));
        ctx.close();
    }

    private Object doInvoke(Invocation invocation, Object target) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return target.getClass().getMethod(invocation.getMethod(), invocation.getArgTypes())
                .invoke(target, invocation.getArgs());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
