package com.study.dubbo.config;

import lombok.Data;

/**
 * Dubbo的配置类
 *
 * @author 雷池
 * @date 2020/5/27 0027 16:45
 */
@Data
public class Config {

    private String servicePackage;

    private String zookeeperUrl;

    private String ip;

    private Integer port;
}
