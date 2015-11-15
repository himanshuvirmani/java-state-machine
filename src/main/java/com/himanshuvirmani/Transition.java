package com.himanshuvirmani;

import lombok.Data;

import java.lang.ref.WeakReference;

/**
 * Created by himanshu.virmani on 15/11/15.
 */
@Data
public class Transition<T, E> {

    private T from;

    private T to;

    private E on;

    public Transition(TransitionBuilder<T, E> tseTransitionBuilder) {
        this.from = tseTransitionBuilder.from;
        this.to = tseTransitionBuilder.to;
        this.on = tseTransitionBuilder.on;
    }

    public static class TransitionBuilder<U, V> {

        private WeakReference<StateMachine> stateMachineWeakReference;

        private U from;

        private U to;

        private V on;

        public TransitionBuilder(StateMachine stateMachine) {
            stateMachineWeakReference = new WeakReference<StateMachine>(stateMachine);
        }

        public TransitionBuilder<U, V> from(U fromState) {
            from = fromState;
            return this;
        }

        public TransitionBuilder<U, V> to(U toState) {
            to = toState;
            return this;
        }

        public TransitionBuilder<U, V> on(V onEvent) {
            on = onEvent;
            return this;
        }

        public StateMachine create() {
            stateMachineWeakReference.get().apply(new Transition<U, V>(this));
            return stateMachineWeakReference.get();
        }
    }

}
