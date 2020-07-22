package in.bluebytes.statechanger.domain;

/**
 * The SETTLE event here doesn't potentially change the state but acts as a trigger to action method
 * to determine the state to be changed to
 */
public enum DocumentEvent {
    CREATE, UPDATE, SETTLE_COMPLETE, SETTLE_FAIL, SETTLE_CLOSE, SETTLE
}
