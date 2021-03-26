package org.example.test;

import org.example.po.User;

/**
 * 用户数据访问对象
 *
 * @author thor
 */
public interface UserDao {

    /**
     * 根据Id查找用户
     *
     * @param id the id
     * @return the user if exists
     */
    User getUserById(Integer id);

}
