package com.himanshuvirmani;

import lombok.Data;

import java.lang.ref.WeakReference;

/**
 * Created by himanshu.virmani on 15/11/15.
 */
@Data
public class Transitions<T, E> {

    private T[] fromAny;

    private T[] toAmong;

    private E[] onEach;

    private boolean ignore;

    private Transition.onSuccessListener<T, E> onSuccessListener;

    public Transitions(TransitionsBuilder<T, E> tseTransitionBuilder) {
        this.fromAny = tseTransitionBuilder.fromAny;
        this.toAmong = tseTransitionBuilder.toAmong;
        this.onEach = tseTransitionBuilder.onEach;
        this.onSuccessListener = tseTransitionBuilder.onSuccessListener;
        this.ignore = tseTransitionBuilder.ignore;
    }


    public static class TransitionsBuilder<U, V> {

        private WeakReference<StateMachine<U, V>> stateMachineWeakReference;

        private U[] fromAny;

        private U[] toAmong;

        private V[] onEach;

        private boolean ignore;

        private Transition.onSuccessListener<U, V> onSuccessListener;

        public TransitionsBuilder(StateMachine<U, V> stateMachine) {
            stateMachineWeakReference = new WeakReference<StateMachine<U, V>>(stateMachine);
        }

        public TransitionsBuilder<U, V> fromAny(U... fromStates) {
            fromAny = fromStates;
            return this;
        }

        public TransitionsBuilder<U, V> toAmong(U... toStates) {
            toAmong = toStates;
            return this;
        }

        public TransitionsBuilder<U, V> onEach(V... onEvents) {
            onEach = onEvents;
            return this;
        }

        public TransitionsBuilder<U, V> ignore() {
            ignore=  true;
            return this;
        }

        public TransitionsBuilder<U, V> setOnSuccessListener(Transition.onSuccessListener<U, V> onSuccessListener) {
            this.onSuccessListener = onSuccessListener;
            return this;
        }

        public StateMachine<U, V> create() {
            final StateMachine<U, V> stateMachine = stateMachineWeakReference.get();
            if(stateMachine != null) {
                stateMachine.apply(new Transitions<U, V>(this));
                return stateMachine;
            }
            return null;
        }
    }

}
