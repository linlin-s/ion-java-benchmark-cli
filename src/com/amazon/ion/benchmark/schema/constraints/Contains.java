package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonList;
import com.amazon.ion.IonSequence;
import com.amazon.ion.IonValue;

public class Contains implements ReparsedConstraint {
    private final IonList expectedContainedList;

    /**
     *
     */
    public Contains(IonValue expectedContainedList) {
        this.expectedContainedList = (IonList) expectedContainedList;
    }

    public IonList getExpectedContainedList() {
        return this.expectedContainedList;
    }

    public static Contains of(IonValue field) {
        return new Contains(field);
    }
}
