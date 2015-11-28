package com.himanshuvirmani;

import com.himanshuvirmani.exceptions.TransitionCreationException;
import com.himanshuvirmani.exceptions.TransitionException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by himanshu.virmani on 13/11/15.
 */
@Slf4j
@NoArgsConstructor
public class StateMachine<T, E> {

    private LinkedHashMap<E, Map<T, Transition<T, E>>> stateTransitions;

    @Getter
    @Setter
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

        if (transition.getCondition() != null && !transition.getCondition().isMet()) {
            log.info("Condition not met. Ignoring transition");
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

        if (tseTransitions.getToAmong() != null && !tseTransitions.isIgnore())
            toAmongLength = tseTransitions.getToAmong().length;

        if (!isAllTransitionLengthsValid(fromAnyLength, onEachLength, toAmongLength)) {
            throw new TransitionCreationException("Ambiguous Transitions Creation. " +
                    "State machine is not able to comprehend the transitions applied. " +
                    "HINT: Check out lengths of from state, to state and events");
        }

        final int length = getMax(fromAnyLength, onEachLength, toAmongLength);

        for (int i = 0; i < length; i++) {

            T fromState = i < fromAnyLength ? tseTransitions.getFromAny()[i] : tseTransitions.getFromAny()[fromAnyLength - 1];

            E onEvent = i < onEachLength ? tseTransitions.getOnEach()[i] : tseTransitions.getOnEach()[onEachLength - 1];

            if (toAmongLength == 0) {
                new Transition.TransitionBuilder<T, E>(this).from(fromState).on(onEvent).
                        ignore().setOnSuccessListener(tseTransitions.getOnSuccessListener()).create();
            } else {
                T toState = i < toAmongLength ? tseTransitions.getToAmong()[i] : tseTransitions.getToAmong()[toAmongLength - 1];
                new Transition.TransitionBuilder<T, E>(this).from(fromState).on(onEvent).
                        to(toState).setOnSuccessListener(tseTransitions.getOnSuccessListener()).create();
            }
        }

    }

    private boolean isAllTransitionLengthsValid(int fromAnyLength, int onEachLength, int toAmongLength) {

        if (fromAnyLength > 1) {
            if (toAmongLength > 1 && fromAnyLength != toAmongLength) return false;
            if (onEachLength > 1 && fromAnyLength != onEachLength) return false;
        } else if (toAmongLength > 1) {
            if (fromAnyLength > 1 && toAmongLength != fromAnyLength) return false;
            if (onEachLength > 1 && toAmongLength != onEachLength) return false;
        } else if (onEachLength > 1) {
            if (toAmongLength > 1 && onEachLength != toAmongLength) return false;
            if (fromAnyLength > 1 && fromAnyLength != onEachLength) return false;
        }

        return !(fromAnyLength == 1 && onEachLength == 1 && toAmongLength != 1);

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

    private int getMax(int fromAnyLength, int onEachLength, int toAmongLength) {
        int max = fromAnyLength;
        if (max < onEachLength) max = onEachLength;
        if (max < toAmongLength) max = toAmongLength;
        return max;
    }

}
