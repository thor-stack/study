package com.study.dubbo.container;

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

    public List<Object> getServicesByInterfaceName(String interfaceName) {
        return servicesByInterfaceName.get(interfaceName);
    }

    public Object getServiceByClassName(String className) {
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

    public Set<String> getAllInterfaceName(){
        return servicesByInterfaceName.keySet();
    }
}
