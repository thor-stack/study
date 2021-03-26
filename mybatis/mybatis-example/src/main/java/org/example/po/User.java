package org.example.po;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户Po
 *
 * @author thor
 */
@Data
public class User implements Serializable {

    private Integer id;

    private String name;

    private String sex;

    private String address;
}
