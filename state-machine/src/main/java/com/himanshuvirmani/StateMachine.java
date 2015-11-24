package com.himanshuvirmani;

import com.himanshuvirmani.exceptions.TransitionCreationException;
import com.himanshuvirmani.exceptions.TransitionException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by himanshu.virmani on 13/11/15.
 */
@Slf4j
public class StateMachine<T, E> {

    private LinkedHashMap<E, Map<T, Transition<T, E>>> stateTransitions;

    private StateChangeListener<T, E> stateChangeListener;

    @Getter
    @Setter
    private T currentState;

    public StateMachine(T initialState) {
        currentState = initialState;
    }

    public void fire(E event) throws TransitionException {
        if (currentState == null)
            throw new TransitionException("current state cannot be null");
        if (stateTransitions == null)
            throw new TransitionException("No transitions defined for state machine");
        if (stateTransitions.get(event) == null)
            throw new TransitionException("No transitions defined for Event " + event);
        if (stateTransitions.get(event).get(currentState) == null)
            throw new TransitionException("No transitions defined from Current State " + currentState + " for Event " + event);

        Transition<T, E> transition = stateTransitions.get(event).get(currentState);

        log.info("Event accepted with State Transition " + transition);

        if (transition.getTo() == null || transition.isIgnore()) {
            log.info("Transition Ignored for From State "
                    + transition.getFrom() + " on " + transition.getOn());
            return;
        }

        currentState = transition.getTo();

        if (transition.getOnSuccessListener() != null)
            transition.getOnSuccessListener().onSuccess(transition.getFrom(), transition.getTo(), transition.getOn());

        if (stateChangeListener != null)
            stateChangeListener.onStateChanged(transition.getFrom(), transition.getTo(), transition.getOn());
    }

    public void fire(E event, T currentState) throws TransitionException {
        this.currentState = currentState;
        fire(event);
    }

    public Transition.TransitionBuilder<T, E> transition() {
        return new Transition.TransitionBuilder<T, E>(this);
    }

    public Transitions.TransitionsBuilder<T, E> transitions() {
        return new Transitions.TransitionsBuilder<T, E>(this);
    }

    public void apply(Transition<T, E> tseTransition) {

        validateTransition(tseTransition);

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

    public void apply(Transitions<T, E> tseTransitions) {

        validateAndApplyTransitions(tseTransitions);

    }

    private void validateAndApplyTransitions(Transitions<T, E> tseTransitions) throws TransitionCreationException {

        if (tseTransitions.getFromAny() == null)
            throw new TransitionCreationException("From states should be defined while creating a transition");
        if (tseTransitions.getOnEach() == null)
            throw new TransitionCreationException("On Events should be defined while creating a transition");
        if (!tseTransitions.isIgnore() && tseTransitions.getToAmong() == null)
            throw new TransitionCreationException("A transition while its creation should either have \"ignore\" or \"To States\"");

        final int fromAnyLength = tseTransitions.getFromAny().length;
        final int onEachLength = tseTransitions.getOnEach().length;
        int toAmongLength = 0;

        if (tseTransitions.getToAmong() != null) toAmongLength = tseTransitions.getToAmong().length;

        if (fromAnyLength == 1) {
            if (toAmongLength != 0 && toAmongLength != onEachLength) {
                throw new TransitionCreationException("Number of states in \"From Any\" or \"To Among\" should be equal " +
                        "to events provided in \"OnEach\" to avoid ambiguity");
            }
            for (int i = 0; i < onEachLength; i++) {
                if (toAmongLength == 0)
                    new Transition.TransitionBuilder<T, E>(this).from(tseTransitions.getFromAny()[0]).on(tseTransitions.getOnEach()[i]).
                            ignore().setOnSuccessListener(tseTransitions.getOnSuccessListener()).create();
                else
                    new Transition.TransitionBuilder<T, E>(this).from(tseTransitions.getFromAny()[0]).on(tseTransitions.getOnEach()[i]).
                            to(tseTransitions.getToAmong()[i]).setOnSuccessListener(tseTransitions.getOnSuccessListener()).create();
            }
            return;
        }

        if (toAmongLength == 1) {
            if (fromAnyLength != onEachLength) {
                throw new TransitionCreationException("Number of states in \"From Any\" or \"To Among\" should be equal " +
                        "to events provided in \"OnEach\" to avoid ambiguity");
            }
            for (int i = 0; i < onEachLength; i++) {
                new Transition.TransitionBuilder<T, E>(this).from(tseTransitions.getFromAny()[i]).on(tseTransitions.getOnEach()[i]).
                        to(tseTransitions.getToAmong()[0]).setOnSuccessListener(tseTransitions.getOnSuccessListener()).create();
            }
            return;
        }

        if (onEachLength == 1) {
            if (toAmongLength != 0 && fromAnyLength != toAmongLength) {
                throw new TransitionCreationException("Number of states in \"From Any\" should be equal to \"To Among\" for a " +
                        "single event to avoid ambiguity");
            }
            for (int i = 0; i < fromAnyLength; i++) {
                if (toAmongLength == 0)
                    new Transition.TransitionBuilder<T, E>(this).from(tseTransitions.getFromAny()[i]).on(tseTransitions.getOnEach()[0]).
                            ignore().setOnSuccessListener(tseTransitions.getOnSuccessListener()).create();
                else
                    new Transition.TransitionBuilder<T, E>(this).from(tseTransitions.getFromAny()[i]).on(tseTransitions.getOnEach()[0]).
                            to(tseTransitions.getToAmong()[i]).setOnSuccessListener(tseTransitions.getOnSuccessListener()).create();
            }
            return;
        }

        if ((onEachLength == fromAnyLength) && (fromAnyLength == toAmongLength)) {
            for (int i = 0; i < fromAnyLength; i++) {
                new Transition.TransitionBuilder<T, E>(this).from(tseTransitions.getFromAny()[i]).on(tseTransitions.getOnEach()[i]).
                        to(tseTransitions.getToAmong()[i]).setOnSuccessListener(tseTransitions.getOnSuccessListener()).create();
            }
            return;
        } else if ((onEachLength == fromAnyLength) && toAmongLength == 0) {
            for (int i = 0; i < fromAnyLength; i++) {
                new Transition.TransitionBuilder<T, E>(this).from(tseTransitions.getFromAny()[i]).on(tseTransitions.getOnEach()[i]).
                        ignore().setOnSuccessListener(tseTransitions.getOnSuccessListener()).create();
            }
            return;
        } else {
            throw new TransitionCreationException("Ambiguous Transitions Creation");
        }
    }

    private void validateTransition(Transition<T, E> tseTransition) throws TransitionCreationException {
        if (tseTransition.getFrom() == null) throw new TransitionCreationException("From state should be defined");
        if (tseTransition.getOn() == null) throw new TransitionCreationException("On Event should be defined");
        if (!tseTransition.isIgnore() && tseTransition.getTo() == null)
            throw new TransitionCreationException("A transition while its creation should either have \"ignore\" or \"To State\"");
    }

    public interface StateChangeListener<T, E> {
        void onStateChanged(T from, T to, E on);
    }

    public void setStateChangeListener(StateChangeListener<T, E> stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
    }

}
