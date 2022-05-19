package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonList;
import com.amazon.ion.IonSequence;
import com.amazon.ion.IonType;
import com.amazon.ion.IonValue;
import com.amazon.ion.benchmark.IonSchemaUtilities;

import java.util.Arrays;
import java.util.Random;

import static com.amazon.ion.benchmark.IonSchemaUtilities.KEYWORD_RANGE;

public class Range {
    IonSequence sequence;
    Random random = new Random();

    public Range(IonSequence sequence) {
       this.sequence = sequence;
    }

    public <T extends IonValue> T lowerBound(Class<T> klass) {
        return klass.cast(sequence.get(0));
    }

    public <T extends IonValue> T upperBound(Class<T> klass) {
        return klass.cast(sequence.get(1));
    }

    // TODO: Validate size? We can assume that ion-schema-kotlin has already done this
    public static Range of(IonValue value) {
        IonSequence sequence;
        if (!(value instanceof IonList)) {
            sequence = value.getSystem().newList(value.clone(), value.clone());
            sequence.addTypeAnnotation("range");
        } else {
            sequence = (IonSequence) value;
        }
        return new Range(sequence);
    }

    /**
     * Check whether the value contains annotation 'range'.
     * @param value represents the constraint.
     * @return the verification result in the boolean format.
     */
    public static boolean isRange(IonValue value) {
        return Arrays.stream(value.getTypeAnnotations()).anyMatch(KEYWORD_RANGE::equals);
    }

    public int getRandomIntValueFromRange() {
        int randomValue;
        int lowerBoundValue;
        int upperBoundValue;
        String lowerBound = sequence.get(0).toString();
        String upperBound = sequence.get(1).toString();
        if (lowerBound.equals(IonSchemaUtilities.KEYWORD_MIN)) {
            lowerBoundValue = Integer.MIN_VALUE;
        } else {
            lowerBoundValue = Integer.parseInt(lowerBound);
        }
        if (upperBound.equals(IonSchemaUtilities.KEYWORD_MAX)) {
            upperBoundValue = Integer.MAX_VALUE;
        } else {
            upperBoundValue = Integer.parseInt(upperBound);
        }
        randomValue = random.nextInt(upperBoundValue - lowerBoundValue + 1) + lowerBoundValue;
        return randomValue;
    }

    public static boolean ifTimestampPrecision(IonValue value) {
        IonType valueType = value.getType();
        switch (valueType) {
            case INT:
                return false;
            case SYMBOL:
                return true;
            default:
                throw new IllegalStateException("This IonValue cannot be processed in the Range.");
        }
    }
}
