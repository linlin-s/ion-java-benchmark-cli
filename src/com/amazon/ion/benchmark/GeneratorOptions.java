package com.amazon.ion.benchmark;

import java.util.Map;

import com.amazon.ion.IonReader;
import com.amazon.ion.system.IonReaderBuilder;

public class GeneratorOptions {
        
    public static void executeGenerator(Map<String, Object> optionsMap) throws Exception {
        int size = Integer.valueOf(optionsMap.get("--data-size").toString());
        String expRange = optionsMap.get("--decimal-exponent-range").toString();
        String coefficientDigits = optionsMap.get("--decimal-coefficient-digit-range").toString();
        String format = optionsMap.get("--format").toString().substring(1, optionsMap.get("--format").toString().length() - 1);
        String path = optionsMap.get("<output_file>").toString();
        String range = optionsMap.get("--text-code-point-range").toString();
        String type = optionsMap.get("--data-type").toString();

        String timestampTemplate;
        if (optionsMap.get("--timestamps-template") == null) {
            timestampTemplate = "random";
        } else {
            timestampTemplate = optionsMap.get("--timestamps-template").toString();
        }

        switch (type) {
            case "timestamp":
                WriteRandomIonValues.writeRandomTimestamps(Integer.valueOf(optionsMap.get("--data-size").toString()), optionsMap.get("<output_file>").toString(), timestampTemplate, format);
                break;
            case "string":
                WriteRandomIonValues.writeRandomStrings(Integer.valueOf(optionsMap.get("--data-size").toString()), optionsMap.get("<output_file>").toString(), range, format);
                break;
            case "decimal":
                WriteRandomIonValues.writeRandomDecimals(Integer.valueOf(optionsMap.get("--data-size").toString()), optionsMap.get("<output_file>").toString(), format, expRange, coefficientDigits);
                break;
            case "integer":
                WriteRandomIonValues.writeRandomInts(size, format, path);
                break;
            case "float":
                WriteRandomIonValues.writeRandomFloats(size, format, path);
                break;
            case "blob":
                WriteRandomIonValues.writeRandomLobs(size, type, format, path);
                break;
            case "clob":
                WriteRandomIonValues.writeRandomLobs(size, type, format, path);
                break;
            case "symbol":
                WriteRandomIonValues.writeRandomSymbolValues(size, format, path);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
