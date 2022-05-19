package com.amazon.ion.benchmark.schema;

import com.amazon.ion.IonStruct;
import com.amazon.ion.IonType;
import com.amazon.ion.IonValue;
import com.amazon.ion.benchmark.IonSchemaUtilities;
import com.amazon.ion.benchmark.schema.constraints.*;
import com.amazon.ionschema.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReparsedType {
    public final Type type;
    List<ReparsedConstraint> constraints;
    // Using map to avoid dealing with the multiple repeat constraints situation.
    Map<String, ReparsedConstraint> constraintMap;
    public ReparsedType(Type type) {
        this.type = type;
        constraints = new ArrayList<>();
        constraintMap = new HashMap<>();
        getIsl().forEach(this::handleField);
    }

    public String getName() {
        return type.getName();
    }

    private void handleField(IonValue field) {
        switch (field.getFieldName()) {
            case "name":
            case "type":
                return;
            default:
                constraints.add(toConstraint(field));
                constraintMap.put(field.getFieldName(), toConstraint(field));
        }
    }

    /**
     * Redefining the getIsl method when creating ReparsedType object.
     * @return an IonStruct which contains constraints in type definition.
     */
    public IonStruct getIsl() {
        return (IonStruct) type.getIsl();
    }

    //TODO: Should we return a class or something here?
    //TODO: This might not exist
    public IonType getIonType() {
        return IonType.valueOf(getIsl().get(IonSchemaUtilities.KEYWORD_TYPE).toString().toUpperCase());
    }

    public List<ReparsedConstraint> getConstraints() {
        return constraints;
    }

    public Map<String, ReparsedConstraint> getConstraintMap() {
        return constraintMap;
    }

    //TODO: Constraints come in two flavors- container and scalar?

    private static ReparsedConstraint toConstraint(IonValue field) {
        switch (field.getFieldName()) {
            // annotations
            // occurs: The occurs field indicates either the exact or minimum/maximum number of occurrences of
            // the specified type or field. The special value optional is synonymous with range::[0, 1]; similarly,
            // the special value required is synonymous with the exact value 1 (or range::[1, 1]).
            case "byte_length":
                return ByteLength.of(field);
            case "precision":
                return Precision.of(field);
            case "scale":
                return Scale.of(field);
            case "codepoint_length":
                return CodepointLength.of(field);
            case "container_length":
                return CodepointLength.of(field);
            case "valid_values":
                return ValidValues.of(field);
            case "regex":
                return Regex.of(field);
            case "timestamp_precision":
                return TimestampPrecision.of(field);
            default:
                throw new IllegalArgumentException("This field is not understood: " + field);
        }
    }
}
