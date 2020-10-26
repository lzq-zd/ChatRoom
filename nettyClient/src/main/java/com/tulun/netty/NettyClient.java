package com.tulun.netty;

import com.tulun.service.SendService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @ClassName NettyClient
 * @Description 客户端
 * @Author lzq
 * @Date 2019/7/27 23:13
 * @Version 1.0
 **/
public class NettyClient {
    public void init(String ip, int port) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new ClientHandler());
                    }
                });

        try {
            Channel channel = bootstrap.connect(ip,port).sync().channel();
            System.out.println("已连接服务器...");
            //通过channel发送消息给服务器
            SendService sendService = new SendService(channel);
            sendService.sendMsg();

            //管理
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            System.out.println("服务器已关闭...");
        } finally {
            group.shutdownGracefully();
        }
    }
}
