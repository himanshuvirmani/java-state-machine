Java State Machine  
----------
This is a simple event based state machine library written in java to avoid any boiler plate code around writing if-else/switch cases around state changes for a particular entity. It interprets the current state, new state and event fired to verify if it is a valid transition.


## Status

Java State Machine is currently in a very nascent stage and has a lot improvement scope around making it more generic and addition of more features around state management.

## Usage

### for Maven

```
<dependency>
    <groupId>com.himanshuvirmani</groupId>
    <artifactId>java-state-machine</artifactId>
    <version>1.0.0</version>
</dependency>
```


### for Gradle

``` groovy
repositories {
    mavenCentral()
}
```

``` groovy
dependencies {
    compile 'com.himanshuvirmani:java-state-machine:1.0.0'
}
```

### Sample Code

You can find a working sample project in this repository which showcases some of the features of the library. Find below some excerpts from the same sample project on how to use the library.

##### Initialize

``` java
StateMachine<MySampleState, MySampleEvent, Object> stateMachine =
                new StateMachine<MySampleState, MySampleEvent, Object>(MySampleState.CREATED);
```

##### Creating Transitions

``` java
stateMachine.transition().from(MySampleState.CREATED).to(MySampleState.ONHOLD).on(MySampleEvent.HOLD).setOnSuccessListener(onSuccessListener).create();

stateMachine.transition().from(MySampleState.ONHOLD).to(MySampleState.DELIVERED).on(MySampleEvent.DELIVER).create();
```

##### Firing an event

``` java
try {
    stateMachine.fire(MySampleEvent.HOLD);
} catch (TransitionException e) {
    e.printStackTrace();
}
```

##### Additionally you can have StateChangeListener

```java
MyEntityClass implements StateMachine.StateChangeListener<MySampleState, MySampleEvent>{

@Override
public void onStateChanged(MySampleState from, MySampleState to, MySampleEvent on) {
    log.info("State changed from " + from + " to " + to + " on " + on);
}
```


## To Do
1. Add ToAmong, OnEach for multiple transitions creation.
2. State Exit and Entry Listeners.
3. Better Java Docs
4. More features as and when they come.
