package com.sxtreh.context;


public class BaseContext {

    public static ThreadLocal<Long> threadLocalId = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocalId.set(id);
    }

    public static Long getCurrentId() {
        return threadLocalId.get();
    }

    public static void removeCurrentId() {
        threadLocalId.remove();
    }

}