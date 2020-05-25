package com.study.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class RpcProxy {

    private static volatile ChannelFuture channelFuture;

    private static void initChannel(CountDownLatch countDownLatch) {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectEncoder())
                                    .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
                                    .addLast(new RpcReceiver());
                        }
                    });
            channelFuture = bootstrap.connect("localhost", 8888).sync();
            countDownLatch.countDown();
//            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            eventLoopGroup.shutdownGracefully();
        }
    }

    public static <T> T newInstance(Class<T> clazz) throws InterruptedException {
        if (channelFuture == null) {
            synchronized (RpcProxy.class) {
                if (channelFuture == null) {
                    CountDownLatch countDownLatch = new CountDownLatch(1);
                    new Thread(() -> {
                        RpcProxy.initChannel(countDownLatch);
                    }).start();
                    countDownLatch.await();
                }
            }
        }
        T instance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (Object.class.equals(method.getDeclaringClass())) {
                    return method.invoke(this, args);
                }
                Invocation invocation = Invocation.builder().interfaceName(clazz.getName())
                        .method(method.getName())
                        .argTypes(method.getParameterTypes())
                        .args(args).build();
                return rpc(invocation);
            }
        });
        return instance;
    }

    private static Object rpc(Invocation invocation) throws InterruptedException {
        String id = UUID.randomUUID().toString();
        RpcMessage<Invocation> rpcMessage = new RpcMessage<>(id, invocation);
        channelFuture.channel().writeAndFlush(rpcMessage).sync();
        return RpcReceiver.getResult(id);
    }


}
