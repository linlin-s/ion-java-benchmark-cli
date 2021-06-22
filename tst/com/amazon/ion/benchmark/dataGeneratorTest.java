package com.amazon.ion.benchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.amazon.ion.IonReader;
import com.amazon.ion.IonType;
import com.amazon.ion.Timestamp;
import com.amazon.ion.system.IonReaderBuilder;
import com.amazon.ion.util.IonStreamUtils;
import org.junit.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class dataGeneratorTest {

    /**
     * Construct the input Map <String, Object>, the key is the name of the option, the value is the parameter provided in the unit test.
     * @param size specifies the size in bytes of the generated file.
     * @param dataType represents the scalar type of Ion data.
     * @param textCodePointRange provides the range of Unicode code point of characters which construct the Ion string.
     * @param coefficientDigit the range of digit number of coefficient when the decimal represented in coefficient * 10 ^ exponent.
     * @param path the destination of the generated file.
     * @param timestampsTemplate is a string which provides a series of template timestamps which data generating process will follow with.
     * @param format the format of output file (ion_binary | ion_text).
     * @param expRange the range of exponent when the decimal represented in coefficient * 10 ^ exponent.
     * @return the constructed hashmap which can be used in the following test process.
     */
    private static Map<String, Object> testCasesGenerator (Object size, Object dataType, Object textCodePointRange, Object coefficientDigit, Object path, Object timestampsTemplate, Object format, Object expRange) {
        HashMap <String, Object> optionsMap = new HashMap<String, Object>();
        optionsMap.put("--data-size", size);
        optionsMap.put("generate", "true");
        optionsMap.put("--data-type", dataType);
        optionsMap.put("--text-code-point-range", textCodePointRange);
        optionsMap.put("--decimal-coefficient-digit-range", coefficientDigit);
        optionsMap.put("<output_file>", path);
        optionsMap.put("--timestamps-template", timestampsTemplate);
        optionsMap.put("--decimal-exponent-range", expRange);
        optionsMap.put("--format", format);
        return  optionsMap;
    }

    /**
     * Assert generated Ion data is the same type as expected.
     * @throws Exception if error occurs when executing Ion data generator.
     */
    @Test
    public void testGeneratedType() throws Exception {
        Map<String, Object> optionsMap = dataGeneratorTest.testCasesGenerator("500","decimal", "[1,1114111]", "[1,10]", "test1.ion", "2021T", "[ion_text]", "[-13,-2]");
        GeneratorOptions.executeGenerator(optionsMap);
        IonReader reader = IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(optionsMap.get("<output_file>").toString())));
        while (reader.next() != null) {
            assertTrue(reader.getType() == IonType.valueOf(optionsMap.get("--data-type").toString().toUpperCase()));
        }
    }

    /**
     * Assert the exponent range of generated Ion decimals is conform with the expected range.
     * @throws Exception if error occurs when executing Ion data generator.
     */
    @Test
    public void testGeneratedDecimalExponentRange() throws Exception {
        Map<String, Object> optionsMap = dataGeneratorTest.testCasesGenerator("1000","decimal", "[1,1114111]", "[1,10]", "test2.10n", "2021T", "[ion_binary]", "[-13,-2]");

        GeneratorOptions.executeGenerator(optionsMap);
        IonReader reader = IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(optionsMap.get("<output_file>").toString())));

        List<Integer> range = WriteRandomIonValues.rangeParser(optionsMap.get("--decimal-exponent-range").toString());
        while (reader.next() != null) {
            int exp = reader.decimalValue().scale();
            assertTrue(exp * (-1) >= range.get(0) && exp * (-1) <= range.get(1));
        }
    }

    /**
     * Assert the range of coefficient digits number of generated Ion decimals is conform with the expected range.
     * @throws Exception if error occurs when executing Ion data generator.
     */
    @Test
    public void testGeneratedDecimalCoefficientRange() throws Exception {
        Map<String, Object> optionsMap = dataGeneratorTest.testCasesGenerator("3000","decimal", "[1,1114111]", "[1,4]", "tes3.10n", "2021T", "[ion_text]", "[-13,-2]");
        GeneratorOptions.executeGenerator(optionsMap);
        IonReader reader = IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(optionsMap.get("<output_file>").toString())));
        List<Integer> range = WriteRandomIonValues.rangeParser(optionsMap.get("--decimal-coefficient-digit-range").toString());

        while (reader.next() != null) {
            BigInteger coefficient = reader.decimalValue().unscaledValue();
            double factor = Math.log(2) / Math.log(10);
            int digitCount = (int) (factor * coefficient.bitLength() + 1);
            if (BigInteger.TEN.pow(digitCount - 1).compareTo(coefficient) > 0) {
                digitCount = digitCount - 1;
            }
            assertTrue(digitCount >= range.get(0) && digitCount <= range.get(1));
        }
    }

    /**
     * Assert the format of generated file is conform with the expected format [ion_binary|ion_text].
     * @throws Exception if error occurs when executing Ion data generator.
     */
    @Test
    public void testGeneratedFormat() throws Exception {
        Map<String, Object> optionsMap = dataGeneratorTest.testCasesGenerator("500","symbol", "[1,1114111]", "[1,4]", "test11.ion", "2021T", "[ion_text]", "[-13,-2]");

        GeneratorOptions.executeGenerator(optionsMap);
        String format = optionsMap.get("--format").toString().substring(1, optionsMap.get("--format").toString().length() - 1);

        Path path = Paths.get(optionsMap.get("<output_file>").toString());
        byte[] buffer = Files.readAllBytes(path);
        assertEquals(Format.valueOf(format.toUpperCase()) == Format.ION_BINARY, IonStreamUtils.isIonBinary(buffer));
    }

    /**
     * Assert the unicode code point range of the character which constructed the generated Ion string is conform with the expect range.
     * @throws Exception if error occurs when executing Ion data generator.
     */
    @Test
    public void testGeneratedStringUniCodeRange() throws Exception {
        Map<String, Object> optionsMap = dataGeneratorTest.testCasesGenerator("500","string", "[0,135]", "[1,4]", "test5.ion", "2021T", "[ion_text]", "[-13,-2]");

        GeneratorOptions.executeGenerator(optionsMap);
        IonReader reader = IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(optionsMap.get("<output_file>").toString())));
        List<Integer> range = WriteRandomIonValues.rangeParser(optionsMap.get("--text-code-point-range").toString());

        while (reader.next() != null) {
            String str = reader.stringValue();
            for (int i = 0; i < str.length(); i++) {
                int codePoint = Character.codePointAt(str, i);
                int charCount = Character.charCount(codePoint);
                //UTF characters may use more than 1 char to be represented
                if (charCount == 2) {
                    i++;
                }
                assertTrue(codePoint >= range.get(0) && codePoint <= range.get(1));
            }
        }
    }

    /**
     * Assert the generated timestamps is follow the precision and proportion of the given timestamp template.
     * @throws Exception if error occurs when executing Ion data generator.
     */
    @Test
    public void testGeneratedTimestampTemplateFormat() throws Exception{
        Map<String, Object> optionsMap = dataGeneratorTest.testCasesGenerator("500","timestamp", "[1,135]", "[1,4]", "test6.ion", "2013T", "[ion_text]", "[-13,-2]");

        ArrayList timestampsList = new ArrayList(Arrays.asList(optionsMap.get("--timestamps-template").toString().split(",")));

        GeneratorOptions.executeGenerator(optionsMap);
        IonReader reader = IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(optionsMap.get("<output_file>").toString())));
        reader.next();

        while (reader.isNullValue()) {
            for ( int i = 0; i < timestampsList.size(); i++) {
                IonReaderBuilder readerBuilder = IonReaderBuilder.standard();
                IonReader readerTemplate = readerBuilder.build(timestampsList.get(i).toString());
                readerTemplate.next();
                Timestamp.Precision templatePrecision = readerTemplate.timestampValue().getPrecision();
                Timestamp.Precision currentPrecision = reader.timestampValue().getPrecision();
                Integer templateOffset = readerTemplate.timestampValue().getLocalOffset();
                Integer currentOffset = reader.timestampValue().getLocalOffset();
                assertTrue( templatePrecision == currentPrecision);
                if (currentOffset == null || currentOffset == 0) {
                    assertTrue(currentOffset == templateOffset);
                } else {
                    assertEquals(currentOffset >= -1439 && currentOffset <= 1439, templateOffset >= -1439 && templateOffset <= 1439);
                }
                if (currentPrecision == Timestamp.Precision.SECOND) {
                    assertTrue(reader.timestampValue().getDecimalSecond().scale() == readerTemplate.timestampValue().getDecimalSecond().scale());
                }
                reader.next();
            }
        }
    }

    /**
     * Assert the generated data size in bytes has an 10% difference with the expected size, this range is not available for Ion symbol, because the size of symbol is predicted.
     * @throws Exception if error occurs when executing Ion data generator
     */
    @Test
    public void testSizeOfGeneratedData() throws Exception {
        Map<String, Object> optionsMap = dataGeneratorTest.testCasesGenerator("5000","clob", "[1,135]", "[1,4]", "symbol.ion", "2021T,7787T", "[ion_text]", "[-13,-2]");
        GeneratorOptions.executeGenerator(optionsMap);
        int expectedSize = Integer.parseInt(optionsMap.get("--data-size").toString());
        String fileName = optionsMap.get("<output_file>").toString();
        Path filePath = Paths.get(fileName);
        FileChannel fileChannel;
        fileChannel = FileChannel.open(filePath);
        int fileSize = (int)fileChannel.size();
        fileChannel.close();
        int difference = Math.abs(expectedSize - fileSize);
        assertTrue(difference <= 0.1 * expectedSize);
    }
}
