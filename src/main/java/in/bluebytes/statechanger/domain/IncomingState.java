package in.bluebytes.statechanger.domain;

/**
 * Holds possible states incoming from third party tool which gets mapped and equated
 * to corresponding State Machine states in the event action methods
 */
public enum IncomingState {
    COMPLETE, FAIL, CLOSE
}
