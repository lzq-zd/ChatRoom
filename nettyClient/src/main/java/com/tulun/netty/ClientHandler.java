package com.tulun.netty;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tulun.controller.EnMsgType;
import com.tulun.service.FileThread;
import com.tulun.util.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;;

import java.net.InetSocketAddress;
import java.util.concurrent.SynchronousQueue;

/**
 * @ClassName ClientHandler
 * @Description 客户端的业务处理类
 * @Author lzq
 * @Date 2019/7/27 23:21
 * @Version 1.0
 **/
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    //同步阻塞队列，将服务端返回给工作线程的数据传输给主线程处理
    public static SynchronousQueue<Integer> queue = new SynchronousQueue <>();

    //存储的是消息，密码等之类的
    public static SynchronousQueue<String> content = new SynchronousQueue<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //解析服务端返回数据
        String recvMsg = (String)msg;
        ObjectNode jsonNodes = JsonUtils.getObjectNode(recvMsg);
        String type = jsonNodes.get("type").asText();
        if (String.valueOf(EnMsgType.EN_MSG_ACK).equals(type)) {
            //ack消息,得到它是那种消息的返回结果
            String srctype = jsonNodes.get("srctype").asText();

            //登录操作
            if (String.valueOf(EnMsgType.EN_MSG_LOGIN).equals(srctype)) {
                //登录操作的ack消息
                int code = jsonNodes.get("code").asInt();
                //将服务端的返回交给主线程
                queue.put(code);
            }

            //注册操作
            else if(String.valueOf(EnMsgType.EN_MSG_REGISTER).equals(srctype)) {
                int code = jsonNodes.get("code").asInt();
                queue.put(code);
            }

            //忘记密码操作
            else if(String.valueOf(EnMsgType.EN_MSG_FORGET_PWD).equals(srctype)) {
                int code = jsonNodes.get("code").asInt();
                queue.put(code);
            }

            //验证码操作
            else if(String.valueOf(EnMsgType.EN_MSG_YZM).equals(srctype)) {
                int code = jsonNodes.get("code").asInt();
                queue.put(code);
            }

            //修改密码操作
            else if(String.valueOf(EnMsgType.EN_MSG_MODIFY_PWD).equals(srctype)) {
                int code = jsonNodes.get("code").asInt();
                queue.put(code);
            }

            //获取所有用户列表操作
            else if(String.valueOf(EnMsgType.EN_MSG_GET_ALL_USERS).equals(srctype)) {
                int code = jsonNodes.get("code").asInt();
                String s = jsonNodes.get("content").asText();
                queue.put(code);
                content.put(s);
            }

            //一对一聊天
            else if(String.valueOf(EnMsgType.EN_MSG_CHAT).equals(srctype)) {
                String forName = jsonNodes.get("forName").asText();
                String getMsg = jsonNodes.get("msg").asText();
                String time = jsonNodes.get("time").asText();
                System.out.println("==============="+forName+"================");
                System.out.println(getMsg);
                System.out.println("========"+time+"========");
            }

            //群聊
            else if(String.valueOf(EnMsgType.EN_MSG_CHAT_ALL).equals(srctype)) {
                String forName = jsonNodes.get("forName").asText();
                String getMsg = jsonNodes.get("msg").asText();
                String time = jsonNodes.get("time").asText();
                System.out.println("=========================群聊=========================");
                System.out.println("["+forName+"]\t\t\t\t"+getMsg+"\t\t\t\t["+time+"]");
            }

            //用户下线
            else if(String.valueOf(EnMsgType.EN_MSG_NOTIFY_OFFLINE).equals(srctype)) {
                String forName = jsonNodes.get("forName").asText();
                String getMsg = jsonNodes.get("msg").asText();
                String time = jsonNodes.get("time").asText();
                System.out.println("<"+forName+">\t\t"+getMsg+"\t\t["+time+"]");
            }

            //用户上线
            else if(String.valueOf(EnMsgType. EN_MSG_NOTIFY_ONLINE).equals(srctype)) {
                String forName = jsonNodes.get("forName").asText();
                String getMsg = jsonNodes.get("msg").asText();
                String time = jsonNodes.get("time").asText();
                System.out.println("<"+forName+">\t\t\t\t"+getMsg+"\t\t\t\t["+time+"]");
            }

            //发送文件  接收端口号 启动子线程
            else if(String.valueOf(EnMsgType.EN_MSG_SEND_FILE).equals(srctype)) {
                int port = jsonNodes.get("port").asInt();
                queue.put(port);
            }

            //文件传输 接收文件
            else if(String.valueOf(EnMsgType.EN_MSG_TRANSFER_FILE).equals(srctype)) {
                if(jsonNodes.get("code") != null) {
                    System.out.println("失败");
                    int code = jsonNodes.get("code").asInt();
                    queue.put(code);
                }else {
                    int port = jsonNodes.get("port").asInt();
                    String forName = jsonNodes.get("forName").asText();
                    InetSocketAddress insocket = (InetSocketAddress)ctx.channel().remoteAddress();
                    String ip = insocket.getAddress().getHostAddress();
                    new FileThread(port,ip,forName).start();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("服务端异常关闭");
    }
}
