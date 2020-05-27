package com.study.rpc;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Invocation implements Serializable {

    private String interfaceName;

    private String method;

    private Class<?>[] argTypes;

    private Object[] args;
}
