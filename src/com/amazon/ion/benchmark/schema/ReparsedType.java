package com.amazon.ion.benchmark.schema;

import com.amazon.ion.IonStruct;
import com.amazon.ion.IonType;
import com.amazon.ion.IonValue;
import com.amazon.ion.benchmark.schema.constraints.*;

import java.util.HashMap;
import java.util.Map;

// Parsing the type definition in ISL file into ReparsedType format which allows getting constraints information directly.
public class ReparsedType {
    private static final String KEYWORD_TIMESTAMP_PRECISION = "timestamp_precision";
    private static final String KEYWORD_TYPE = "type";
    private static final String KEYWORD_CODE_POINT_LENGTH = "codepoint_length";
    private static final String KEYWORD_REGEX = "regex";
    private static final String KEYWORD_CONTAINER_LENGTH = "container_length";
    private static final String KEYWORD_BYTE_LENGTH = "byte_length";
    private static final String KEYWORD_SCALE = "scale";
    private static final String KEYWORD_PRECISION = "precision";
    private static final String KEYWORD_VALID_VALUES = "valid_values";
    private static final String KEYWORD_NAME = "name";
    private static final String KEYWORD_FIELDS = "fields";
    private static final String KEYWORD_CONTAINS = "contains";
    private static final String KEYWORD_ORDERED_ELEMENTS = "ordered_elements";
    private static final String KEYWORD_OCCURS = "occurs";

    // Using map to avoid processing the multiple repeat constraints situation.
    private final Map<String, ReparsedConstraint> constraintMap;
    private final IonStruct constraintStruct;
    private Occurs occurs;

//    /**
//     * Initializing the newly created ReparsedType object.
//     * @param type represents type definition of ISL file.
//     * @param constraintStruct
//     */

    /**
     *
     * @param typeDefinition
     */
    public ReparsedType(IonStruct typeDefinition) {
        this.constraintStruct = typeDefinition;
        constraintMap = new HashMap<>();
        typeDefinition.forEach(this::handleField);
    }

    public Occurs getOccurs() {
        return this.occurs;
    }

    /**
     * Get the name of type definition.
     * @return the name of type definition.
     */
    public String getName() {
        return constraintStruct.get(KEYWORD_NAME).toString();
    }

    /**
     * Handling the fields which are not used for specifying generated data.
     * @param field represents the field contained by the type definition.
     */
    private void handleField(IonValue field) {
        switch (field.getFieldName()) {
            case KEYWORD_NAME:
            case KEYWORD_TYPE:
            case KEYWORD_OCCURS:
                return;
            default:
                constraintMap.put(field.getFieldName(), toConstraint(field));
        }
    }

    /**
     * Get the value of constraint 'type' in IonType format.
     * @return the value of 'type' in IonType format.
     */
    public IonType getIonType() {
        return IonType.valueOf(constraintStruct.get(KEYWORD_TYPE).toString().toUpperCase());
    }

    /**
     * Get the constraintMap.
     * The keys in constraintMap represent constraint name, and the values represents the ReparsedConstraint.
     * @return constraintMap.
     */
    public Map<String, ReparsedConstraint> getConstraintMap() {
        return constraintMap;
    }

    //TODO: Separate into two flavors - container and scalar?
    /**
     * This method helps to categorize constraints based on the data type that they represent.
     * @param field represents the field contained in type definition.
     * @return ReparsedConstraints which are processed based on the provided constraint 'type'.
     */
    private ReparsedConstraint toConstraint(IonValue field) {
        switch (field.getFieldName()) {
            //TODO: Add cases of constraints 'annotation' and 'occurs'.
            //TODO: Add container type constraints: 'element', 'ordered_element', 'fields', these might cover some of the implemented constraints.
            case KEYWORD_BYTE_LENGTH:
                return ByteLength.of(field);
            case KEYWORD_PRECISION:
                return Precision.of(field);
            case KEYWORD_SCALE:
                return Scale.of(field);
            case KEYWORD_CODE_POINT_LENGTH:
                return CodepointLength.of(field);
            case KEYWORD_CONTAINER_LENGTH:
                return ContainerLength.of(field);
            case KEYWORD_VALID_VALUES:
                return ValidValues.of(field);
            case KEYWORD_REGEX:
                return Regex.of(field);
            case KEYWORD_TIMESTAMP_PRECISION:
                return TimestampPrecision.of(field);
            case KEYWORD_FIELDS:
                return Fields.of(field);
            case KEYWORD_CONTAINS:
                return Contains.of(field);
            case KEYWORD_ORDERED_ELEMENTS:
                return OrderedElements.of(field);
            case KEYWORD_OCCURS:
                this.occurs = Occurs.of(field);
            default:
                throw new IllegalArgumentException("This field is not understood: " + field);
        }
    }
}
