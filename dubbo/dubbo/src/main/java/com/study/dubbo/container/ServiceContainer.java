package com.study.dubbo.container;

import io.netty.util.internal.StringUtil;

import java.util.*;

/**
 * 服务容器
 *
 * @author 雷池
 * @date 2020/5/27 0027 17:22
 */
public class ServiceContainer {

    private Map<String, List<Object>> servicesByInterfaceName;

    private Map<String, Object> serviceByClassName;

    public ServiceContainer() {
        servicesByInterfaceName = new HashMap<>();
        serviceByClassName = new HashMap<>();
    }

    public Object math(String interfaceName, String prefix) {
        List<Object> services = getServicesByInterfaceName(interfaceName);
        if (services == null) {
            return null;
        }
        if (services.size() == 1 && StringUtil.isNullOrEmpty(prefix)) {
            return services.get(0);
        }
        String[] split = interfaceName.split("\\.");
        String lastName = split[split.length - 1];
        if (!StringUtil.isNullOrEmpty(prefix)) {
            lastName = prefix.concat(lastName);
        }
        String className = "";
        for (int i = 0; i < split.length - 1; i++) {
            className += split[i] + ".";
        }
        className += lastName;
        return getServiceByClassName(className);
//        return services.get(0);
    }

    private List<Object> getServicesByInterfaceName(String interfaceName) {
        return servicesByInterfaceName.get(interfaceName);
    }

    private Object getServiceByClassName(String className) {
        return serviceByClassName.get(className);
    }

    public void addService(String interfaceName, Object service) {
        if (interfaceName == null || interfaceName == "" || service == null) {
            return;
        }
        List<Object> services = this.servicesByInterfaceName.get(interfaceName);
        if (services == null) {
            services = new ArrayList<>();
            servicesByInterfaceName.put(interfaceName, services);
        }
        services.add(service);
        serviceByClassName.put(service.getClass().getName(), service);
    }

    public Set<String> getAllInterfaceName() {
        return servicesByInterfaceName.keySet();
    }
}
