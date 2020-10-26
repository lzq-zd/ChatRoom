package com.tulun.cantant;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName ID
 * @Description
 * @Author lzq
 * @Date 2019/7/28 11:42
 * @Version 1.0
 **/
public class ID {
    private static AtomicInteger id = new AtomicInteger(1);


    public static AtomicInteger getId() {
        return id;
    }
}
