package TestUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by himanshu.virmani on 15/11/15.
 */
@AllArgsConstructor
public enum MySampleState {

    CREATED("created"), ONHOLD("onhold"), DELIVERED("delivered"), CANCELLED("cancelled");

    @Getter
    private String state;
}
