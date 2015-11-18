package com.himanshuvirmani;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by himanshu.virmani on 13/11/15.
 */
@Slf4j
public class StateMachine<T, E, V> {

    private LinkedHashMap<E, Map<T, Transition<T, E>>> stateTransitions;

    private T currentState;

    private StateMachine() {

    }

    public StateMachine(T initialState) {
        currentState = initialState;
    }

    public void fire(E event) throws Exception {
        if (stateTransitions == null) throw new Exception("No transitions defined for state machine");
        if (stateTransitions.get(event) == null) throw new Exception("No transitions defined for Event " + event);
        if (stateTransitions.get(event).get(currentState) == null)
            throw new Exception("No transitions defined from Current State " + currentState + " for Event " + event);

        Transition<T, E> transition = stateTransitions.get(event).get(currentState);
        log.info("Event accepted with State Transition " + transition);
        currentState = transition.getTo();
        if (transition.getOnSuccessListener() != null)
            transition.getOnSuccessListener().onSuccess(transition.getFrom(), transition.getTo(), transition.getOn());
    }

    public Transition.TransitionBuilder<T, E> transition() {
        return new Transition.TransitionBuilder<T, E>(this);
    }

    public void apply(Transition<T, E> tseTransition) {

        if (stateTransitions == null) {
            stateTransitions = new LinkedHashMap<E, Map<T, Transition<T, E>>>();
        }

        Map<T, Transition<T, E>> transitions = stateTransitions.get(tseTransition.getOn());

        if (transitions == null) {
            transitions = new HashMap<T, Transition<T, E>>();
        }

        transitions.put(tseTransition.getFrom(), tseTransition);
        stateTransitions.put(tseTransition.getOn(), transitions);
    }

}
