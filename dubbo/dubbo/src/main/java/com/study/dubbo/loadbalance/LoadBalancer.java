package com.study.dubbo.loadbalance;

import java.net.SocketAddress;
import java.util.List;

public interface LoadBalancer {

    SocketAddress peek(List<SocketAddress> socketAddresses);
}
