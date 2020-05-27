package com.study.service;

import com.study.dubbo.annotation.Service;

@Service
public class MySecondService implements SecondService {

    @Override
    public Integer second(Integer integer) throws Exception {
        return integer >> 2;
    }
}
