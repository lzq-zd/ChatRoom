package com.tulun.bean;

import java.io.Serializable;

/**
 * @ClassName User
 * @Description 用户类
 * @Author lzq
 * @Date 2019/7/27 23:44
 * @Version 1.0
 **/
public class User implements Serializable {
    private int Uid;
    private String Uname;
    private String Upasswd;
    private String Uemail;

    public int getUid() {
        return Uid;
    }

    public void setUid(int uid) {
        Uid = uid;
    }

    public String getUname() {
        return Uname;
    }

    public void setUname(String uname) {
        Uname = uname;
    }

    public String getUpasswd() {
        return Upasswd;
    }

    public void setUpasswd(String upasswd) {
        Upasswd = upasswd;
    }

    public String getUemail() {
        return Uemail;
    }

    public void setUemail(String uemail) {
        Uemail = uemail;
    }

    @Override
    public String toString() {
        return "[姓名：" + Uname +" 邮箱：" + Uemail + "]";
    }
}
