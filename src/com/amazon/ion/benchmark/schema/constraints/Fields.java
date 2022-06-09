package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonStruct;
import com.amazon.ion.IonValue;
import com.amazon.ion.benchmark.schema.ReparsedType;

import java.util.HashMap;
import java.util.Map;

//fields: {
//        city: string,
//        age: { type: int, valid_values: range::[0, 200] },
//        }

public class Fields implements ReparsedConstraint {
    public static Map<String, ReparsedType> fieldMap;

    public Fields(IonValue value) {
        fieldMap = new HashMap<>();
        IonStruct fieldsStruct = (IonStruct) value;
        fieldsStruct.forEach(this::handleField);
    }

    private void handleField(IonValue value) {
        IonStruct fieldStruct = (IonStruct) value;
        fieldMap.put(fieldStruct.getFieldName(), new ReparsedType(fieldStruct));
    }

    public Map<String, ReparsedType> getFieldMap() {
        return this.fieldMap;
    }

    public static Fields of(IonValue value) {
        return new Fields(value);
    }
}
