package com.tulun.service;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Scanner;

/**
 * @ClassName SendService
 * @Description 客户端本地 只处理按钮
 * @Author lzq
 * @Date 2019/7/27 23:25
 * @Version 1.0
 **/
public class SendService {
    public static boolean flag = false;  //记录自己的状态，是否登录
    private Channel channel;
    Scanner scanner = new Scanner(System.in);
    private Assist assist;
    //记录自己底的名字
    private HashMap<String,String> hashMap = new HashMap<>(1);

    public SendService(Channel channel) {
        this.channel = channel;
        this.assist = new Assist(this);
        hashMap.put("me","");
    }

    /**
     * 用户登录界面处理
     */
    public void sendMsg() {
        scanner.useDelimiter("\n");
        while (true) {
            this.assist.loginView();
            String line = scanner.nextLine();

            //登录操作
            if ("1".equals(line)) {
                System.out.println("请输入账号：");
                String name = scanner.nextLine();
                System.out.println("请输入密码：");
                String passwd = scanner.nextLine();

                System.out.println("登录中...");
                this.assist.doLogin(name, passwd,channel,hashMap);
            }

            //注册操作
            else if("2".equals(line)){
                System.out.println("请输入账号：");
                String name = scanner.nextLine();
                System.out.println("请输入密码：");
                String passwd = scanner.nextLine();
                System.out.println("请输入邮箱：（仅限qq邮箱和163邮箱）");
                String email = scanner.nextLine();

                System.out.println("注册中...");
                this.assist.register(name,passwd,email,channel,hashMap);
            }

            //忘记密码
            else if("3".equals(line)) {
                System.out.println("请输入账号：");
                String name = scanner.nextLine();
                System.out.println("请输入邮箱：（仅限qq邮箱和163邮箱）");
                String email = scanner.nextLine();

                System.out.println("查询密码中...");
                this.assist.findPasswd(name,email,channel,hashMap);
            }

            //进入主菜单
            else if("4".equals(line)) {
                if(flag) {
                    InputMemu();
                }else {
                    System.out.println("请先登录！！！");
                }
            }

            //退出系统
            else {
                flag = false;
                System.exit(1);
            }
        }

    }


    /**
     * 主菜单界面处理
     */
    public void InputMemu() {
        while (true) {
            this.assist.showMainMemu();
            String line = scanner.nextLine();

            //修改密码
            if("1".equals(line)) {
                System.out.println("请输入原来的密码：");
                String oldPasswd = scanner.nextLine();
                System.out.println("请输入新密码：");
                String newPasswd = scanner.nextLine();

                System.out.println("修改中...");
                this.assist.updatePasswd(oldPasswd,newPasswd,channel);
            }

            //查看所有用户的信息
            else if("2".equals(line)) {
                System.out.println("请稍后...");
                System.out.println("数据准备中...");
                this.assist.selectAllUser(channel,hashMap.get("me"));
            }

            //单聊模式
            else if("3".equals(line)) {
                System.out.println("你要和谁聊天，请输入对方的名字：");
                String toName = scanner.nextLine();
                String forName = hashMap.get("me"); //获取自己的名字
                this.assist.oneChat(forName,toName,channel);
            }

            //群聊模式
            else if("4".equals(line)) {
                System.out.println("================群聊================");
                //进入群聊方法
                this.assist.groupChat(channel,hashMap.get("me"));
            }

            //发送文件
            else if("5".equals(line)) {
                System.out.println("请输入接收方名字：");
                String toName = scanner.nextLine();
                System.out.println("请输入文件路径");
                String path = scanner.nextLine();
                this.assist.sendFile(toName,path,channel);
            }

            //用户下线
            else if("6".equals(line)) {
                this.assist.quit(channel);
                System.out.println("===============你已下线================");
                flag = true;
                break;
            }

            //回到登录界面
            else {
                break;
            }
        }
    }

}
