package com.tulun.service;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @ClassName SendFile
 * @Description 发送文件线程
 * @Author lzq
 * @Date 2019/8/10 11:58
 * @Version 1.0
 **/
public class SendFile extends Thread {
    private int port;  //端口号
    private File file;  //发送的文件
    private String time;  //发送时间
    private String ip;  //服务端IP

    public SendFile(int port, File file,String time,String ip) {
        this.port = port;
        this.file = file;
        this.time = time;
        this.ip = ip;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip,port));
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String fileHead = file.getName()+","+time;
            dataOutputStream.writeUTF(fileHead);

            FileInputStream dataInputStream = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int i = 0;
            while ((i = dataInputStream.read(bytes)) != -1) {
                dataOutputStream.write(bytes,0,i);
            }

            dataOutputStream.flush();
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
            System.out.println("文件<"+file.getName()+">传输完成!");
        } catch (IOException e) {
            System.out.println("文件解析错误！！！");
        }
    }
}
