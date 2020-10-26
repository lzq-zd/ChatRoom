package com.tulun.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tulun.controller.EnMsgType;
import com.tulun.netty.ClientHandler;
import com.tulun.util.JsonUtils;
import io.netty.channel.Channel;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @ClassName Assist
 * @Description 辅助类
 * @Author lzq
 * @Date 2019/8/2 20:10
 * @Version 1.0
 **/
public class Assist {
    private Scanner scanner = new Scanner(System.in);
    private SendService sendService = null;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Assist(SendService sendService) {
        this.sendService = sendService;
    }

    /**
     * 登录业务逻辑
     * @param name
     * @param passwd
     * @param channel
     * @param hashMap
     */
    public void doLogin(String name, String passwd, Channel channel, HashMap<String, String> hashMap) {
        //封装JSON数据
        ObjectNode node = JsonUtils.getObjectNode();
        node.put("name", name);
        node.put("passwd", passwd);
        node.put("type", String.valueOf(EnMsgType.EN_MSG_LOGIN));
        String msg = node.toString();

        //发送服务端
        channel.writeAndFlush(msg);

        //等待服务端返回登录结果
        int code = -1;
        try {
            code = ClientHandler.queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //结果展示：
        if (code == 200) {
            //登录成功
            hashMap.put("me",name);
            SendService.flag = true;
            this.sendService.InputMemu();
        } else if(code == 0){
            //需要先注册
            System.out.println("用户不存在，请先注册！！！");
        } else if(code == 50) {
            System.out.println("该用户已经登录！！！");
        } else {
            //密码错误
            System.out.println("密码错误，请点击[忘记密码]或重新[注册]");
        }
    }

    /**
     * 注册
     * @param name
     * @param passwd
     * @param email
     * @param channel
     * @param hashMap
     */
    public void register(String name, String passwd, String email, Channel channel, HashMap<String, String> hashMap) {
        //封装JSON数据
        ObjectNode node = JsonUtils.getObjectNode();
        node.put("name", name);
        node.put("passwd", passwd);
        node.put("email",email);
        node.put("type", String.valueOf(EnMsgType.EN_MSG_REGISTER));
        String msg = node.toString();

        //发送服务端
        channel.writeAndFlush(msg);

        //等待服务端返回结果
        int code = -1;
        try {
            code = ClientHandler.queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //结果展示：
        if (code == 200) {
            //注册成功
            hashMap.put("me",name);
            SendService.flag = true;
            this.sendService.InputMemu();
        } else if(code == 100) {
            System.out.println("你已登录，请先退出登录，否者无法注册！！！");
        } else {
            System.out.println("注册失败，用户名已存在，请选择其他用户名重新注册！");
        }
    }

    /**
     * 密码
     * @param name
     * @param email
     * @param channel
     * @param hashMap
     */
    public void findPasswd(String name, String email, Channel channel, HashMap<String, String> hashMap) {
        //封装JSON数据
        ObjectNode node = JsonUtils.getObjectNode();
        node.put("name", name);
        node.put("email",email);
        node.put("type", String.valueOf(EnMsgType.EN_MSG_FORGET_PWD));
        String msg = node.toString();

        //发送服务端
        channel.writeAndFlush(msg);

        //等待服务端返回结果
        int code = -1;
        try {
            code = ClientHandler.queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(code == 200) {  //获取验证码
            System.out.println("请输入你的验证码：");
            String s = scanner.nextLine();
            disposeVerificationCode(name,s,channel,hashMap); //验证码操作
        }else {
            System.out.println("不存在这个用户或邮箱！！");
        }

    }

    /**
     * 验证码操作
     * @param name
     * @param s
     * @param channel
     * @param hashMap
     */
    public void disposeVerificationCode(String name, String s, Channel channel, HashMap<String, String> hashMap) {
        //封装JSON数据
        ObjectNode node1 = JsonUtils.getObjectNode();
        node1.put("name",name);
        node1.put("yzm",s);
        node1.put("type", String.valueOf(EnMsgType.EN_MSG_YZM));
        String msg = node1.toString();

        //发送服务端
        channel.writeAndFlush(msg);

        //等待服务端返回结果
        int code = -1;
        try {
            code = ClientHandler.queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(code == 200) {  //验证码正确且未超时
            System.out.println("登录成功");
            hashMap.put("me",name);
            this.sendService.InputMemu();
        }else {
            System.out.println("验证码错误或超时！！");
        }
    }


    /**
     * 修改密码
     * @param oldPasswd
     * @param newPasswd
     * @param channel
     */
    public void updatePasswd(String oldPasswd, String newPasswd,Channel channel) {
        //封装JSON数据
        ObjectNode node = JsonUtils.getObjectNode();
        node.put("oldPasswd", oldPasswd);
        node.put("newPasswd",newPasswd);
        node.put("type", String.valueOf(EnMsgType.EN_MSG_MODIFY_PWD));
        String msg = node.toString();

        //发送服务端
        channel.writeAndFlush(msg);

        //等待服务端返回结果
        int code = -1;
        try {
            code = ClientHandler.queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(code == 200) {
            System.out.println("修改密码成功...");
        }else {
            System.out.println("密码修改失败，请检查你的用户名和原密码输入是否正确！");
        }
    }

    /**
     * 查询所有用户的信息
     * @param channel
     * @param me
     */
    public void selectAllUser(Channel channel, String me) {
        //封装JSON数据
        ObjectNode node = JsonUtils.getObjectNode();
        node.put("type", String.valueOf(EnMsgType.EN_MSG_GET_ALL_USERS));
        node.put("me",me); //不用查自己的
        String msg = node.toString();

        channel.writeAndFlush(msg);

        //等待服务端返回结果
        int code = -1;

        try {
            code = ClientHandler.queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(code == 200) {
            System.out.println("获取所有用户列表成功...");
            try {
                String s = ClientHandler.content.take();
                for (int i = 0; i < s.length(); i++) {
                    if(s.charAt(i) == '[' && i != 0) {
                        System.out.println();
                    }
                    System.out.print(s.charAt(i));
                }
                System.out.println();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            System.out.println("获取所有用户失败，请稍后重试");
        }
    }

    /**
     * 开启单聊
     * @param forName
     * @param toName
     * @param channel
     */
    public void oneChat(String forName, String toName, Channel channel) {

        while (true) {
            chatTechweb(toName);
            String line = scanner.nextLine();

            if("退出".equals(line)) {
                break;
            }
            //封装JSON数据
            ObjectNode node = JsonUtils.getObjectNode();
            node.put("type", String.valueOf(EnMsgType.EN_MSG_CHAT));
            node.put("forName",forName);
            node.put("toName",toName);
            node.put("msg",line);
            node.put("time",sdf.format(new Date()));

            String msg = node.toString();
            channel.writeAndFlush(msg);  //发送完就不管了

        }
        System.out.println("===============================");
    }



    /**
     * 单人聊天界面
     * @param toName
     */
    public void chatTechweb(String toName) {
        System.out.println("================"+toName+"================");
        System.out.println("输入[退出]，退出聊天（其他默认是消息）");
    }

    /**
     * 群聊
     * @param channel
     * @param me
     */
    public void groupChat(Channel channel, String me) {
        while (true) {
            String line = scanner.nextLine();
            if("退出".equals(line)) {
                break;
            }
            //封装JSON数据
            ObjectNode node = JsonUtils.getObjectNode();
            node.put("type", String.valueOf(EnMsgType.EN_MSG_CHAT_ALL));
            node.put("forName",me);
            node.put("msg",line);
            node.put("time",sdf.format(new Date()));

            String msg = node.toString();
            channel.writeAndFlush(msg);  //发送完就不管了
        }
        System.out.println("====================="+"你已退出群聊"+"=====================");
    }

    
    /**
     * 发送文件
     * @param toName
     * @param path
     * @param channel
     */
    public void sendFile(String toName, String path, Channel channel) {
        File file = new File(path);
        if(!file.exists()) {
            System.out.println("文件路径不存在!!!");
            return;
        }
        //封装JSON数据
        ObjectNode node = JsonUtils.getObjectNode();
        //准备获取端口号
        node.put("type", String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE));
        node.put("toName",toName);
        String msg = node.toString();
        channel.writeAndFlush(msg);

        int port = 0;

        try {
            port = ClientHandler.queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(port < 0) {
            System.out.println("该用户不存在，无法发送文件！！！");
        }
        
        if(port > 0) {
            //启动新线程 发送文件
            InetSocketAddress insocket = (InetSocketAddress) channel.remoteAddress();
            String ip = insocket.getAddress().getHostAddress();
            new SendFile(port,file,sdf.format(new Date()),ip).start();
        }
    }



    /**
     * 退出聊天
     * @param channel
     */
    public void quit(Channel channel) {
        //封装JSON数据
        ObjectNode node = JsonUtils.getObjectNode();
        node.put("type", String.valueOf(EnMsgType.EN_MSG_NOTIFY_OFFLINE));
        node.put("time",sdf.format(new Date()));

        String msg = node.toString();
        channel.writeAndFlush(msg);  //发送完就不管了
    }


    /**
     * 用户登录界面
     */
    public void loginView() {
        System.out.println("===========用户登录==============");
        System.out.println("1.登录");
        System.out.println("2.注册");
        System.out.println("3.忘记密码");
        System.out.println("4.主菜单");
        System.out.println("5.退出系统");
        System.out.println("=================================");
    }


    /**
     * 主菜单页面
     */
    public void showMainMemu() {
        System.out.println("====================系统使用说明====================");
        System.out.println("                         注：输入多个信息用\":\"分割");
        System.out.println("1.输入modifypwd:username 表示该用户要修改密码");
        System.out.println("2.输入getallusers 表示用户要查询所有人员信息");
        System.out.println("3.输入username:xxx 表示一对一聊天"); //
        System.out.println("4.输入all:xxx 表示发送群聊消息");
        System.out.println("5.输入sendfile:xxx 表示发送文件请求:[sendfile][接收方用户名][发送文件路径]");
        System.out.println("6.输入quit 表示该用户下线，注销当前用户重新登录");
        System.out.println("7.输入help查看系统菜单");
        System.out.println("===================================================");
    }

}
