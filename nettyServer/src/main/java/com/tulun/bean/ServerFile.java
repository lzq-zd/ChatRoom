package com.tulun.bean;

/**
 * @ClassName ServerFile
 * @Description 存储未发送文件
 * @Author lzq
 * @Date 2019/8/10 14:50
 * @Version 1.0
 **/
public class ServerFile {
    private int id;
    private String forName;
    private String toName;
    private String path;
    private String time;
    private int sign;
    private String fileName;

    public String getForName() {
        return forName;
    }

    public void setForName(String forName) {
        this.forName = forName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
