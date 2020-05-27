package com.study.dubbo.register;

import com.study.dubbo.config.Config;
import com.study.dubbo.constent.CommonConstent;
import com.study.dubbo.container.ServiceContainer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

/**
 * 基于Zookeeper实现的注册器
 *
 * @author 雷池
 * @date 2020/5/27 0027 17:11
 */
@Slf4j
public class ZookeeperRegister implements Register {

    private ServiceContainer serviceContainer;

    public ZookeeperRegister() {
        serviceContainer = new ServiceContainer();
    }

    @Override
    public void register(Config config) throws Exception {
        // 检查配置
        checkConfig(config);
        // 扫描服务
        serviceScan(config.getServicePackage());
        // 注册服务
        doRegister(config);
    }

    private void doRegister(Config config) throws Exception {
        Set<String> allInterfaceName = serviceContainer.getAllInterfaceName();
        if (allInterfaceName == null || allInterfaceName.size() == 0){
            log.info("Nothing to register.");
        }
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(config.getZookeeperUrl())
                .connectionTimeoutMs(1000)
                .sessionTimeoutMs(1000)
                .retryPolicy(new RetryOneTime(1000))
                .build();
        for (String interfaceName: allInterfaceName){
            String zkPath = String.format("%s/%s/%s:%s", CommonConstent.ROOT_PATH, interfaceName, config.getIp(), config.getPort());
            curatorFramework.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(zkPath);
        }
    }

    private void serviceScan(String basePackage) throws URISyntaxException, ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        URL url = getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));
        File directory = new File(url.toURI());
        for (File child : directory.listFiles()) {
            String name = child.getName();
            if (child.isDirectory()) {
                serviceScan(basePackage + "." + name);
            }
            if (name.endsWith(".class")) {
                String className = basePackage + "." + name.replace(".class", "");
                Class<?> clazz = Class.forName(className);
                Class<?>[] interfaces = clazz.getInterfaces();
                if (interfaces == null || interfaces.length == 0) {
                    continue;
                }
                serviceContainer.addService(interfaces[0].getName(), clazz.newInstance());
            }
        }
    }

    private void checkConfig(Config config) {
        // TODO
    }
}
