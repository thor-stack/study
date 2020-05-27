package com.study.dubbo.loadbalance;

import java.net.SocketAddress;
import java.util.List;
import java.util.Random;

/**
 * 随机负债均衡器
 *
 * @author 雷池
 * @date 2020/5/27 0027 17:08
 */
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public SocketAddress next(List<SocketAddress> socketAddresses) {
        if (socketAddresses == null || socketAddresses.size() == 0) {
            return null;
        }
        int index = new Random().nextInt(socketAddresses.size());
        return socketAddresses.get(index);
    }
}
