package com.tulun.dao;

import com.tulun.bean.Msg;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @ClassName MsgMapper
 * @Description
 * @Author lzq
 * @Date 2019/8/2 20:45
 * @Version 1.0
 **/
public interface MsgMapper {
    //查找指定接收方
    @Select("select * from msg where toName = #{toName} and sign = 0")
    public Msg[] getToNameAndSign(String toName);

    @Update("update msg set sign=1 where id = #{id}")
    public void updateSign(int id);

    //添加新的消息体
    @Insert("insert into msg(forName,toName,msg,datetime,sign) values(#{forName},#{toName},#{msg},#{datetime},#{sign})")
    public void addMsg(Msg msg);
}
