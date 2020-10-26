package com.tulun.bean;

import java.sql.Timestamp;

/**
 * @ClassName Msg
 * @Description 存储消息表POJO
 * @Author lzq
 * @Date 2019/8/2 20:41
 * @Version 1.0
 **/
public class Msg {
    private int id;  //消息id
    private String forName; //发送者
    private String toName;  //接收者
    private String msg;  //消息体
    private String datetime;  //消息发送时间
    private int sign = 0;  //消息接收方是否接收  0 没有  1

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }
}
