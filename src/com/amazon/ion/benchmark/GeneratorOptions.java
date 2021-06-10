package com.amazon.ion.benchmark;

import java.util.Map;

public class GeneratorOptions {
        
    public static void excuteGenerator(Map<String, Object> optionsMap) throws Exception {
            
        String format = optionsMap.get("--format").toString().substring(1,optionsMap.get("--format").toString().length()-1);
        String exp_range = optionsMap.get("--decimal-exponent-range").toString() ;
        String coeffi_digits= optionsMap.get("--decimal-coefficient-digit-range").toString();
        
        WriteRandomIonValues.writeRandomDecimals(Integer.valueOf(optionsMap.get("<data_size>").toString()), optionsMap.get("<output_file>").toString(), format, exp_range, coeffi_digits);
    }       
}
