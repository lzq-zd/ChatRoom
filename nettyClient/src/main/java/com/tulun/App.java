package com.tulun;

import com.tulun.netty.NettyClient;

/**
 * 客户端
 */
public class App {
    public static void main( String[] args ){
        NettyClient nettyClient = new NettyClient();
        nettyClient.init("127.0.0.1", 6666);
    }
}
