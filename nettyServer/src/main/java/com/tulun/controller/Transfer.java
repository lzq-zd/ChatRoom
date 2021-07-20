package com.tulun.controller;

import com.tulun.bean.Msg;
import com.tulun.cantant.EnMsgType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tulun.util.JsonUtils;
import com.tulun.util.PortUtils;
import com.tulun.util.SerializeUtil;
import io.netty.channel.Channel;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @ClassName Transfer
 * @Description 处理业务逻辑
 * @Author lzq
 * @Date 2019/7/27 08:54
 * @Version 1.0
 **/
public class Transfer {
    private Jedis jedis = null;
    private Assist assist = new Assist();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private ExecutorService executorService = Executors.newCachedThreadPool();


    //存储 通道 —— 是否正常下线
    private ConcurrentHashMap<Channel,Boolean> concurrentHashMap = new ConcurrentHashMap<>();

    //存储 通道——账户信息
    private ConcurrentHashMap<Channel,String> concurrentHashMap1 = new ConcurrentHashMap<>();
    //存储 用户信息——通过
    private ConcurrentHashMap<String,Channel> concurrentHashMap2 = new ConcurrentHashMap<>();


    public Transfer() {
        JedisPoolConfig config = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(config, "127.0.0.1", 6379, 10000, null, 0);
        jedis = jedisPool.getResource();
        new MonitorChannel(concurrentHashMap,concurrentHashMap1,concurrentHashMap2,
                assist,sdf).start();
    }


    /**
     * 消息解析器
     * @param msg
     * @param channel
     * @return
     */
    public String process(String msg,Channel channel) {
        ObjectNode objectNode = JsonUtils.getObjectNode(msg);
        String type = objectNode.get("type").asText();  //解析数据类型

        //处理用户登录请求
        if(String.valueOf(EnMsgType.EN_MSG_LOGIN).equals(type)) {
            String userName = objectNode.get("name").asText();
            String userPasswd = objectNode.get("passwd").asText();
            System.out.println("登陆操作：用户名"+userName+",密码"+userPasswd);

            //封装返回数据类型
            ObjectNode objectNode1 = JsonUtils.getObjectNode();
            objectNode1.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
            objectNode1.put("srctype",String.valueOf(EnMsgType.EN_MSG_LOGIN)); //用户登录消息

            //该用户已经登录，登录失败
            if(concurrentHashMap2.containsKey(userName)) {
                objectNode1.put("code",50);
                String recvMsg = objectNode1.toString();
                return recvMsg;
            }else {

                //数据库判断登录是否成功
                int sign = assist.dbJudge(userName, userPasswd);

                if (sign == -1) {
                    //登录失败！需要先注册
                    objectNode1.put("code", 0);
                } else if (sign == 0) {
                    //密码错误
                    objectNode1.put("code", 100);
                } else {
                    //登录成功
                    objectNode1.put("code", 200);
                    //登录成功，加入到concurrentHashMap中去
                    if (!concurrentHashMap1.containsKey(channel)) {
                        concurrentHashMap1.put(channel, userName);
                        concurrentHashMap2.put(userName, channel);
                        concurrentHashMap.put(channel, false);
                        System.out.println(concurrentHashMap2.containsKey(userName));
                    }
                    this.assist.sendStorageFile(userName,executorService,channel);
                    this.assist.monitorUnsentMsg(userName, channel);
                    this.assist.sendAll(concurrentHashMap2, userName, "上线", sdf.format(new Date()), EnMsgType.EN_MSG_NOTIFY_ONLINE);  //广播消息
                }
            }
            String recvMsg = objectNode1.toString();
            return recvMsg;
        }

        //处理用户注册请求
        else if(String.valueOf(EnMsgType.EN_MSG_REGISTER).equals(type)) {
            String userName = objectNode.get("name").asText();
            String userPasswd = objectNode.get("passwd").asText();
            String uemail = objectNode.get("email").asText();
            System.out.println("注册操作：用户名"+userName+",密码"+userPasswd+",邮箱"+uemail);

            //封装返回数据类型
            ObjectNode objectNode1 = JsonUtils.getObjectNode();
            objectNode1.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
            objectNode1.put("srctype",String.valueOf(EnMsgType.EN_MSG_REGISTER)); //用户注册消息

            if(concurrentHashMap1.containsKey(channel)) {
                //登录的同时是不能注册的
                objectNode1.put("code",100);
                return objectNode1.toString();
            }

            int sign = assist.dbRegister(userName,userPasswd,uemail);

            if(sign == 0) {
                objectNode1.put("code",0);
            }else {
                if(!concurrentHashMap1.containsKey(channel)) {
                    concurrentHashMap1.put(channel,userName);
                    concurrentHashMap2.put(userName,channel);
                    concurrentHashMap.put(channel,false);
                }
                objectNode1.put("code",200);
                this.assist.sendAll(concurrentHashMap2, userName, "上线", sdf.format(new Date()), EnMsgType.EN_MSG_NOTIFY_ONLINE);  //广播消息
            }

            String recvMsg = objectNode1.toString();
            return recvMsg;
        }

        //忘记密码请求
        else if(String.valueOf(EnMsgType.EN_MSG_FORGET_PWD).equals(type)) {
            String userName = objectNode.get("name").asText();
            String uemail = objectNode.get("email").asText();
            System.out.println("忘记密码操作：用户名"+userName+",邮箱"+uemail);

            //封装返回数据类型
            ObjectNode objectNode1 = JsonUtils.getObjectNode();
            objectNode1.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
            objectNode1.put("srctype",String.valueOf(EnMsgType.EN_MSG_FORGET_PWD)); //验证码

            boolean sign = assist.select(userName,uemail);

            if(sign) {
                objectNode1.put("code",200);
                assist.disposeVerificationCode(userName,uemail,jedis); //仅仅是发送验证码到邮箱，记录当前验证码
            }else {
                objectNode1.put("code",0);
            }

            String recvMsg = objectNode1.toString();
            return recvMsg;
        }

        //验证码
        else if(String.valueOf(EnMsgType.EN_MSG_YZM).equals(type)) {
            String name = objectNode.get("name").asText();
            String yzm = objectNode.get("yzm").asText();

            //封装返回数据类型
            ObjectNode objectNode1 = JsonUtils.getObjectNode();
            objectNode1.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息

            if(jedis.exists(name)) {
                try {
                    String y = (String) SerializeUtil.deserialize(jedis.get(name.getBytes()));
                    if(y.equals(yzm)) {
                        objectNode1.put("srctype", String.valueOf(EnMsgType.EN_MSG_YZM)); //验证码
                        objectNode1.put("code", 200);
                        if(!concurrentHashMap1.containsKey(channel)) {
                            concurrentHashMap1.put(channel,name);
                            concurrentHashMap2.put(name,channel);
                            concurrentHashMap.put(channel,false);
                        }
                        this.assist.sendStorageFile(concurrentHashMap1.get(channel),executorService,channel);
                        this.assist.monitorUnsentMsg(concurrentHashMap1.get(channel), channel);
                        this.assist.sendAll(concurrentHashMap2,concurrentHashMap1.get(channel),"上线",sdf.format(new Date()),EnMsgType.EN_MSG_NOTIFY_ONLINE);  //广播消息
                    }else {
                        objectNode1.put("srctype", String.valueOf(EnMsgType.EN_MSG_YZM)); //验证码
                        objectNode1.put("code",0);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                objectNode1.put("srctype", String.valueOf(EnMsgType.EN_MSG_YZM)); //验证码
                objectNode1.put("code",0);
            }

            String recvMsg = objectNode1.toString();
            return recvMsg;
        }

        //修改密码
        else if(String.valueOf(EnMsgType.EN_MSG_MODIFY_PWD).equals(type)) {
            String oldPasswd = objectNode.get("oldPasswd").asText();
            String newPasswd = objectNode.get("newPasswd").asText();

            String userName = concurrentHashMap1.get(channel);

            System.out.println("忘记密码操作：用户名"+userName);

            //封装返回数据类型
            ObjectNode objectNode1 = JsonUtils.getObjectNode();
            objectNode1.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
            objectNode1.put("srctype",String.valueOf(EnMsgType.EN_MSG_MODIFY_PWD)); //忘记密码

            int sign = assist.updatePasswd(userName,oldPasswd,newPasswd);

            if(sign == 0) {
                objectNode1.put("code",0);
            }else {
                objectNode1.put("code",200);
            }
            String recvMsg = objectNode1.toString();
            return recvMsg;
        }

        //获取所有用户列表
        else if(String.valueOf(EnMsgType.EN_MSG_GET_ALL_USERS).equals(type)) {
            String me = objectNode.get("me").asText();  //用户自己是谁，不用查自己的
            //封装返回数据类型
            ObjectNode objectNode1 = JsonUtils.getObjectNode();
            objectNode1.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
            objectNode1.put("srctype",String.valueOf(EnMsgType.EN_MSG_GET_ALL_USERS)); //打印列表

            System.out.println("<"+me+"> 获取在线用户列表");
            Iterator<Map.Entry<Channel, String>> iterator = concurrentHashMap1.entrySet().iterator();
            StringBuilder s = new StringBuilder();
            while (iterator.hasNext()) {
                Map.Entry<Channel, String> next = iterator.next();
                String name = next.getValue();
                String yh = assist.getName(name);  //拿到在线用户的用户名和邮箱
                s.append("[").append(yh).append("]");
            }

            if(s.length() > 0) {
                objectNode1.put("code",200);
                objectNode1.put("content",s.toString());
            }else {
                objectNode1.put("code",0);
                objectNode1.put("content","用户列表为空！！");
            }
            String recvMsg = objectNode1.toString();
            return recvMsg;
        }

        //单人聊天
        else if(String.valueOf(EnMsgType.EN_MSG_CHAT).equals(type)) {
            String forName = objectNode.get("forName").asText();
            String toName = objectNode.get("toName").asText();
            String getMsg = objectNode.get("msg").asText();
            String time = objectNode.get("time").asText();

            //封装返回数据类型
            ObjectNode objectNode1 = JsonUtils.getObjectNode();
            objectNode1.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
            objectNode1.put("srctype",String.valueOf(EnMsgType.EN_MSG_CHAT)); //单人聊天

            System.out.println(forName+"→"+getMsg+"→"+toName+"\t"+time);

            boolean isToName = concurrentHashMap2.containsKey(toName);

            if(isToName) {  //在线转发消息
                objectNode1.put("forName",forName);
                objectNode1.put("msg",getMsg);
                objectNode1.put("time",time);
                String recvMsg = objectNode1.toString();
                concurrentHashMap2.get(toName).writeAndFlush(recvMsg);
            }else {  //不在线记录消息
                Msg msg1 = new Msg();
                msg1.setForName(forName);
                msg1.setToName(toName);
                msg1.setMsg(getMsg);
                msg1.setDatetime(time);
                msg1.setSign(0);
                assist.storeMsg(msg1);
            }
        }

        //群聊
        else if(String.valueOf(EnMsgType.EN_MSG_CHAT_ALL).equals(type)) {
            String forName = objectNode.get("forName").asText();
            String getMsg = objectNode.get("msg").asText();
            String time = objectNode.get("time").asText();
            System.out.println("<"+forName+"> 发送消息"+getMsg);

            this.assist.sendAll(concurrentHashMap2,forName,getMsg,time,EnMsgType.EN_MSG_CHAT_ALL);  //广播消息
        }

        //端口号，发送文件前的准备
        else if(String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE).equals(type)) {
            String forName = concurrentHashMap1.get(channel); //发送方
            String toName = objectNode.get("toName").asText();  //接收方

            //得到接收方状态
            int verify = this.assist.verifyToName(concurrentHashMap2, toName);
            if(verify == -1) {
                //封装返回数据类型
                ObjectNode objectNode1 = JsonUtils.getObjectNode();
                objectNode1.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
                objectNode1.put("srctype",String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE)); //发送文件
                objectNode1.put("code",-1);
                String Msg = objectNode1.toString();
                return Msg;
            }else if(verify == 1) { //在线
                //给发送方、接收方的端口号
                int forPort = PortUtils.getFreePort();
                int toPort = PortUtils.getFreePort();
                System.out.println(forName+"发送文件给"+toName);
                executorService.execute(new SendFile(forPort,toPort)); //起新线程处理

                //封装返回数据类型
                ObjectNode objectNode1 = JsonUtils.getObjectNode();
                objectNode1.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
                objectNode1.put("srctype",String.valueOf(EnMsgType.EN_MSG_SEND_FILE)); //发送文件
                objectNode1.put("port",forPort);
                String forMsg = objectNode1.toString();
                channel.writeAndFlush(forMsg);

                //封装返回数据类型
                ObjectNode objectNode2 = JsonUtils.getObjectNode();
                objectNode2.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
                objectNode2.put("srctype",String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE)); //接收文件
                objectNode2.put("port",toPort);
                objectNode2.put("forName",forName);
                String toMsg = objectNode2.toString();
                concurrentHashMap2.get(toName).writeAndFlush(toMsg);
            }else {   //不在线，但是存在这个用户
                //把东西储存起来，上线在发
                int forPort = PortUtils.getFreePort();  //给发送方端口号

                //启动子线程接收文件
                executorService.execute(new StorageFile(forName,toName,forPort,assist));

                //封装返回数据类型
                ObjectNode objectNode1 = JsonUtils.getObjectNode();
                objectNode1.put("type",String.valueOf(EnMsgType.EN_MSG_ACK));  //响应消息
                objectNode1.put("srctype",String.valueOf(EnMsgType.EN_MSG_SEND_FILE)); //发送文件
                objectNode1.put("port",forPort);

                channel.writeAndFlush(objectNode1.toString());
            }
        }

        //用户退出聊天  下线
        else if(String.valueOf(EnMsgType.EN_MSG_NOTIFY_OFFLINE).equals(type)) {
            String time = objectNode.get("time").asText();
            String name = concurrentHashMap1.get(channel); //得到发送消息用户的名字
            String getMsg = "下线";
            System.out.println("<"+name+">"+getMsg);

            concurrentHashMap.put(channel,true);
            this.assist.sendAll(concurrentHashMap2,name,getMsg,time,EnMsgType.EN_MSG_NOTIFY_OFFLINE); //广播
            concurrentHashMap1.remove(channel);
            concurrentHashMap2.remove(name);
        }


        return "";
    }



}


