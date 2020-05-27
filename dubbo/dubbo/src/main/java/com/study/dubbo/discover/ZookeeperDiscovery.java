package com.study.dubbo.discover;

import com.study.dubbo.constent.CommonConstant;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于Zookeeper实现的服务发现器
 */
public class ZookeeperDiscovery implements Discovery {

    /**
     * zookeeper的客户端，通过构造器注入
     */
    private CuratorFramework curatorFramework;

    public ZookeeperDiscovery(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    @Override
    public List<SocketAddress> discover(String interfaceName) throws Exception {
        String zkPath = String.format("%s/%s", CommonConstant.ROOT_PATH, interfaceName);
        List<String> children = curatorFramework.getChildren().forPath(zkPath);
        if (children == null || children.size() == 0) {
            return Collections.emptyList();
        }
        List<SocketAddress> socketAddresses = children.stream().map(path -> {
            String[] ipAndPort = path.split(":");
            String ip = ipAndPort[0];
            Integer port = Integer.valueOf(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).collect(Collectors.toList());
        return socketAddresses;
    }
}
