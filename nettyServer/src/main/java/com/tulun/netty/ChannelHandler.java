package com.tulun.netty;

import com.tulun.controller.Transfer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName ChannelHandler
 * @Description 服务端逻辑处理
 * @Author lzq
 * @Date 2019/7/27 08:55
 * @Version 1.0
 **/

public class ChannelHandler extends SimpleChannelInboundHandler<String> {
    private static Transfer transfer = new Transfer();
    public static LinkedBlockingQueue<Channel> queue = new LinkedBlockingQueue<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

    }

    /**
     * 读取数据
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String recv = transfer.process((String)msg,ctx.channel()); //解析消息，拿到返回的内容
        ctx.channel().writeAndFlush(recv);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        queue.put(ctx.channel());
    }
}

