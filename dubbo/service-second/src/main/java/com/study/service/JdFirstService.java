package com.study.service;

import com.study.dubbo.annotation.Service;
import com.study.service.FirstService;

@Service
public class JdFirstService implements FirstService {

    @Override
    public String first(String input) {
        return "Jd first got message: " + input;
    }
}
