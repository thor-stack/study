package com.study.dubbo.discover;

import java.net.SocketAddress;
import java.util.List;

public interface Discovery {

    /**
     * 根据接口名称找到可以调用的远端地址
     *
     * @param interfaceName 接口名称
     * @return 可调用的地址列表
     * @throws Exception 异常
     */
    List<SocketAddress> discover(String interfaceName) throws Exception;
}
