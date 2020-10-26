package com.tulun.controller;

import com.tulun.util.PortUtils;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName SendStorageFile
 * @Description 发送未接收的文件
 * @Author lzq
 * @Date 2019/8/10 15:38
 * @Version 1.0
 **/
public class SendStorageFile extends Thread{
    private String forName;
    private String path;
    private int port;
    private String time;
    private String fileName;

    public SendStorageFile(String forName, String path, int port, String time,String fileName) {
        this.forName = forName;
        this.path = path;
        this.port = port;
        this.time = time;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
            Socket socket = serverSocket.accept();
            InputStream inputStream = new FileInputStream(new File(path));
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String fileHead = fileName+","+time;
            dataOutputStream.writeUTF(fileHead);

            byte[] bytes = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(bytes)) != -1) {
                dataOutputStream.write(bytes,0,i);
            }
            dataOutputStream.flush();
            dataOutputStream.close();
            inputStream.close();
            socket.close();
            serverSocket.close();
            PortUtils.closePort(port);
            System.out.println("成功发送文件["+fileName+"]");
        } catch (IOException e) {
            System.out.println("文件解析错误！！！");
        }
    }
}
