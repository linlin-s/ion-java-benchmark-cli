package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonValue;

public class ByteLength extends QuantifiableConstraints{
    public ByteLength(IonValue value) {
        super(value);
    }

    public static ByteLength of(IonValue field) {
        return new ByteLength(field);
    }
}
