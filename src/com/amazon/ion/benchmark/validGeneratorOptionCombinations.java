package com.amazon.ion.benchmark;

import com.amazon.ion.IonType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Check if the current combination of options is valid.
 * e.g. The specific options only follow the specific data type, "--text-code-point-range" cannot follow the type "decimal"
 */
public class validGeneratorOptionCombinations {
    /**
     * The method combine all options which are not available for the current data type, and check
     * if the current command line which aim to generate the current type of data contain those options or not.
     * @param typeOptionsList  is a list of option lists for different type of data.
     * @param commandLine is a list of arguments which indicates options.
     */
    private static void throwException (ArrayList <ArrayList<String>> typeOptionsList, List commandLine) {
        List<String> combinedList = new ArrayList<>();
        for (ArrayList<String> singleList : typeOptionsList) {
            combinedList.addAll(singleList);
        }
        for (String option : combinedList) {
            if (commandLine.contains(option)) throw new IllegalStateException("Please provide options supported by the current type");
        }
    }

    /**
     * This method is used for checking if the combination of options is valid or not.
     * In other word, some options can be only assigned to a specific type of data, if these options provided
     * when generating other types, then this combination ia not valid.
     * @param args is the arguments provided in command line.
     * @param optionsMap is a hashmap which match the option name and its value.
     */
    public static void checkValid (String [] args, Map <String, Object> optionsMap) {
        List commandLine = Arrays.asList(args);
        ArrayList<String> timestampsOptions = new ArrayList<>(Arrays.asList("-M", "--timestamps-template"));
        ArrayList<String> decimalOptions = new ArrayList<>(Arrays.asList("-E", "--decimal-exponent-range <exp_range>", "-C", "--decimal-coefficient-digit-range"));
        ArrayList<String> stringOptions = new ArrayList<>(Arrays.asList("-N", "--text-code-point-range"));

        IonType type = IonType.valueOf(optionsMap.get("--data-type").toString().toUpperCase());
        switch (type) {
            case TIMESTAMP:
                ArrayList<ArrayList<String>> invalidForTimestamp = new ArrayList<>();
                invalidForTimestamp.add(decimalOptions);
                invalidForTimestamp.add(stringOptions);
                validGeneratorOptionCombinations.throwException(invalidForTimestamp, commandLine);
                break;
            case STRING:
                ArrayList<ArrayList<String>> invalidForString = new ArrayList<>();
                invalidForString.add(decimalOptions);
                invalidForString.add(timestampsOptions);
                validGeneratorOptionCombinations.throwException(invalidForString, commandLine);
                break;
            case DECIMAL:
                ArrayList<ArrayList<String>> invalidForDecimal = new ArrayList<>();
                invalidForDecimal.add(timestampsOptions);
                invalidForDecimal.add(stringOptions);
                validGeneratorOptionCombinations.throwException(invalidForDecimal, commandLine);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }


    }
}
