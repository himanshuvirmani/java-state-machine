package TestUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by himanshu.virmani on 15/11/15.
 */
@AllArgsConstructor
public enum MySampleEvent {

    CREATE("create"), HOLD("hold"), DELIVER("deliver"), CANCEL("cancel");

    @Getter
    private String event;

}
