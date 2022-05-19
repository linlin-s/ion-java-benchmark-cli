package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonSequence;
import com.amazon.ion.IonValue;
import com.amazon.ion.Timestamp;
import com.amazon.ion.benchmark.IonSchemaUtilities;

import java.util.Random;

public class TimestampPrecision extends QuantifiableConstraints{
    public TimestampPrecision(IonValue value) {
        super(value);
    }
    public static TimestampPrecision of(IonValue field) {
        return new TimestampPrecision(field);
    }

    public static Timestamp.Precision getRandomTimestampPrecision(Range range) {
        Random random = new Random();
        IonSequence constraintSequence = range.sequence;
        Timestamp.Precision[] precisions = Timestamp.Precision.values();

        int lowerBoundOrdinal;
        int upperBoundOrdinal;
        String lowerBound = constraintSequence.get(0).toString();
        String upperBound = constraintSequence.get(1).toString();
        if (lowerBound.equals(IonSchemaUtilities.KEYWORD_MIN)) {
            lowerBoundOrdinal = 0;
        } else {
            lowerBoundOrdinal = Timestamp.Precision.valueOf(lowerBound.toUpperCase()).ordinal();
        }
        if (upperBound.equals(IonSchemaUtilities.KEYWORD_MAX)) {
            upperBoundOrdinal = precisions.length;
        } else {
            upperBoundOrdinal = Timestamp.Precision.valueOf(upperBound.toUpperCase()).ordinal();
        }
        int randomIndex = random.nextInt(upperBoundOrdinal - lowerBoundOrdinal + 1) + lowerBoundOrdinal;
        return precisions[randomIndex];
    }



}
