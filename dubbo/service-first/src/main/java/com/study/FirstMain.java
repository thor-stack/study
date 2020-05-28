package com.study;

import com.study.dubbo.DubboWorker;
import com.study.dubbo.annotation.Reference;
import com.study.dubbo.config.Config;
import com.study.service.FirstService;
import com.study.service.SecondService;

import java.util.concurrent.TimeUnit;

public class FirstMain {

    @Reference
    private static FirstService firstService;

    @Reference
    private static SecondService secondService;

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setIp("localhost");
        config.setPort(8888);
        config.setScanPkg("com.study");
        config.setZookeeperUrl("47.52.159.139:2181");
        DubboWorker worker = new DubboWorker(config);
        worker.start();
        worker.fillStaticReference(FirstMain.class);
        for (int i= 0; i < 10; i++){
            System.out.println(firstService.first("Hello Dubbo-" + i));
            TimeUnit.MILLISECONDS.sleep(1000);
        }
        Integer response2 = secondService.second(222222);
        System.out.println(response2);
    }
}
