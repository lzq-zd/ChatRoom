package com.tulun.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @ClassName NettyServer
 * @Description 启动服务端
 * @Author lzq
 * @Date 2019/7/27 08:58
 * @Version 1.0
 **/
public class NettyServer {
    public void init(int port) {
        //创建时间循环组
        NioEventLoopGroup boss = new NioEventLoopGroup(1);  //负责处理连接
        NioEventLoopGroup worker = new NioEventLoopGroup(5); //负责处理读写

        //辅助
        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new ChannelHandler());
            }
        });

        Channel channel = bootstrap.bind(port).channel();
        System.out.println("服务端已启动...");

        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            System.out.println("客户端断开连接...");
        } finally {
            if(channel != null) {
                try {
                    channel.closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            System.out.println("服务器关闭...");
        }
    }
}
