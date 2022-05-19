package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonValue;

public class Precision extends QuantifiableConstraints{
    public Precision(IonValue value) {
        super(value);
    }

    public static Precision of(IonValue field) {
        return new Precision(field);
    }
}
