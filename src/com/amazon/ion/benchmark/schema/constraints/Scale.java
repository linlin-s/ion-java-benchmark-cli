package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonValue;

public class Scale extends QuantifiableConstraints{

    public Scale(IonValue value) {
        super(value);
    }

    public static Scale of(IonValue field) {
        return new Scale(field);
    }
}
