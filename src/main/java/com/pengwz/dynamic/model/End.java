package com.pengwz.dynamic.model;

/**
 * 结束对象。常用于断句环境，然后返回预定的入参。
 *
 * @param <S> 任何类型
 */
public abstract class End<S> {
    //返回参数类型
    private final S s;
    //子类实现者，此处可以从众多子类中探知到具体的子类，而不需要从子类处向上查询
    private End<S> registerEnd;

    protected End(S s) {
        this.s = s;
    }

    /**
     * 结束语句，然后返回预定的参数，若未注入子类，则直接返回
     *
     * @return 预定的对象
     * @see this#register(End)
     */
    public S end() {
        if (registerEnd == null) {
            return get();
        }
        return registerEnd.doEnd().get();
    }

    /**
     * 在方法结束前做的一些事情
     *
     * @return 处理逻辑本身
     */
    protected abstract End<S> doEnd();

    /**
     * 将子类处理完的结果继续向上返回
     *
     * @return 获取处理结果
     */
    protected S get() {
        return s;
    }

    /**
     * 将继承的子类注入到本类中，此后调用结束函数时，将会调用到子类的方法。
     * 仅仅允许子类调用
     */
    protected void register(End<S> end) {
        this.registerEnd = end;
    }

    /**
     * end断句默认实现类，这里其实什么也没有做
     *
     * @param <S> 最终结果
     */
    public static class DefaultEnd<S> extends End<S> {

        public DefaultEnd(S s) {
            super(s);
        }

        @Override
        protected End<S> doEnd() {
            //nothing ~
            return this;
        }

        @Override
        protected void register(End<S> end) {
            super.register(null);
        }
    }
}

