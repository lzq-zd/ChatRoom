package com.tulun;

import com.tulun.netty.NettyServer;

/**
 * 启动类   记着一定要启动redis
 */
public class App {
    public static void main( String[] args ) {
        NettyServer nettyServer = new NettyServer();
        nettyServer.init(6666);
    }
}
