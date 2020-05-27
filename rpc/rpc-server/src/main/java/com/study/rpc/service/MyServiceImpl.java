package com.study.rpc.service;

public class MyServiceImpl implements MyService{

    @Override
    public String process(String arg) {
        return String.format("执行了远程调用，参数是：%s", arg);
    }
}
