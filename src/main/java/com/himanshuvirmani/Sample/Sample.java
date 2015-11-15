package com.himanshuvirmani.sample;

import com.himanshuvirmani.StateMachine;

/**
 * Created by himanshu.virmani on 15/11/15.
 */
public class Sample {

    public static void main(String[] args) {
        StateMachine<MySampleState, MySampleEvent, Object> stateMachine =
                new StateMachine<MySampleState, MySampleEvent, Object>(MySampleState.CREATED);

        stateMachine.addTransition().from(MySampleState.CREATED).to(MySampleState.ONHOLD).on(MySampleEvent.HOLD).create();
        stateMachine.addTransition().from(MySampleState.ONHOLD).to(MySampleState.DELIVERED).on(MySampleEvent.DELIVER).create();

        try {
            stateMachine.fire(MySampleEvent.DELIVER);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
