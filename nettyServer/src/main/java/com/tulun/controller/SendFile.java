package com.tulun.controller;

import com.tulun.util.PortUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName SendFile
 * @Description 转发文件的线程
 * @Author lzq
 * @Date 2019/8/10 11:42
 * @Version 1.0
 **/
public class SendFile extends Thread {
    private int forPort;
    private int toPort;

    public SendFile(int forPort, int toPort) {
        this.forPort = forPort;
        this.toPort = toPort;
    }

    @Override
    public void run() {
        try {
            ServerSocket forServerSocker = new ServerSocket();
            ServerSocket toServerSocker = new ServerSocket();
            forServerSocker.bind(new InetSocketAddress(forPort));
            toServerSocker.bind(new InetSocketAddress(toPort));

            Socket forSocket = forServerSocker.accept();
            Socket toSocekt = toServerSocker.accept();

            OutputStream outputStream = toSocekt.getOutputStream();
            InputStream inputStream = forSocket.getInputStream();

            byte[] bytes = new byte[1024];
            int i  = 0;
            while ((i = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes,0,i);
            }

            outputStream.flush();
            //关闭资源
            inputStream.close();
            outputStream.close();
            forSocket.close();
            toSocekt.close();
            forServerSocker.close();
            toServerSocker.close();
            //端口管理
            PortUtils.closePort(forPort);
            PortUtils.closePort(toPort);
        } catch (IOException e) {
            System.out.println("文件解析错误！！！");
        }
    }
}
