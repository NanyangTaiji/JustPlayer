package com.nytaiji.nybase.utils;

import java.util.function.Function;

public class SingletonHolder<T, A> {

    private volatile T instance;
    private Function<A, T> creator;

    public SingletonHolder(Function<A, T> creator) {
        this.creator = creator;
    }

    public T getInstance(A arg) {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    T created = creator.apply(arg);
                    instance = created;
                    creator = null;
                    return created;
                }
            }
        }
        return instance;
    }
}

