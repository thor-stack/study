package com.study.dubbo.register;

import com.study.dubbo.config.Config;

/**
 * 注册机
 *
 * @author 雷池
 * @date 2020/5/27 0027 16:50
 */
public interface Register {

    void register(Config config) throws Exception;
}
