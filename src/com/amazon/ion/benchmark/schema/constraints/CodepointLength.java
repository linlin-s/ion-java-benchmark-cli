package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonValue;

// codepoint_length: 5
// codepoint_length: range::[10, max]
// codepoint_length: range::[min, 100]
// codepoint_length: range::[10, 100]

public class CodepointLength extends QuantifiableConstraints {

    public CodepointLength(IonValue value) {
        super(value);
    }

    public static CodepointLength of(IonValue field) {
       return new CodepointLength(field);
    }
}
