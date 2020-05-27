package com.study;

import com.study.dubbo.DubboWorker;
import com.study.dubbo.annotation.Reference;
import com.study.dubbo.config.Config;
import com.study.service.FirstService;
import com.study.service.SecondService;

public class SecondMain {

    @Reference
    private static FirstService firstService;

    @Reference
    private static SecondService secondService;

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setIp("localhost");
        config.setPort(9999);
        config.setScanPkg("com.study");
        config.setZookeeperUrl("10.0.0.101:2181");
        DubboWorker worker = new DubboWorker(config);
        worker.start();
        worker.fillStaticReference(SecondMain.class);
        String response = firstService.first("Hello Dubbo");
        System.out.println(response);
        Integer response2 = secondService.second(222222);
        System.out.println(response2);
    }
}
