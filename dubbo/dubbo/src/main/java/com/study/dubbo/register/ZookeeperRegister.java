package com.study.dubbo.register;

import com.study.dubbo.annotation.Service;
import com.study.dubbo.config.Config;
import com.study.dubbo.constent.CommonConstant;
import com.study.dubbo.container.ServiceContainer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.net.URISyntaxException;
import java.util.Set;

/**
 * 基于Zookeeper实现的注册器
 *
 * @author 雷池
 * @date 2020/5/27 0027 17:11
 */
@Slf4j
public class ZookeeperRegister implements Register {

    /**
     * 服务容器，保存服务
     */
    private ServiceContainer serviceContainer;

    /**
     * zookeeper的客户端，通过构造器注入
     */
    private CuratorFramework curatorFramework;

    public ZookeeperRegister(CuratorFramework curatorFramework, ServiceContainer serviceContainer) {
        this.curatorFramework = curatorFramework;
        this.serviceContainer = serviceContainer;
    }

    @Override
    public void register(Config config) throws Exception {
        // 检查配置
        checkConfig(config);
        // 扫描服务
        initDubbo(config.getScanPkg());
        // 注册服务
        doRegister(config);
    }

    private void doRegister(Config config) throws Exception {
        Set<String> allInterfaceName = serviceContainer.getAllInterfaceName();
        if (allInterfaceName == null || allInterfaceName.size() == 0) {
            log.info("Nothing to register.");
        }
        for (String interfaceName : allInterfaceName) {
            String zkPath = String.format("%s/%s/%s:%s", CommonConstant.ROOT_PATH,
                    interfaceName, config.getIp(), config.getPort());
            createTmpNode(zkPath);
        }
    }

    /**
     * 创建Zookeeper的临时节点
     *
     * @param zkPath zkPath
     * @throws Exception
     */
    private void createTmpNode(String zkPath) throws Exception {
        if (curatorFramework.checkExists().forPath(zkPath) == null) {
            curatorFramework.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(zkPath);
            log.info("Created zookeeper ephemeral node, path: {}", zkPath);
        } else {
            log.info("Zookeeper path has been exist, skip creating. path: {}", zkPath);
        }
    }

    private void initDubbo(String basePackage) throws URISyntaxException, ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        ConfigurationBuilder config = new ConfigurationBuilder();
        config.addUrls(ClasspathHelper.forPackage(basePackage));
        config.setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(), new FieldAnnotationsScanner());
        Reflections reflections = new Reflections(config);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Service.class);
        for (Class<?> clazz : classes) {
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces == null || interfaces.length == 0) {
                continue;
            }
            serviceContainer.addService(interfaces[0].getName(), clazz.newInstance());
        }
    }



    private void checkConfig(Config config) {
        // TODO
    }
}
