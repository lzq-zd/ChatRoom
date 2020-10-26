package com.tulun.controller;


import com.tulun.cantant.EnMsgType;
import com.tulun.netty.ChannelHandler;
import io.netty.channel.Channel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName MonitorChannel
 * @Description 监视通道是否正常关闭
 * @Author lzq
 * @Date 2019/8/3 21:02
 * @Version 1.0
 **/
public class MonitorChannel extends Thread{
    private Assist assist;
    private SimpleDateFormat sdf;
    //存储 通道 —— 是否正常下线
    private ConcurrentHashMap<Channel,Boolean> concurrentHashMap;

    //存储 通道——账户信息
    private ConcurrentHashMap<Channel,String> concurrentHashMap1;
    //存储 用户信息——通过
    private ConcurrentHashMap<String,Channel> concurrentHashMap2;

    public MonitorChannel(ConcurrentHashMap<Channel, Boolean> concurrentHashMap,
                          ConcurrentHashMap<Channel, String> concurrentHashMap1,
                          ConcurrentHashMap<String, Channel> concurrentHashMap2,
                          Assist assist, SimpleDateFormat sdf) {
        this.assist = assist;
        this.sdf = sdf;
        this.concurrentHashMap = concurrentHashMap;
        this.concurrentHashMap1 = concurrentHashMap1;
        this.concurrentHashMap2 = concurrentHashMap2;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Channel channel = ChannelHandler.queue.take();
                if(!concurrentHashMap.containsKey(channel)) {
                    //用户只是连接上了，并未成功登录便退出，
                    continue;
                }
                if(concurrentHashMap.get(channel)) {  //通道时客户端请求关闭的
                    concurrentHashMap.remove(channel);
                    continue;
                }
                this.assist.sendAll(concurrentHashMap2,concurrentHashMap1.get(channel),"异常下线",sdf.format(new Date()),EnMsgType.EN_MSG_NOTIFY_OFFLINE);
                concurrentHashMap.remove(channel);
                System.out.println("<"+concurrentHashMap1.get(channel)+"> 异常下线");
                concurrentHashMap2.remove(concurrentHashMap1.get(channel));
                concurrentHashMap1.remove(channel);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
