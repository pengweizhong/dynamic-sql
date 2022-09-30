package com.pengwz.dynamic.model;

/**
 * 结束对象
 *
 * @param <S> 任何类型
 */
public class End<S> {
    private final S s;
    private End<S> registerEnd;

    public End(S s) {
        this.s = s;
    }

    public S end() {
        if (registerEnd == null) {
            return s;
        }
        return registerEnd.end();
    }

    /**
     * 将继承的子类注入到本类中，此后调用结束函数时，将会调用到子类的方法。
     * 仅仅允许子类调用
     */
    protected void register(End<S> end) {
        this.registerEnd = end;
    }

}

