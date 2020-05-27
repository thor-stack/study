package com.study.dubbo;

import com.study.dubbo.annotation.Reference;
import com.study.dubbo.config.Config;
import com.study.dubbo.container.ServiceContainer;
import com.study.dubbo.discover.Discovery;
import com.study.dubbo.discover.ZookeeperDiscovery;
import com.study.dubbo.handler.ReceiveHandler;
import com.study.dubbo.loadbalance.LoadBalancer;
import com.study.dubbo.loadbalance.RandomLoadBalancer;
import com.study.dubbo.proxy.ServiceProxyFactory;
import com.study.dubbo.register.Register;
import com.study.dubbo.register.ZookeeperRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;

/**
 * Dubbo运行器
 */
@Slf4j
public class DubboWorker {

    private Config config;

    private Register register;

    private Discovery discovery;

    private LoadBalancer loadBalancer;

    private ServiceContainer serviceContainer;

    private CuratorFramework curatorFramework;

    private ServiceProxyFactory serviceProxyFactory;

    public DubboWorker(Config config) {
        this.config = config;
        initCuratorFramework(config.getZookeeperUrl());
        serviceContainer = new ServiceContainer();
        register = new ZookeeperRegister(curatorFramework, serviceContainer);
        discovery = new ZookeeperDiscovery(curatorFramework);
        loadBalancer = new RandomLoadBalancer();
        serviceProxyFactory = new ServiceProxyFactory(discovery, loadBalancer);
    }

    private void initCuratorFramework(String zookeeperUrl) {
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(config.getZookeeperUrl())
                .connectionTimeoutMs(1000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        curatorFramework.start();
        log.info("Zookeeper initialization completed.");
    }

    public void start() throws Exception {
        register.register(config);
        log.info("Services registration completed.");
        startProvider();
    }

    public void fillStaticReference(Class<?> clazz) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Reference.class) && Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                field.set(clazz.newInstance(), serviceProxyFactory.newInstance(field.getType(), null));
            }
        }
    }

    private void startProvider() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            EventLoopGroup parentGroup = new NioEventLoopGroup();
            EventLoopGroup childGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(parentGroup, childGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(new ObjectEncoder())
                                        .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
                                        // 配置自定义处理器
                                        .addLast(new ReceiveHandler(serviceContainer));
                            }
                        });
                ChannelFuture channelFuture = bootstrap.bind(config.getIp(), config.getPort()).sync();
                countDownLatch.countDown();
                log.info("Service provider started.");
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                parentGroup.shutdownGracefully();
                childGroup.shutdownGracefully();
            }
        }, "dubbo-provider").start();
        countDownLatch.await();
    }
}
