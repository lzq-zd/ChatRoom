package com.tulun.dao;

import com.tulun.bean.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @ClassName UserMapper
 * @Description
 * @Author lzq
 * @Date 2019/7/28 00:05
 * @Version 1.0
 **/
public interface UserMapper {

    /**
     * 找是否存在这个用户
     * @param name
     * @return
     */
    @Select("select * from User where Uname = #{name}")
    public User getJudgeUname(String name);

    /**
     * 判断用户对应的密码是否正确
     * @param name
     * @param passwd
     * @return
     */
    @Select("select * from User where Uname = #{name} and Upasswd = #{passwd}")
    public User getJudgeUnameAndPasswd(@Param("name") String name, @Param("passwd") String passwd);

    /**
     * 查找对应的用户名、邮箱是否存在
     * @param name
     * @param email
     * @return
     */
    @Select("select * from User where Uname = #{name} and Uemail = #{email}")
    public User getNameAndEmail(@Param("name") String name, @Param("email") String email);

    @Select("select * from User where Uname = #{name}")
    public User getName(String name);

    /**
     * 获取最大id
     * @return
     */
    @Select("select max(Uid) from user")
    public User getMaxID();

    /**
     * 查找指定id的用户
     * @return
     */
    @Select("select * from user")
    public User[] getId();

    /**
     * 插入新用户
     * @param user
     */
    @Insert("insert into user (Uname,Upasswd,Uemail) values(#{Uname},#{Upasswd},#{Uemail})")
    public void addUser(User user);

    /**
     * 修改密码
     * @param passwd
     * @param name
     */
    @Update("update User set Upasswd = #{passwd} where Uname=#{name}")
    public void updatePasswd(@Param("passwd") String passwd, @Param("name") String name);
}
