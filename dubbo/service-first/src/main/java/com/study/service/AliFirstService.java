package com.study.service;

import com.study.dubbo.annotation.Service;
import com.study.service.FirstService;

@Service
public class AliFirstService implements FirstService {

    public String first(String input) {
        return "Ali first got message: " + input;
    }
}
