package com.tulun.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tulun.bean.Msg;
import com.tulun.bean.ServerFile;
import com.tulun.bean.User;
import com.tulun.cantant.EnMsgType;
import com.tulun.dao.MsgMapper;
import com.tulun.dao.ServerFileMapper;
import com.tulun.dao.UserMapper;
import com.tulun.util.JsonUtils;
import com.tulun.util.PortUtils;
import com.tulun.util.SerializeUtil;
import io.netty.channel.Channel;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @ClassName Assist
 * @Description 辅助类 辅助处理
 * @Author lzq
 * @Date 2019/8/2 19:19
 * @Version 1.0
 **/
public class Assist {
    private static String resource = "mybatis-config.xml";
    private Random random = new Random();


    /**
     * 获取SqlSession对象
     * @return
     */
    private static SqlSession getSqlSession() {
        //读取配置文件
        InputStream asStream = null;
        try {
            asStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            System.out.println("读取配置文件失败...");
            e.printStackTrace();
        }

        //创建sqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(asStream);

        //创建sqlSession
        SqlSession sqlSession = sqlSessionFactory.openSession();
        return sqlSession;
    }


    /**
     * 判断该用户、密码是否登录成功，即在数据库中是否存在
     * @param userName
     * @param userPasswd
     * @return
     */
    public int dbJudge(String userName, String userPasswd) {
        //通过动态代理产生UserMapper对象
        UserMapper mapper = getSqlSession().getMapper(UserMapper.class);
        User user = mapper.getJudgeUname(userName);
        if(user == null) {
            return -1;  //不存在该用户，需要先注册
        }
        user = mapper.getJudgeUnameAndPasswd(userName,userPasswd);
        if(user == null) {
            return 0;  //密码不正确
        }
        return 1;  //登录成功
    }

    /**
     * 注册
     * @param userName
     * @param userPasswd
     * @param uemail
     * @return
     */
    public int dbRegister(String userName, String userPasswd, String uemail) {
        //通过动态代理产生UserMapper对象
        SqlSession sqlSession = getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User user = mapper.getJudgeUname(userName);
        if(user != null) {
            return 0;  //用户名已经存在
        }

        //往数据库添加新用户
        User newUser = new User();
        newUser.setUname(userName);
        newUser.setUpasswd(userPasswd);
        newUser.setUemail(uemail);

        mapper.addUser(newUser);
        sqlSession.commit();

        return 1; //注册成功
    }

    /**
     * 判断该用户的用户名、邮箱是否存在
     * @param userName
     * @param uemail
     * @return
     */
    public boolean select(String userName, String uemail) {
        //通过动态代理产生UserMapper对象
        SqlSession sqlSession = getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        User user = mapper.getNameAndEmail(userName,uemail);
        if(user == null) {
            return false;
        }

        return true; //成功
    }


    /**
     * 修改密码
     * @param userName
     * @param oldPasswd
     * @param newPasswd
     * @return
     */
    public int updatePasswd(String userName, String oldPasswd, String newPasswd) {
        //通过动态代理产生UserMapper对象
        SqlSession sqlSession = getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        User user = mapper.getJudgeUnameAndPasswd(userName,oldPasswd);

        if(user == null) {
            return 0;
        }else {
            mapper.updatePasswd(newPasswd,userName);
            sqlSession.commit();
            return 1;
        }
    }

    /**
     * 获取所有用户的列表
     * @return
     * @param me
     */
    public String selectAllUser(String me) {
        //通过动态代理产生UserMapper对象
        SqlSession sqlSession = getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        StringBuilder stringBuilder = new StringBuilder();

        User[] users = mapper.getId();
        for (User u : users) {
            if(u.getUname().equals(me)) {
                continue;  //跳过自己
            }
            stringBuilder.append(u.toString());
        }

        return stringBuilder.toString();
    }

    /**
     * 发送和记录验证码
     * @param userName
     * @param uemail
     */
    public void disposeVerificationCode(String userName, String uemail,Jedis jedis) {
        SimpleEmail email = new SimpleEmail();
        email.setHostName("smtp.163.com");//邮件服务器
        int x = random.nextInt(899999)+100000;



        String yzm = String.valueOf(x);  //验证码
        jedis.set(userName.getBytes(), SerializeUtil.serialize(yzm)); //将用户名和验证码放在Redis里面

        System.out.println("发送验证码 "+x+" 给用户 "+userName);
        email.setAuthentication("lzq_zd@163.com", "lzq436280");//邮件登录用户名及授权码
        email.setSSLOnConnect(true);
        email.setSubject("用户忘记密码");//主题名称
        email.setCharset("UTF-8");//设置字符集编码
        try {
            email.setFrom("lzq_zd@163.com", "聊天室后台");//发送方邮箱、发送方名称
            String str = "你的验证码为 "+x+ ",请在60s内使用！";
            email.setMsg(String.valueOf(str));//发送内容
            email.addTo(String.valueOf(uemail));//接收方邮箱
            email.send();//发送方法
            jedis.expire(userName,120);  //设置等待时间
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }


    /**
     * 存储未发送的消息
     * @param msg
     */
    public void storeMsg(Msg msg) {
        SqlSession sqlSession = getSqlSession();
        MsgMapper mapper = sqlSession.getMapper(MsgMapper.class);
        mapper.addMsg(msg);
        sqlSession.commit();
    }

    /**
     * 通过名字获取用户信息
     * @param name
     * @return
     */
    public String getName(String name) {
        UserMapper mapper = getSqlSession().getMapper(UserMapper.class);
        User user = mapper.getName(name);
        String s = user.getUname()+" "+user.getUemail();
        return s;
    }

    /**
     * 群聊
     * @param concurrentHashMap 所有在线用户集合
     * @param forName 发送方
     * @param getMsg  消息
     * @param time    发送时间
     * @param enMsgChatAll
     */
    public void sendAll(ConcurrentHashMap<String,Channel> concurrentHashMap, String forName, String getMsg, String time, EnMsgType enMsgChatAll) {
        Iterator<Map.Entry<String, Channel>> iterator = concurrentHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Channel> next = iterator.next();

            if (next.getKey().equals(forName)) {
                continue;  //不用发送给自己
            }

            Channel channel = next.getValue(); //拿到其他用户的通道
            //封装返回数据类型
            ObjectNode objectNode1 = JsonUtils.getObjectNode();
            objectNode1.put("type", String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
            objectNode1.put("srctype", String.valueOf(enMsgChatAll)); //群聊
            objectNode1.put("forName", forName);
            objectNode1.put("msg", getMsg);
            objectNode1.put("time", time);

            String msg = objectNode1.toString();
            channel.writeAndFlush(msg);
        }
    }


    /**
     * 检测是否有该用户未接收的消息
     * @param name
     * @param channel
     */
    public void monitorUnsentMsg(String name,Channel channel) {
        //通过动态代理产生UserMapper对象
        SqlSession sqlSession = getSqlSession();
        MsgMapper mapper = sqlSession.getMapper(MsgMapper.class);

        Msg[] msgs = mapper.getToNameAndSign(name);

        for (Msg Msg : msgs) {
            int id = Msg.getId();
            String forName = Msg.getForName();
            String getMsg = Msg.getMsg();
            String time = Msg.getDatetime();
            //封装返回数据类型
            ObjectNode objectNode1 = JsonUtils.getObjectNode();
            objectNode1.put("type", String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
            objectNode1.put("srctype", String.valueOf(EnMsgType.EN_MSG_CHAT)); //单人聊天
            objectNode1.put("forName", forName);
            objectNode1.put("msg", getMsg);
            objectNode1.put("time", time);

            String msg = objectNode1.toString();
            channel.writeAndFlush(msg);

            mapper.updateSign(id); //消息已经发送
            sqlSession.commit();
        }

    }

    /**
     * 验证用户状态：不存在、在线、不在线
     * @param concurrentHashMap2
     * @param toName  接收方
     */
    public int verifyToName(ConcurrentHashMap<String,Channel> concurrentHashMap2,
                         String toName) {
        //通过动态代理产生UserMapper对象
        SqlSession sqlSession = getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User user = mapper.getName(toName);

        if(user == null) {  //这个用户不存在
            return -1;
        }

        //用户在线
        if(concurrentHashMap2.containsKey(toName)) {
            return 1;
        }

        return 0;  //与户不在线
    }

    /**
     * 接收方不在线，把文件存储起来
     * @param forName
     * @param toName
     * @param path
     * @param time
     * @param fileName
     */
    public void storageFile(String forName, String toName, String path, String time,String fileName) {
        //通过动态代理产生UserMapper对象
        SqlSession sqlSession = getSqlSession();
        ServerFileMapper mapper = sqlSession.getMapper(ServerFileMapper.class);
        ServerFile serverFile = new ServerFile();
        serverFile.setForName(forName);
        serverFile.setToName(toName);
        serverFile.setPath(path);
        serverFile.setTime(time);
        serverFile.setFileName(fileName);
        serverFile.setSign(0);
        mapper.addFile(serverFile);
        sqlSession.commit();
    }

    /**
     * 查找这个用户是否有未接受的文件
     * @param toName
     * @param executorService
     * @param channel
     */
    public void sendStorageFile(String toName,ExecutorService executorService,Channel channel) {
        //通过动态代理产生UserMapper对象
        SqlSession sqlSession = getSqlSession();
        ServerFileMapper mapper = sqlSession.getMapper(ServerFileMapper.class);
        ServerFile[] toNameAndSign = mapper.getToNameAndSign(toName);

        for (ServerFile file : toNameAndSign) {
            int port = PortUtils.getFreePort();
            String forName = file.getForName();
            String path = file.getPath();
            String time = file.getTime();
            String fileName = file.getFileName();
            int id = file.getId();
            executorService.execute(new SendStorageFile(forName,path,port,time,fileName));

            //封装返回数据类型
            ObjectNode objectNode1 = JsonUtils.getObjectNode();
            objectNode1.put("type", String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
            objectNode1.put("srctype", String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE)); //接收文件
            objectNode1.put("forName", forName);
            objectNode1.put("port", port);

            String msg = objectNode1.toString();
            channel.writeAndFlush(msg);

            file.setSign(1);
            mapper.updateSign(id);
            sqlSession.commit();
        }
    }
}
