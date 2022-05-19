package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonValue;

public abstract class QuantifiableConstraints extends ReparsedConstraint {
    Range range;



    public QuantifiableConstraints(IonValue value) {
        range = Range.of(value);
    }

    public Range getRange() {
        return range;
    }

}
