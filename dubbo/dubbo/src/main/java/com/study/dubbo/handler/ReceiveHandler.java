package com.study.dubbo.handler;

import com.study.dubbo.container.ServiceContainer;
import com.study.dubbo.message.Invocation;
import com.study.dubbo.message.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiveHandler extends SimpleChannelInboundHandler<RpcMessage<Invocation>> {

    private ServiceContainer serviceContainer;

    public ReceiveHandler(ServiceContainer serviceContainer) {
        this.serviceContainer = serviceContainer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage<Invocation> message) throws Exception {
        Invocation msg = message.getData();
        // 找到本地的服务进行调用
        String interfaceName = msg.getInterfaceName();
        RpcMessage<Object> response = null;
        try {
            Object target = serviceContainer.math(interfaceName, msg.getPrefix());
            if (target == null) {
                response = new RpcMessage<>(message.getId());
                response.setError("Cannot find remote target.");
            } else {
                Object result = Class.forName(interfaceName).getMethod(msg.getMethod(), msg.getArgTypes())
                        .invoke(target, msg.getArgs());
                response = new RpcMessage<>(message.getId(), result);
            }
            ctx.writeAndFlush(response);
        } finally {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Error processing message", cause);
        ctx.close();
    }
}
