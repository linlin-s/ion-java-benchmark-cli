package com.amazon.ion.benchmark.schema.constraints;
//The occurs field indicates either the exact or minimum/maximum number of occurrences of the specified type or field. The special value optional is synonymous with range::[0, 1]; similarly, the special value required is synonymous with the exact value 1 (or range::[1, 1]).
//
//        occurs: 3
//        occurs: range::[1, max]
//        occurs: range::[min, 3]
//        occurs: range::[1, 5]
//        occurs: optional           // equivalent to range::[0, 1]
//        occurs: required           // equivalent to 1 or range::[1, 1]
//


import com.amazon.ion.IonSymbol;
import com.amazon.ion.IonSystem;
import com.amazon.ion.IonValue;
import com.amazon.ion.system.IonSystemBuilder;

import javax.management.relation.RelationNotFoundException;

public class Occurs extends QuantifiableConstraints {
    private final  IonValue occurValue;
    private final static IonSystem SYSTEM = IonSystemBuilder.standard().build();

    private IonValue getOccurValue() {
      return this.occurValue;
  }

    private Occurs(IonValue field) {
      super(field);
      this.occurValue = field;
    }

    public Range getOccurRange() {
        if (this.occurValue instanceof IonSymbol) {
            String occurValue = this.occurValue.toString();
            switch (occurValue) {
                case "optional":
                    return new Range(SYSTEM.newList(SYSTEM.newInt(0), SYSTEM.newInt(1)));
                case "required":
                    return new Range(SYSTEM.newList(SYSTEM.newInt(1), SYSTEM.newInt(1)));
                default:
                    throw new IllegalStateException("The symbol value cannot be processed.");
            }
        } else {
            return QuantifiableConstraints.getRange();
        }
    }


    public static Occurs of(IonValue value) {
        return new Occurs(value);
    }

}
