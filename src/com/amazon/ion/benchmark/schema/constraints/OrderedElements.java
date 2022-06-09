package com.amazon.ion.benchmark.schema.constraints;

import com.amazon.ion.IonList;
import com.amazon.ion.IonStruct;
import com.amazon.ion.IonValue;
import com.amazon.ion.benchmark.schema.ReparsedType;
import com.amazon.ion.system.IonSystemBuilder;

import java.util.stream.Collectors;


//An inlined type definition may also contain an occurs field that indicates how many times a value of a specific type may occur.
//This is applicable only within the context of ordered_elements and struct fields constraints.
public class OrderedElements implements ReparsedConstraint {
    private final IonList orderedElementsConstraints;

    public OrderedElements(IonList orderedElementsConstraints) {
        this.orderedElementsConstraints = orderedElementsConstraints;
    }

    public static OrderedElements of(IonValue field) {
        IonList list = (IonList) field;
        list.forEach(p ->parseConstraint(p));
        return new OrderedElements((IonList) field);
    }

    public IonList getOrderedElementsConstraints() {
        return this.orderedElementsConstraints;
    }

    private static ReparsedType parseConstraint(IonValue constraint) {
        return new ReparsedType((IonStruct) constraint);
    }
}
