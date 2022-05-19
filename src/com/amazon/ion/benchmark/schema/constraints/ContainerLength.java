package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonValue;

public class ContainerLength extends QuantifiableConstraints{
    public ContainerLength(IonValue value) {
        super(value);
    }

    public static ContainerLength of(IonValue field) {
        return new ContainerLength(field);
    }
}
