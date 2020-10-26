package com.tulun.util;

import java.io.*;

/**
 * @ClassName SerializeUtil
 * @Description 序列号和反序列化
 * @Author lzq
 * @Date 2019/7/31 01:15
 * @Version 1.0
 **/
public class SerializeUtil {
    // 序列化
    public static byte[] serialize(Object object) {

        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        byte[] bytes = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            bytes = baos.toByteArray();
        } catch (Exception e) {
            System.err.println("序列化失败" + e.getMessage());
        }
        return bytes;
    }

    // 反序列化
    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
        } catch (Exception e) {
            System.err.println("反序列化失败" + e.getMessage());
        }
        return ois.readObject();
    }
}
