package com.himanshuvirmani;

import com.himanshuvirmani.exceptions.TransitionCreationException;
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

    private boolean ignore;

    private Condition condition;

    private onSuccessListener<T, E> onSuccessListener;

    public Transition(TransitionBuilder<T, E> tseTransitionBuilder) {
        this.from = tseTransitionBuilder.from;
        this.to = tseTransitionBuilder.to;
        this.on = tseTransitionBuilder.on;
        this.onSuccessListener = tseTransitionBuilder.onSuccessListener;
        this.ignore = tseTransitionBuilder.ignore;
        this.condition =  tseTransitionBuilder.condition;
    }

    public interface onSuccessListener<T, E> {
        void onSuccess(T from, T to, E on);
    }

    public static class TransitionBuilder<U, V> {

        private WeakReference<StateMachine<U, V>> stateMachineWeakReference;

        private U from;

        private U to;

        private V on;

        private boolean ignore;

        private Condition condition;

        private onSuccessListener<U, V> onSuccessListener;

        public TransitionBuilder(StateMachine<U, V> stateMachine) {
            stateMachineWeakReference = new WeakReference<StateMachine<U, V>>(stateMachine);
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

        public TransitionBuilder<U, V> ignore() {
            ignore=  true;
            return this;
        }

        public TransitionBuilder<U, V> when(Condition condition) {
            this.condition = condition;
            return this;
        }

        public TransitionBuilder<U, V> setOnSuccessListener(onSuccessListener<U, V> onSuccessListener) {
            this.onSuccessListener = onSuccessListener;
            return this;
        }

        public StateMachine<U, V> create() throws TransitionCreationException {
            final StateMachine<U, V> stateMachine = stateMachineWeakReference.get();
            if(stateMachine != null) {
                stateMachine.apply(new Transition<U, V>(this));
                return stateMachine;
            }
            return null;
        }
    }

}
