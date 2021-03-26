package org.example.test;

import lombok.SneakyThrows;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.example.mapper.UserMapper;
import org.example.test.impl.UserDaoImpl;
import org.example.po.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;

@RunWith(JUnit4.class)
public class MybatisTest {

    private SqlSessionFactory sqlSessionFactory;

    @SneakyThrows
    @Before
    public void before(){
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        InputStream inputStream = Resources.getResourceAsStream("SqlMapConfig.xml");
        this.sqlSessionFactory = sqlSessionFactoryBuilder.build(inputStream);
    }

    @Test
    public void testUserDao() throws IOException {
        UserDao userDao = new UserDaoImpl(sqlSessionFactory);
        User user = userDao.getUserById(1);
        Assert.assertNotNull(user);
        System.out.println(user);
    }

    @Test
    public void testUserMapper(){
        try (SqlSession sqlSession = sqlSessionFactory.openSession()){
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            final User user = mapper.getUserById(2);
            Assert.assertNotNull(user);
            System.out.println(user);
        }
    }
}
