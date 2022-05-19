package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonValue;

public class Regex extends ReparsedConstraint{
    String pattern;
    public Regex(IonValue pattern) {
        this.pattern = pattern.toString().replace("\"","");
    }
    public String getPattern() {
        return this.pattern;
    }
    public static Regex of(IonValue value) {
        return new Regex(value);
    }

}
