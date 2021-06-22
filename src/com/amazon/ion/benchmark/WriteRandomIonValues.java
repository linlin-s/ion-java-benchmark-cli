package com.amazon.ion.benchmark;

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import com.amazon.ion.IonReader;
import com.amazon.ion.IonType;
import com.amazon.ion.IonWriter;
import com.amazon.ion.Timestamp;
import com.amazon.ion.system.IonBinaryWriterBuilder;
import com.amazon.ion.system.IonReaderBuilder;
import com.amazon.ion.system.IonTextWriterBuilder;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;



/**
 * Generate specific scalar type of Ion data randomly, for some specific type, e.g. String, Decimal, Timestamp, users can put specifications on these types of Ion data.
 */
class WriteRandomIonValues {

    /**
     * Build up the writer based on the provided format (ion_text|ion_binary)
     * @param format the option to decide which writer to be constructed.
     * @param file the generated file which contains specified Ion data.
     * @return the writer which conforms with the required format.
     * @throws Exception if an error occurs while creating a file output stream.
     */
    private static IonWriter formatWriter(String format, File file) throws Exception {
        IonWriter writer;
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        Format formatName = Format.valueOf(format.toUpperCase());

        switch (formatName) {
            case ION_BINARY:
                writer = IonBinaryWriterBuilder.standard().withLocalSymbolTableAppendEnabled().build(out);
                break;
            case ION_TEXT:
                writer = IonTextWriterBuilder.standard().build(out);
                break;
            default:
                throw new IllegalStateException("Please input the format ion_text or ion_binary");
        }
        return writer;
    }

    /**
     * Use Ion-java parser to parse the data provided in the options which specify the range of data.
     * @param range the range needed to be parsed, normally in the format of "[Integer, Integer]"
     * @return a list of Integer which will be extracted in the following executions.
     */
    public static List<Integer> rangeParser(String range) {
        IonReaderBuilder readerBuilder = IonReaderBuilder.standard();
        IonReader reader = readerBuilder.build(range);
        if (reader.next() != IonType.LIST) throw new IllegalStateException("Please provide a list type");

        reader.stepIn();
        List<Integer> result = new ArrayList<>();
        while (reader.next() != null) {
            if (reader.getType() != IonType.INT) throw new IllegalStateException("Please put integer elements inside of the list");
            int value = reader.intValue();
            result.add(value);
        }
        reader.stepOut();

        if (reader.next() != null) throw new IllegalStateException("Only one list is accepted");
        if (result.get(0) > result.get(1)) throw new IllegalStateException("The value of the lower bound should be smaller than the upper bound");
        if (result.size() != 2) throw new IllegalStateException("Please put two integers inside of the list");
        return result;
    }

    /**
     * Print the successfully generated data notification which includes the file path information.
     * @param path identifies the output file path.
     */
    private static void printInfo(String path) {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        System.out.println(fileName + " generated successfully ! ");
        if (fileName.equals(path)) {
            System.out.println("Generated data is under the current directory");
        } else {
            System.out.println("File path: " + path);
        }
    }

    /**
     * Write random Ion strings into target file, and all data conform with the specifications provided by the options if these are provided. Otherwise, this method will generate Ion string data randomly.
     * @param size specifies the size in bytes of the generated file.
     * @param path the destination of the generated file.
     * @param codePointRange provides the range of Unicode code point of characters which construct the Ion string.
     * @param format the format of output file (ion_binary | ion_text).
     * @throws Exception if an error occurs when building up the writer.
     */
    public static void writeRandomStrings(int size, String path, String codePointRange, String format) throws Exception {
        File file = new File(path);
        IonWriter writer = WriteRandomIonValues.formatWriter(format, file);
        List<Integer> pointRange = WriteRandomIonValues.rangeParser(codePointRange);

        if (pointRange.get(0) < 0) throw new IllegalStateException("Please provide the valid range inside of [0, 1114111]");
        if (pointRange.get(1) > Character.MAX_CODE_POINT) throw new IllegalStateException("Please provide the valid range inside of [0, 1114111]");

        int currentSize = 0;
        int count = 0;

        while(currentSize <= size) {
            // Determine how many strings should be write before the writer.flush()
            while (currentSize <= 0.05 * size) {
                writer.writeString(ConstructIonData.constructString(pointRange));
                count += 1;
                writer.flush();
                currentSize = (int)file.length();
            }
            for (int j = 0; j < count; j++) {
                writer.writeString(ConstructIonData.constructString(pointRange));
            }
            writer.flush();
            currentSize = (int)file.length();
        }
        writer.close();
        WriteRandomIonValues.printInfo(path);
    }

    /**
     * Write random Ion decimals into target file. If the options which specify the range of exponent and digits number of coefficient are provided, the generated data will conform with the specifications.
     * Otherwise, this method will generated the random decimals based on the default range.
     * @param size specifies the size in bytes of the generated file.
     * @param path the destination of the generated file.
     * @param format the format of output file (ion_binary | ion_text).
     * @param expRange the range of exponent when the decimal represented in coefficient * 10 ^ exponent.
     * @param coefficientDigit the range of digit number of coefficient when the decimal represented in coefficient * 10 ^ exponent.
     * @throws Exception if an error occurs when building up the writer.
     */
    public static void writeRandomDecimals(int size, String path, String format, String expRange, String coefficientDigit) throws Exception {
        File file = new File(path);
        IonWriter writer = WriteRandomIonValues.formatWriter(format, file);
        List<Integer> expValRange = WriteRandomIonValues.rangeParser(expRange);
        List<Integer> coefficientDigitRange = WriteRandomIonValues.rangeParser(coefficientDigit);

        if (Math.max(Math.abs(expValRange.get(0)), Math.abs(expValRange.get(1))) > 32) throw new IllegalStateException("Please provide the  absolute value of range no more than 32.");
        if (coefficientDigitRange.get(0) <= 0 ) throw new IllegalStateException ("The lower bound of the range should start from 1.");
        if (coefficientDigitRange.get(1) > 32) throw new IllegalStateException ("The value of upper bound of coefficient digits number shouldn't more than 32.");

        int currentSize = 0;
        int count = 0;

        while(currentSize <= size) {
            // Determine how many strings should be write before the writer.flush()
            while (currentSize <= 0.05 * size) {
                writer.writeDecimal(ConstructIonData.constructDecimal(expRange, coefficientDigit));
                count += 1;
                writer.flush();
                currentSize = (int)file.length();
            }
            for (int j = 0; j < count; j++) {
                writer.writeDecimal(ConstructIonData.constructDecimal(expRange, coefficientDigit));
            }
            writer.flush();
            currentSize = (int)file.length();
        }
        writer.close();
        WriteRandomIonValues.printInfo(path);
    }

    /**
     * Write random Ion integers into target file, and all data conform with the specifications provided by the options, e.g. size, format and the output file path.
     * @param size specifies the size in bytes of the generated file.
     * @param path the destination of the generated file.
     * @param format the format of output file (ion_binary | ion_text).
     * @throws Exception if an error occurs when building up the writer.
     */
    public static void writeRandomInts(int size, String format, String path) throws Exception {
        File file = new File(path);
        IonWriter writer = WriteRandomIonValues.formatWriter(format, file);
        Random random = new Random();
        int currentSize = 0;
        int count = 0;

        while(currentSize <= size) {
            // Determine how many strings should be write before the writer.flush()
            while (currentSize <= 0.05 * size) {
                ConstructIonData.constructInt(writer, random);
                count += 1;
                writer.flush();
                currentSize = (int)file.length();
            }
            for (int j = 0; j < count; j++) {
                ConstructIonData.constructInt(writer, random);
            }
            writer.flush();
            currentSize = (int)file.length();
        }
        writer.close();
        WriteRandomIonValues.printInfo(path);
    }


    /**
     * Write random Ion floats into target file, and all data conform with the specifications provided by the options, e.g. size, format and the output file path.
     * @param size specifies the size in bytes of the generated file.
     * @param path the destination of the generated file.
     * @param format the format of output file (ion_binary | ion_text).
     * @throws Exception if an error occurs when building up the writer.
     */
    public static void writeRandomFloats(int size, String format, String path) throws Exception {
        File file = new File(path);
        IonWriter writer = WriteRandomIonValues.formatWriter(format, file);
        Random random = new Random();
        int currentSize = 0;
        int count = 0;
        while(currentSize <= size) {
            // Determine how many strings should be write before the writer.flush()
            while (currentSize <= 0.05 * size) {
                ConstructIonData.constructFloat(writer, random);
                count += 1;
                writer.flush();
                currentSize = (int)file.length();
            }
            for (int j = 0; j < count; j++) {
                ConstructIonData.constructFloat(writer, random);
            }
            writer.flush();
            currentSize = (int)file.length();
        }
        writer.close();
        WriteRandomIonValues.printInfo(path);
    }

    /**
     *This method is not available now
     */
    private static void writeListOfRandomFloats() throws Exception {
        File file = new File("manyLargeListsOfRandomFloats.10n");
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                IonWriter writer = IonBinaryWriterBuilder.standard().withFloatBinary32Enabled().build(out)) {
            Random random = new Random();
            for (int j = 0; j < 100; j++) {
                writer.stepIn(IonType.LIST);
                // Target about 1MB of data. Floats will average at 7 bytes, and we're writing 2
                // per iteration.
                for (int i = 0; i < (1_000_000 / 7 / 2); i++) {
                    writer.writeFloat(Double.longBitsToDouble(random.nextLong()));
                    writer.writeFloat(Float.intBitsToFloat(random.nextInt()));
                }
                writer.stepOut();
            }
        }
        System.out.println("Finished writing floats. Verifying.");
        try (IonReader reader = IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(file)))) {
            while (reader.next() != null) {
                reader.stepIn();
                while (reader.next() != null) {
                    if (reader.getType() != IonType.FLOAT) {
                        throw new IllegalStateException("Found non-float");
                    }
                    double value = reader.doubleValue();
                }
                reader.stepOut();
            }
        }
        System.out.println("Done. Size: " + file.length());
    }

    /**
     * Generate the random local offset conform with the format provided by the timestamp template.
     * @param random is the random number generator.
     * @param offset is the offset of the current timestamp in template.
     * @return the offset which is conform with the provided template timestamp [Z(+00:00) | -00:00 | random offset].
     */
    private static Integer randomLocalOffset(Random random, Object offset) {
        // Offsets are in minutes, [-23:59, 23:59], i.e. [-1439, 1439].
        // The most common offset is Z (00:00), while unknown (-00:00) may also be common.
        Integer offsetMinutes = random.nextInt(2878) - 1439;
        if (offset == null) {
            offsetMinutes = null;
        } else if ((int)offset == 0) {
            offsetMinutes = 0;
        }
        return offsetMinutes;
    }

    /**
     * Generate random offset without any specification
     * @param random is the random number generator.
     * @return random offset [Z(+00:00) | -00:00 | random offset].
     */
    private static Integer localOffset(Random random) {
        // Offsets are in minutes, [-23:59, 23:59], i.e. [-1439, 1439].
        // The most common offset is Z (00:00), while unknown (-00:00) may also be common.
        Integer offsetMinutes = random.nextInt(6000) - 2000;
        if (offsetMinutes > 1439) {
            // This means about 43% of timestamps will have offset Z (UTC).
            offsetMinutes = 0;
        } else if (offsetMinutes < -1439) {
            // This means about 9% of timestamps will have unknown offset (-00:00).
            offsetMinutes = null;
        }
        return offsetMinutes;
    }

    /**
     * Generate random fractional second which the precision conforms with the precision of the second in template timestamp.
     * @param random is the random number generator.
     * @param scale is the the scale of the decimal second in the current template timestamp.
     * @return a random timestamp second in a fractional format which conforms with the current template timestamp.
     */
    private static BigDecimal randomSecondWithFraction(Random random, int scale) {
        int second = random.nextInt(60);
        if (scale != 0) {
            StringBuilder coefficientStr = new StringBuilder();
            for (int digit = 0; digit < scale; digit++) {
                coefficientStr.append(random.nextInt(10));
            }
            BigDecimal fractional = new BigDecimal(coefficientStr.toString());
            BigDecimal fractionalSecond = fractional.scaleByPowerOfTen(scale * (-1));
            return fractionalSecond.add(BigDecimal.valueOf(second));
        } else {
            return BigDecimal.valueOf(second);
        }
    }

    /**
     * Write random Ion timestamps into target file, and all data conform with the specifications provided by the options.
     * If timestamps template provided, the generated timestamps will be conformed with the precision and portion of the template. Otherwise, the data will be generated randomly.
     * @param size specifies the size in bytes of the generated file.
     * @param path the destination of the generated file.
     * @param format the format of output file (ion_binary | ion_text).
     * @param timestampTemplate is a string which provides a series of template timestamps which data generating process will follow with.
     * @throws Exception if an error occurs when building up the writer.
     */
    public static void writeRandomTimestamps(int size, String path, Object timestampTemplate, String format) throws Exception {
        File file = new File(path);
        IonWriter writer = WriteRandomIonValues.formatWriter(format, file);
        int currentSize = 0;
        int count = 0;
        while (currentSize <= size) {
            while (currentSize < 0.05 * size) {
                ConstructIonData.constructTimestamp(timestampTemplate, writer);
                count += 1;
                writer.flush();
                currentSize = (int) file.length();
            }
            for (int j = 0; j < count; j++) {
                ConstructIonData.constructTimestamp(timestampTemplate, writer);
            }
            writer.flush();
            currentSize = (int)file.length();
        }
        writer.close();
        WriteRandomIonValues.printInfo(path);
    }

    /**
     * Execute the writing timestamp data process based on the precision provided by the template timestamps.
     * @param precision is the precision of current template timestamp.
     * @param writer writes Ion timestamp data.
     * @param value is the current timestamp in the provided template.
     * @throws IOException if an error occurs when writing timestamp value.
     */
    public static void writeTimestamp(Timestamp.Precision precision, IonWriter writer, Timestamp value) throws IOException {
        Timestamp timestamp;
        Random random = new Random();

        switch (precision) {
            case YEAR:
                timestamp = Timestamp.forYear(random.nextInt(9998) + 1);
                break;
            case MONTH:
                timestamp = Timestamp.forMonth(random.nextInt(9998) + 1, random.nextInt(12) + 1);
                break;
            case DAY:
                timestamp = Timestamp.forDay(
                        random.nextInt(9998) + 1,
                        random.nextInt(12) + 1,
                        random.nextInt(28) + 1 // Use max 28 for simplicity. Not including up to 31 is not going to affect the measurement.
                );
                break;
            case MINUTE:
                if (value == null) {
                    timestamp = Timestamp.forMinute(
                            random.nextInt(9998) + 1,
                            random.nextInt(12) + 1,
                            random.nextInt(28) + 1, // Use max 28 for simplicity. Not including up to 31 is not going to affect the measurement.
                            random.nextInt(24),
                            random.nextInt(60),
                            localOffset(random)
                    );
                } else {
                    timestamp = Timestamp.forMinute(random.nextInt(9998) + 1, random.nextInt(12) + 1,
                            random.nextInt(28) + 1, // Use max 28 for simplicity. Not including up to 31 is not going to
                            // affect the measurement.
                            random.nextInt(24), random.nextInt(60), randomLocalOffset(random, value.getLocalOffset()));
                }
                break;

            case SECOND:
                if (value == null) {
                    timestamp = Timestamp.forSecond(
                            random.nextInt(9998) + 1,
                            random.nextInt(12) + 1,
                            random.nextInt(28) + 1, // Use max 28 for simplicity. Not including up to 31 is not going to affect the measurement.
                            random.nextInt(24),
                            random.nextInt(60),
                            random.nextInt(60),
                            localOffset(random)
                    );
                    break;
                } else {
                    timestamp = Timestamp.forSecond(random.nextInt(9998) + 1, random.nextInt(12) + 1,
                            random.nextInt(28) + 1, // Use max 28 for simplicity. Not including up to 31 is not going to
                            // affect the measurement.
                            random.nextInt(24), random.nextInt(60), randomSecondWithFraction(random,value.getDecimalSecond().scale()), randomLocalOffset(random, value.getLocalOffset()));
                    break;
                }
            case FRACTION:
                int scale = random.nextInt(20);
                timestamp = Timestamp.forSecond(
                        random.nextInt(9998) + 1,
                        random.nextInt(12) + 1,
                        random.nextInt(28) + 1, // Use max 28 for simplicity. Not including up to 31 is not going to affect the measurement.
                        random.nextInt(24),
                        random.nextInt(60),
                        randomSecondWithFraction(random,scale),
                        localOffset(random)
                );
                break;
            default:
                throw new IllegalStateException();
        }
        writer.writeTimestamp(timestamp);
    }

    /**
     * Write random Ion blobs/clobs into target file, and all data conform with the specifications provided by the options, e.g. size, format, type(blob/clob) and the output file path.
     * @param size specifies the size in bytes of the generated file.
     * @param path the destination of the generated file.
     * @param format the format of output file (ion_binary | ion_text).
     * @param type determines which type of data will be generated [blob | clob].
     * @throws Exception if an error occurs when building up the writer.
    */
    public static void writeRandomLobs(int size, String type, String format, String path) throws Exception {
        File file = new File(path);
        IonWriter writer = WriteRandomIonValues.formatWriter(format, file);
        Random random = new Random();
        int currentSize = 0;
        while (currentSize <= size) {
            ConstructIonData.constructLobs(random, type, writer);
            writer.flush();
            currentSize = (int) file.length();
        }
        writer.close();
        WriteRandomIonValues.printInfo(path);
    }


    /**
     * This method is not available now.
     * @throws Exception
     */
    private static void writeRandomAnnotatedFloats() throws Exception {
        File file = new File("randomAnnotatedFloats.10n");
        List<String> annotations = new ArrayList<>(500);
        Random random = new Random();
        for (int i = 0; i < 500; i++) {
            int length = random.nextInt(20);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; j++) {
                int codePoint;
                int type;
                do {
                    codePoint = random.nextInt(Character.MAX_CODE_POINT);
                    type = Character.getType(codePoint);
                } while (type == Character.PRIVATE_USE || type == Character.SURROGATE || type == Character.UNASSIGNED);
                sb.appendCodePoint(codePoint);
            }
            annotations.add(sb.toString());
        }
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                IonWriter writer = IonBinaryWriterBuilder.standard().build(out)) {
            // Target about 100MB of data. Annotated floats will average around 14 bytes.
            for (int i = 0; i < (100_000_000 / 14); i++) {
                // 60% of values will have 1 annotation; 40% will have 2 or 3.
                int numberOfAnnotations = random.nextInt(5) + 1;
                if (numberOfAnnotations > 3) {
                    numberOfAnnotations = 1;
                }
                for (int j = 0; j < numberOfAnnotations; j++) {
                    writer.addTypeAnnotation(annotations.get(random.nextInt(500)));

                }
                writer.writeFloat(Double.longBitsToDouble(random.nextLong()));

            }
        }
        System.out.println("Finished writing floats. Verifying.");
        try (IonReader reader = IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(file)))) {
            int i = 0;
            while (reader.next() != null) {
                if (reader.getType() != IonType.FLOAT) {
                    throw new IllegalStateException("Found non-float");
                }
                double value = reader.doubleValue();
                if (i++ < 100) {
                    System.out.print(Arrays.toString(reader.getTypeAnnotations()) + " ");
                    System.out.println(value);
                }
            }
        }
        System.out.println("Done. Size: " + file.length());
    }

    /**
     * Write random Ion symbols into target file, and all data conform with the specifications provided by the options, e.g. size, format and the output file path.
     * @param size specifies the size in bytes of the generated file.
     * @param path the destination of the generated file.
     * @param format the format of output file (ion_binary | ion_text).
     * @throws Exception if an error occurs when building up the writer.
     */
    public static void writeRandomSymbolValues(int size, String format, String path) throws Exception {
        File file = new File(path);
        IonWriter writer = WriteRandomIonValues.formatWriter(format, file);
        List<String> symbols = new ArrayList<>(500);
        Random random = new Random();
        for (int i = 0; i < 500; i++) {
            int length = random.nextInt(20);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; j++) {
                int codePoint;
                int type;
                do {
                    codePoint = random.nextInt(Character.MAX_CODE_POINT);
                    type = Character.getType(codePoint);
                } while (type == Character.PRIVATE_USE || type == Character.SURROGATE || type == Character.UNASSIGNED);
                sb.appendCodePoint(codePoint);
            }
            symbols.add(sb.toString());
        }
        for (int i = 0; i < size / 2; i++) {
            writer.writeSymbol(symbols.get(random.nextInt(500)));
        }
        writer.close();
        WriteRandomIonValues.printInfo(path);
    }
}