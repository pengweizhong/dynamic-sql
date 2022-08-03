package com.pengwz.dynamic.model;

/**
 * 结束对象
 *
 * @param <S> 任何类型
 */
public class End<S> {
    private final S s;

    public End(S s) {
        this.s = s;
    }

    public S end() {
        return s;
    }
}

