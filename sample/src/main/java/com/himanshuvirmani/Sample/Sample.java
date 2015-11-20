package com.himanshuvirmani.sample;

import com.himanshuvirmani.StateMachine;
import com.himanshuvirmani.Transition;
import com.himanshuvirmani.exceptions.TransitionException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by himanshu.virmani on 15/11/15.
 */
@Slf4j
public class Sample implements StateMachine.StateChangeListener<MySampleState, MySampleEvent>{

    public static void main(String[] args) {

        StateMachine<MySampleState, MySampleEvent> stateMachine =
                new StateMachine<MySampleState, MySampleEvent>(MySampleState.CREATED);

        stateMachine.setStateChangeListener(new StateMachine.StateChangeListener<MySampleState, MySampleEvent>() {
            @Override
            public void onStateChanged(MySampleState from, MySampleState to, MySampleEvent on) {
                log.info("State changed from " + from + " to " + to + " on " + on);
            }
        });

        stateMachine.transition().from(MySampleState.CREATED).to(MySampleState.ONHOLD).on(MySampleEvent.HOLD).setOnSuccessListener(onSuccessListener).create();
        stateMachine.transition().from(MySampleState.ONHOLD).to(MySampleState.DELIVERED).on(MySampleEvent.DELIVER).create();
        stateMachine.transition().from(MySampleState.DELIVERED).on(MySampleEvent.CANCEL).ignore().create();

        try {
            stateMachine.fire(MySampleEvent.HOLD);
        } catch (TransitionException e) {
            e.printStackTrace();
        }

    }

    static Transition.onSuccessListener<MySampleState, MySampleEvent> onSuccessListener = new Transition.onSuccessListener<MySampleState, MySampleEvent>() {
        @Override
        public void onSuccess(MySampleState from, MySampleState to, MySampleEvent on) {
            log.info("Transition success from " + from + " to " + to + " on " + on);
        }
    };

    @Override
    public void onStateChanged(MySampleState from, MySampleState to, MySampleEvent on) {
        log.info("State changed from " + from + " to " + to + " on " + on);
    }
}
