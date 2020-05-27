package com.study.dubbo.proxy;

import com.study.dubbo.discover.Discovery;
import com.study.dubbo.handler.InvokeHandler;
import com.study.dubbo.loadbalance.LoadBalancer;
import com.study.dubbo.message.Invocation;
import com.study.dubbo.message.RpcMessage;
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
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;

public class ServiceProxyFactory {

    private Discovery discovery;

    private LoadBalancer loadBalancer;

    public ServiceProxyFactory(Discovery discovery, LoadBalancer loadBalancer) {
        this.discovery = discovery;
        this.loadBalancer = loadBalancer;
    }

    public <T> T newInstance(Class<T> clazz, String prefix) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (Object.class.equals(method.getDeclaringClass())) {
                    return method.invoke(this, args);
                }
                Invocation invocation = Invocation.builder()
                        .interfaceName(clazz.getName())
                        .method(method.getName())
                        .argTypes(method.getParameterTypes())
                        .args(args)
                        .prefix(prefix)
                        .build();
                return ServiceProxyFactory.this.invoke(invocation);
            }
        });
    }

    private Object invoke(Invocation invocation) throws Exception {
        String interfaceName = invocation.getInterfaceName();
        // 找出可调用的服务器列表
        List<SocketAddress> socketAddresses = discovery.discover(interfaceName);
        SocketAddress next = loadBalancer.next(socketAddresses);
        if (next == null) {
            throw new Exception("No server for invoking.");
        }
        return rpc(invocation, next);
    }

    private Object rpc(Invocation invocation, SocketAddress socketAddress) {
        String id = UUID.randomUUID().toString();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        InvokeHandler invokeHandler = new InvokeHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectEncoder())
                                    .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
                                    .addLast(invokeHandler);
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(socketAddress).sync();
            RpcMessage<Invocation> rpcMessage = new RpcMessage<>(id, invocation);
            channelFuture.channel().writeAndFlush(rpcMessage).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
        return invokeHandler.getResult();
    }
}
