package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonList;
import com.amazon.ion.IonValue;

// From Ion schema spec:
// valid_values: [2, 3, 5, 7, 11, 13, 17, 19]
// valid_values: ["abc", "def", "ghi"]
// valid_values: [[1], [2.0, 3.0], [three, four, five]]
// valid_values: [2000T, 2004T, 2008T, 2012T]
// valid_values: range::[-100, max]
// valid_values: range::[min, 100]
// valid_values: range::[-100, 100]
// valid_values: range::[0, 100.0]
// valid_values: range::[exclusive::0d0, exclusive::1]
// valid_values: range::[-0.12e4, 0.123]
// valid_values: range::[2000-01-01T00:00:00Z, max]
// valid_values: [1, 2, 3, null, null.int]
// valid_values: [range::[exclusive::0, max], +inf]
public class ValidValues extends ReparsedConstraint {
    //TODO: Should constraint have a type? Should it be parameterized?
// TODO: Handling min and max value
    // Extracting type information from the lowerbound and upperbound value.
    final private IonList validValues;
    final private Range range;
    final private boolean isRange;
    // Constructor which helps initialize the attributes.
    public ValidValues(IonList validValues, boolean isRange) {
        this.validValues = isRange ? null : validValues;
        this.range = isRange ? Range.of(validValues) : null;
        this.isRange = isRange;
    }

    public IonList getValidValues() {
        return validValues;
    }

    public boolean isRange() {
        return isRange;
    }

    public Range getRange() {
        return range;
    }

    public static ValidValues of(IonValue value) {
        // we know this is a container because ion-schema-kotlin already validated it
        // should we be defensive anyway?
        boolean isRange = Range.isRange(value);
        return new ValidValues((IonList) value, isRange);
    }
}
