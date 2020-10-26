package com.tulun.dao;

import com.tulun.bean.ServerFile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @ClassName ServerFileMapper
 * @Description
 * @Author lzq
 * @Date 2019/8/10 14:52
 * @Version 1.0
 **/
public interface ServerFileMapper {
    //查找指定接收方
    @Select("select * from serverfile where toName = #{toName} and sign = 0")
    public ServerFile[] getToNameAndSign(String toName);

    @Update("update serverfile set sign=1 where id = #{id}")
    public void updateSign(int id);

    //添加新的消息体
    @Insert("insert into serverfile(forName,toName,path,sign,time,fileName) values(#{forName},#{toName},#{path},#{sign},#{time},#{fileName})")
    public void addFile(ServerFile serverFile);
}
