package com.study.rpc.server;

import com.study.rpc.server.handler.RpcHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RpcServer {

    private String basePackage;

    private Map<String, Object> invokeTargets = new HashMap<>();

    public RpcServer(String basePackage) {
        this.basePackage = basePackage;
    }

    public void start() throws Exception {
        // 把basePackage包及其子包下的类注册到invokeTargets中
        registry(basePackage);

        doStart();
    }

    private void doStart() throws InterruptedException {
        EventLoopGroup parentLoopGroup = new NioEventLoopGroup();
        EventLoopGroup childLoopGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(parentLoopGroup, childLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectEncoder())
                                    .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
                                    .addLast(new RpcHandler(invokeTargets));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(8888).sync();
            System.out.println("服务器启动成功");
            channelFuture.channel().closeFuture().sync();
        } finally {
            parentLoopGroup.shutdownGracefully();
            childLoopGroup.shutdownGracefully();
        }
    }

    private void registry(String basePackage) throws URISyntaxException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        URL url = getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));
        File directory = new File(url.toURI());
        for (File child : directory.listFiles()) {
            String name = child.getName();
            if (child.isDirectory()) {
                registry(basePackage + "." + name);
            }
            if (name.endsWith(".class")) {
                String className = basePackage + "." + name.replace(".class", "");
                Class<?> clazz = Class.forName(className);
                Class<?>[] interfaces = clazz.getInterfaces();
                if (interfaces == null || interfaces.length == 0) {
                    continue;
                }
                invokeTargets.put(interfaces[0].getName(), clazz.newInstance());
            }
        }
    }

    public Map<String, Object> getInvokeTargets() {
        return invokeTargets;
    }
}
