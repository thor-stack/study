package com.study.dubbo.discover;

import java.net.SocketAddress;
import java.util.List;

public interface Discoverer {

    List<SocketAddress> discover(String name, String prefix) throws Exception;

    default List<SocketAddress> discover(String name) throws Exception {
        return discover(name, null);
    }
}
