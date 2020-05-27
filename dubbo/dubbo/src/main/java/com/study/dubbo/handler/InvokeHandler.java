package com.study.dubbo.handler;

import com.study.dubbo.message.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvokeHandler extends SimpleChannelInboundHandler<RpcMessage<Object>> {

    private Object result;

    public Object getResult() {
        return result;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage<Object> msg) throws Exception {
        log.info("Got an invoke result from {}, id: {}", ctx.channel().remoteAddress(), msg.getId());
        if (msg.getError() != null){
            throw new Exception(msg.getError());
        }
        result = msg.getData();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Error invoking rpc", cause);
        ctx.close();
    }
}
