import TestUtil.MySampleEvent;
import TestUtil.MySampleState;
import com.himanshuvirmani.Condition;
import com.himanshuvirmani.StateMachine;
import com.himanshuvirmani.Transition;
import com.himanshuvirmani.exceptions.TransitionCreationException;
import com.himanshuvirmani.exceptions.TransitionException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by himanshu.virmani on 22/11/15.
 */
public class StateMachineTest {

    StateMachine<MySampleState, MySampleEvent> stateMachine;

    @Before
    public void init() {
        stateMachine =
                new StateMachine<MySampleState, MySampleEvent>(MySampleState.CREATED);
    }


    @Test
    public void testSingleSuccessTransition() {

        stateMachine.setStateChangeListener(new StateMachine.StateChangeListener<MySampleState, MySampleEvent>() {
            @Override
            public void onStateChanged(MySampleState from, MySampleState to, MySampleEvent on) {
                assertTrue(from == MySampleState.CREATED);
                assertTrue(to == MySampleState.ONHOLD);
                assertTrue(on == MySampleEvent.HOLD);
            }
        });

        stateMachine.transition().from(MySampleState.CREATED).to(MySampleState.ONHOLD).on(MySampleEvent.HOLD).setOnSuccessListener(new Transition.onSuccessListener<MySampleState, MySampleEvent>() {
            @Override
            public void onSuccess(MySampleState from, MySampleState to, MySampleEvent on) {
                assertTrue(from == MySampleState.CREATED);
                assertTrue(to == MySampleState.ONHOLD);
                assertTrue(on == MySampleEvent.HOLD);
            }
        }).create();
        stateMachine.transition().from(MySampleState.ONHOLD).to(MySampleState.DELIVERED).on(MySampleEvent.DELIVER).create();
        stateMachine.transition().from(MySampleState.DELIVERED).on(MySampleEvent.CANCEL).ignore().create();

        stateMachine.fire(MySampleEvent.HOLD);

    }


    @Test
    public void testTransitionExceptionCase() {

        stateMachine.transition().from(MySampleState.CREATED).to(MySampleState.ONHOLD).on(MySampleEvent.HOLD).setOnSuccessListener(new Transition.onSuccessListener<MySampleState, MySampleEvent>() {
            @Override
            public void onSuccess(MySampleState from, MySampleState to, MySampleEvent on) {

            }
        }).create();
        stateMachine.transition().from(MySampleState.ONHOLD).to(MySampleState.DELIVERED).on(MySampleEvent.DELIVER).create();
        stateMachine.transition().from(MySampleState.DELIVERED).on(MySampleEvent.CANCEL).ignore().create();

        try {
            stateMachine.fire(MySampleEvent.DELIVER);
        } catch (TransitionException e) {
            assertEquals(stateMachine.getCurrentState(), MySampleState.CREATED);
        }
    }

    @Test
    public void testIgnoreCase() {

        stateMachine.transition().from(MySampleState.CREATED).to(MySampleState.ONHOLD).on(MySampleEvent.HOLD).setOnSuccessListener(new Transition.onSuccessListener<MySampleState, MySampleEvent>() {
            @Override
            public void onSuccess(MySampleState from, MySampleState to, MySampleEvent on) {

            }
        }).create();
        stateMachine.transition().from(MySampleState.ONHOLD).to(MySampleState.DELIVERED).on(MySampleEvent.DELIVER).create();
        stateMachine.transition().from(MySampleState.DELIVERED).on(MySampleEvent.CANCEL).ignore().create();

        stateMachine.fire(MySampleEvent.HOLD);
        stateMachine.fire(MySampleEvent.DELIVER);
        stateMachine.fire(MySampleEvent.CANCEL);
        assertEquals(stateMachine.getCurrentState(), MySampleState.DELIVERED);
    }

    @Test
    public void testMultipleTransitionsInOneSuccess() {

        stateMachine.transitions().fromAny(MySampleState.CREATED, MySampleState.ONHOLD)
                .toAmong(MySampleState.ONHOLD, MySampleState.DELIVERED).onEach(MySampleEvent.HOLD, MySampleEvent.DELIVER).create();

        stateMachine.fire(MySampleEvent.HOLD);

        assertEquals(stateMachine.getCurrentState(), MySampleState.ONHOLD);
    }

    @Test
    public void testMultipleIgnoreTransitionsSuccess() {

        stateMachine.transitions().fromAny(MySampleState.CREATED, MySampleState.ONHOLD).onEach(MySampleEvent.HOLD, MySampleEvent.DELIVER).ignore().create();

        stateMachine.fire(MySampleEvent.HOLD);

        assertEquals(stateMachine.getCurrentState(), MySampleState.CREATED);
    }

    @Test
    public void testSingleEventMultipleFromIgnoreSuccess() {

        stateMachine.transitions().fromAny(MySampleState.CREATED, MySampleState.ONHOLD).onEach(MySampleEvent.DELIVER).ignore().create();

        stateMachine.fire(MySampleEvent.DELIVER);

        assertEquals(stateMachine.getCurrentState(), MySampleState.CREATED);
    }

    @Test
    public void testNoIgnoreNoToFailure() {

        try {
            stateMachine.transitions().fromAny(MySampleState.CREATED, MySampleState.ONHOLD).onEach(MySampleEvent.DELIVER).create();
            assertEquals(true, false);
        } catch (Exception e) {
            assertTrue(e instanceof TransitionCreationException);
        }
    }

    @Test
    public void testWrongFromToArgumentsFailure() {
        try {
            stateMachine.transitions().fromAny(MySampleState.CREATED, MySampleState.ONHOLD)
                    .toAmong(MySampleState.ONHOLD, MySampleState.DELIVERED, MySampleState.CANCELLED).onEach(MySampleEvent.DELIVER).create();
            assertEquals(true, false);
        } catch (Exception e) {
            assertTrue(e instanceof TransitionCreationException);
        }
    }

    @Test
    public void testWrongFromToOnArgumentsFailure() {
        try {
            stateMachine.transitions().fromAny(MySampleState.CREATED)
                    .toAmong(MySampleState.ONHOLD, MySampleState.DELIVERED).onEach(MySampleEvent.DELIVER).create();
            assertEquals(true, false);
        } catch (Exception e) {
            assertTrue(e instanceof TransitionCreationException);
        }
    }

    @Test
    public void testSingleEventMultipleFromToArgumentsSuccess() {
        stateMachine.transitions().fromAny(MySampleState.CREATED, MySampleState.ONHOLD)
                .toAmong(MySampleState.ONHOLD, MySampleState.DELIVERED).onEach(MySampleEvent.DELIVER).create();

        stateMachine.fire(MySampleEvent.DELIVER);
        assertEquals(stateMachine.getCurrentState(), MySampleState.ONHOLD);

        stateMachine.fire(MySampleEvent.DELIVER);

        assertEquals(stateMachine.getCurrentState(), MySampleState.DELIVERED);
    }

    @Test
    public void testSingleEventMultipleFromSingleToArgumentsSuccess() {
        stateMachine.transitions().fromAny(MySampleState.CREATED, MySampleState.ONHOLD)
                .toAmong(MySampleState.DELIVERED).onEach(MySampleEvent.DELIVER).create();

        stateMachine.fire(MySampleEvent.DELIVER);
        assertEquals(stateMachine.getCurrentState(), MySampleState.DELIVERED);

        stateMachine.fire(MySampleEvent.DELIVER, MySampleState.ONHOLD);

        assertEquals(stateMachine.getCurrentState(), MySampleState.DELIVERED);
    }


    @Test
    public void testSingleConditionalTransitionSuccess() {
        final int a = 2;
        final int b = 2;
        stateMachine.transition().from(MySampleState.CREATED).to(MySampleState.ONHOLD).on(MySampleEvent.HOLD).when(new Condition() {
            @Override
            public boolean isMet() {
                return a==b;
            }
        }).create();
        stateMachine.transition().from(MySampleState.ONHOLD).to(MySampleState.DELIVERED).on(MySampleEvent.DELIVER).create();

        stateMachine.fire(MySampleEvent.HOLD);
        assertEquals(stateMachine.getCurrentState(), MySampleState.ONHOLD);
    }

    @Test
    public void testSingleConditionNotMetTransition() {
        final int a = 2;
        final int b = 3;
        stateMachine.transition().from(MySampleState.CREATED).to(MySampleState.ONHOLD).on(MySampleEvent.HOLD).when(new Condition() {
            @Override
            public boolean isMet() {
                return a==b;
            }
        }).create();
        stateMachine.transition().from(MySampleState.ONHOLD).to(MySampleState.DELIVERED).on(MySampleEvent.DELIVER).create();

        stateMachine.fire(MySampleEvent.HOLD);
        assertEquals(stateMachine.getCurrentState(), MySampleState.CREATED);
    }


}
