package com.amazon.ion.benchmark;

import com.amazon.ion.IonList;
import com.amazon.ion.IonReader;
import com.amazon.ion.IonStruct;
import com.amazon.ion.IonType;
import com.amazon.ion.IonValue;
import com.amazon.ion.Timestamp;
import com.amazon.ion.system.IonReaderBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Contain the methods which process the constraints provided by the Ion Schema file and define the constants relevant to the Ion Schema file.
 */
public class IonSchemaUtilities {
    public static final String KEYWORD_ANNOTATIONS = "annotations";
    public static final String KEYWORD_REQUIRED = "required";
    public static final String KEYWORD_OPTIONAL = "optional";
    public static final String KEYWORD_TIMESTAMP_PRECISION = "timestamp_precision";
    public static final String KEYWORD_TYPE = "type";
    public static final String KEYWORD_FIELDS = "fields";
    public static final String KEYWORD_CODE_POINT_LENGTH = "codepoint_length";
    public static final String KEYWORD_OCCURS = "occurs";
    public static final String KEYWORD_ELEMENT = "element";
    public static final String KEYWORD_ORDERED_ELEMENTS = "ordered_elements";
    public static final String KEYWORD_ORDERED = "ordered";
    public static final String KEYWORD_NAME = "name";
    public static final String KEYWORD_CONTAINER_LENGTH = "container_length";

    /**
     * Extract the value of the constraints, select from the set (occurs | container_length | codepoint_length).
     * @param value is the Ion struct which contain the current constraint field
     * @param keyWord is the field name of the constraint
     * @return the value of the current constraint.
     * @throws IOException if an error occur when constructing the IonReader.
     */
    public static int parseConstraints(IonStruct value, String keyWord) throws IOException {
        Random random = new Random();
        int result = 0;
        int min = 0;
        int max;
        try (IonReader reader = IonReaderBuilder.standard().build(value)) {
            reader.next();
            reader.stepIn();
            while (reader.next() != null) {
                if (reader.getFieldName().equals(keyWord)) {
                    IonType type = reader.getType();
                    switch (type) {
                        case INT:
                            result = reader.intValue();
                            break;
                        case SYMBOL:
                            if (reader.stringValue().equals(KEYWORD_REQUIRED)) {
                                result = 1;
                            } else if (reader.stringValue().equals(KEYWORD_OPTIONAL)) {
                                result = random.nextInt(2);
                            } else {
                                throw new IllegalArgumentException ("The value of this option is not supported");
                            }
                            break;
                        case LIST:
                            reader.stepIn();
                            reader.next();
                            if (reader.getType() != IonType.SYMBOL) {
                                min = reader.intValue();
                            }
                            reader.next();
                            if (reader.getType() == IonType.SYMBOL) {
                                max = Integer.MAX_VALUE;
                            } else {
                                max = reader.intValue();
                            }
                            result = random.nextInt(max - min + 1) + min;
                            break;
                    }
                }
            }
            return result;
        }
    }

    /**
     * Parse the precision of the timestamp.
     * @param value is the Ion struct which contain the current constraint field.
     * @throws IOException if errors occur when reading the data.
     * @return requested timestamp precision
     */
    public static Timestamp.Precision getTimestampPrecisionTemplate(IonStruct value) throws IOException {
        Timestamp.Precision precision = null;
        try (IonReader reader = IonReaderBuilder.standard().build(value)) {
            reader.next();
            reader.stepIn();
            while (reader.next() != null) {
                if (reader.getFieldName().equals(KEYWORD_TIMESTAMP_PRECISION)) {
                    IonType type = reader.getType();
                    switch (type) {
                        case SYMBOL:
                            precision = Timestamp.Precision.valueOf(reader.stringValue().toUpperCase());
                            break;
                    }
                }
            }
        }
        return precision;
    }

    /**
     * Parse the field 'annotations' based on the provided constraints (required|ordered).
     * @param constraintStruct contains the top-level constraints of Ion Schema.
     * @return IonList which contains annotations
     * @throws IOException if error occur when reading constraints.
     */
    public static IonList getAnnotation(IonStruct constraintStruct) throws IOException {
        IonList annotationList = null;
        Random random = new Random();
        try (IonReader annotationInfo = IonReaderBuilder.standard().build(constraintStruct)){
            annotationInfo.next();
            annotationInfo.stepIn();
            while (annotationInfo.next() != null) {
                if (annotationInfo.getFieldName().equals(IonSchemaUtilities.KEYWORD_ANNOTATIONS)) {
                    IonValue annotationValue = ReadGeneralConstraints.SYSTEM.newValue(annotationInfo);
                    List<String> constraint = Arrays.asList(annotationValue.getTypeAnnotations());
                    IonList annotations = (IonList) annotationValue;
                    annotationList = checkOrdered(constraint, annotations, random);
                }
            }
        }
        return annotationList;
    }

    /**
     * This is a helper method of getAnnotation which processes the constraint 'Ordered'.
     * @param constraint is a List which contains all annotations of 'annotations' field.
     * @param annotationList is the original annotation List without any consideration about constraints.
     * @param random is a random integer generator.
     * @return a List of processed annotations.
     * @throws IOException if error occur when reading constraints.
     */
    private static IonList checkOrdered(List<String> constraint, IonList annotationList, Random random) throws IOException {
        IonList result = annotationList;
        if (!constraint.contains(IonSchemaUtilities.KEYWORD_ORDERED)) {
            List<IonValue> annotations = annotationList.stream().collect(Collectors.toList());
            Collections.shuffle(annotations);
            try (IonReader shuffledAnnotationReader = IonReaderBuilder.standard().build(annotations.toString())) {
                shuffledAnnotationReader.next();
                result = (IonList) ReadGeneralConstraints.SYSTEM.newValue(shuffledAnnotationReader);
            }
        }
        return checkRequired(constraint, result,random);
    }

    /**
     * This is a helper method of getAnnotation which processes the constraint 'required'.
     * @param constraint is a List which contains all annotations of 'annotations' field.
     * @param annotationList is the annotation List after processing with the constraint 'ordered'.
     * @param random is a random integer generator.
     * @returna a List of processed annotations.
     * @throws IOException if error occur when reading constraints.
     */
    private static IonList checkRequired(List<String> constraint, IonList annotationList, Random random) throws IOException {
        IonList result = annotationList;
        if (!constraint.contains(IonSchemaUtilities.KEYWORD_REQUIRED)) {
            int randomValueOne = random.nextInt(annotationList.size());
            int randomValueTwo = random.nextInt(annotationList.size());
            List<IonValue> subAnnotationList = annotationList.subList(Math.min(randomValueOne, randomValueTwo), Math.max(randomValueOne, randomValueTwo));
            if (subAnnotationList != null) {
                try (IonReader annotationReader = IonReaderBuilder.standard().build(subAnnotationList.toString())) {
                    annotationReader.next();
                    result = (IonList) ReadGeneralConstraints.SYSTEM.newValue(annotationReader);
                }
            } else {
                result = null;
            }
        }
        return result;
    }
}
