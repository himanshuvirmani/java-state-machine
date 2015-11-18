package com.himanshuvirmani.sample;

import com.himanshuvirmani.StateMachine;
import com.himanshuvirmani.Transition;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by himanshu.virmani on 15/11/15.
 */
@Slf4j
public class Sample {

    public static void main(String[] args) {
        StateMachine<MySampleState, MySampleEvent, Object> stateMachine =
                new StateMachine<MySampleState, MySampleEvent, Object>(MySampleState.CREATED);

        stateMachine.transition().from(MySampleState.CREATED).to(MySampleState.ONHOLD).on(MySampleEvent.HOLD).setOnSuccessListener(onSuccessListener).create();
        stateMachine.transition().from(MySampleState.ONHOLD).to(MySampleState.DELIVERED).on(MySampleEvent.DELIVER).create();

        try {
            stateMachine.fire(MySampleEvent.HOLD);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static Transition.onSuccessListener<MySampleState, MySampleEvent> onSuccessListener = new Transition.onSuccessListener<MySampleState, MySampleEvent>() {
        @Override
        public void onSuccess(MySampleState from, MySampleState to, MySampleEvent on) {
            log.info("Log: transition success from " + from + " to " + to + " on " + on );
        }
    };
}
