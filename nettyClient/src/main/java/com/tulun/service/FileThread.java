package com.tulun.service;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @ClassName FileThread
 * @Description 接收文件线程
 * @Author lzq
 * @Date 2019/8/9 18:51
 * @Version 1.0
 **/
public class FileThread extends Thread{
    private int port;
    private String ip;
    private String forName;

    public FileThread(int port,String ip,String forName) {
        this.port = port;
        this.ip = ip;
        this.forName = forName;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip,port));
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String fileHead = dataInputStream.readUTF();
            String[] fileHeadMsg = fileHead.split(",");
            String fileName = fileHeadMsg[0];  //文件名
            String fileTime = fileHeadMsg[1];  //时间

            String path = "F:\\neety\\clientFile\\"+fileName;
            FileOutputStream outputStream = new FileOutputStream(path);
            byte[] bytes = new byte[1024];
            int i = 0;
            while ((i = dataInputStream.read(bytes)) != -1) {
                outputStream.write(bytes,0,i);
            }

            outputStream.close();
            dataInputStream.close();
            outputStream.close();
            socket.close();
            System.out.println("接收到来自["+forName+"]在["+fileTime+"]发送的文件<"+fileName+">");
        } catch (IOException e) {
            System.out.println("文件解析错误！！！");
        }
    }
}
