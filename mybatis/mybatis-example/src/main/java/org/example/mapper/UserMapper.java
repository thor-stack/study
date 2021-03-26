package org.example.mapper;

import org.example.po.User;

public interface UserMapper {

    /**
     * 根据Id查找用户
     *
     * @param id the id
     * @return the user if exists
     */
    User getUserById(Integer id);
}
