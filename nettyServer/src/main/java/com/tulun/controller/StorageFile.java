package com.tulun.controller;

import com.tulun.util.PortUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName storageFile
 * @Description 接收文件
 * @Author lzq
 * @Date 2019/8/10 15:10
 * @Version 1.0
 **/
public class StorageFile extends Thread{
    private String forName;
    private String toName;
    private int port;
    private Assist assist;

    public StorageFile(String forName, String toName, int port, Assist assist) {
        this.forName = forName;
        this.port = port;
        this.toName = toName;
        this.assist = assist;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(this.port));
            Socket socket = serverSocket.accept();

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String file = dataInputStream.readUTF();
            String[] fileHead = file.split(",");
            String fileName = fileHead[0];  //拿到文件名
            String time = fileHead[1];  //时间

            String path = "F:\\neety\\server\\"+fileName; //存储路径
            OutputStream outputStream = new FileOutputStream(new File(path));
            byte[] bytes = new byte[1024];
            int i = 0;

            while ((i = dataInputStream.read(bytes)) != -1) {
                outputStream.write(bytes,0,i);
            }
            outputStream.flush();
            outputStream.close();
            dataInputStream.close();
            socket.close();
            serverSocket.close();
            PortUtils.closePort(port);
            this.assist.storageFile(forName,toName,path,time,fileName);  //记录到数据库中
        } catch (IOException e) {
            System.out.println("文件解析错误！！！");
        }
    }
}
