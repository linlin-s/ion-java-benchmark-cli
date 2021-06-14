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

import com.amazon.ion.Decimal;
//import com.amazon.ion.IonBufferConfiguration;
//import com.amazon.ion.IonBufferEventHandler;
import com.amazon.ion.IonReader;
import com.amazon.ion.IonType;
import com.amazon.ion.IonWriter;
import com.amazon.ion.Timestamp;
import com.amazon.ion.system.IonBinaryWriterBuilder;
import com.amazon.ion.system.IonReaderBuilder;
import com.amazon.ion.system.IonTextWriterBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

class WriteRandomIonValues {

    // This method helps specify which writer should be used based on the format
    // option [ion_binary/ion_text]
    private static IonWriter formatWriter(String format, OutputStream out) {
        IonWriter writer;
        Format formatName = Format.valueOf(format.toUpperCase());
        switch (formatName) {
            case ION_BINARY:
                writer = IonBinaryWriterBuilder.standard().build(out);
                break;
            case ION_TEXT:
                writer = IonTextWriterBuilder.standard().build(out);
                break;
            default:
                throw new IllegalStateException();
        }
        return writer;
    }
    // Use ion-java parser to parse provided range
    private static int [] rangeParser(String range) {
        IonReaderBuilder readerBuilder = IonReaderBuilder.standard();
        IonReader reader = readerBuilder.build(range);
        reader.next();                             
        reader.stepIn(); 
        reader.next();
        int value1 = reader.intValue();
        reader.next();
        int value2 = reader.intValue();
        int [] result = new int[] {value1,value2};
        reader.stepOut();   
        return result;
    }

    public static void writeRandomStrings(int size, String path, String codePointRange, String format) throws Exception {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        File file = new File(path);

        int [] pointRange = WriteRandomIonValues.rangeParser(codePointRange);
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                IonWriter writer = WriteRandomIonValues.formatWriter(format, out)) {
            Random random = new Random();
            // Target about 100MB of data. Strings will average around 20 bytes (2 bytes on
            // average for each code point,
            // average of 10 code points per string).
            for (int i = 0; i < (size / 20); i++) {
                int length = random.nextInt(20);
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < length; j++) {
                    int codePoint;
                    int type;
                    do {
                        codePoint = random.nextInt(pointRange[1] - pointRange[0]+1) + pointRange[0];
                        type = Character.getType(codePoint);
                    } while (type == Character.PRIVATE_USE || type == Character.SURROGATE
                            || type == Character.UNASSIGNED);
                    sb.appendCodePoint(codePoint);
                }
                writer.writeString(sb.toString());
            }
        }
        System.out.println(fileName + " generated successfully ! ");
        if (fileName.equals(path)) {
            System.out.println("Generated data is under the current directory");
        } else {
            System.out.println("File path: " + path);
        }
    }

    public static void writeRandomDecimals(int size, String path, String format, String expRange, String coefficientDigit) throws Exception {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        File file = new File(path);
        
        int[] expValRange = WriteRandomIonValues.rangeParser(expRange);
        int[] coefficientDigitRange = WriteRandomIonValues.rangeParser(coefficientDigit);

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                IonWriter writer = WriteRandomIonValues.formatWriter(format, out)) {
            Random random = new Random();
            while (size > 0) {
                int exp = random.nextInt((expValRange[1] - expValRange[0]) + 1) + expValRange[0];
                int randDigits = random.nextInt((coefficientDigitRange[1] - coefficientDigitRange[0]) + 1) + coefficientDigitRange[0];

                StringBuilder rs = new StringBuilder();

                for (int digit = 0; digit < randDigits; digit++) {
                    rs.append(random.nextInt(10));
                }
                BigDecimal coefficient = new BigDecimal(rs.toString());
                writer.writeDecimal(coefficient.scaleByPowerOfTen(exp));
                // Calculate the size of current decimal, 4 bytes can store 9 digits number, and the decimal point == 1 byte, totalDigits is the digit number of the whole decimal.
                int totalDigits;
                if (Math.abs(exp) > Math.abs(randDigits)){
                    totalDigits = Math.abs(exp);
                } else {
                    totalDigits = Math.abs(randDigits);
                }

                int currentSize;
                if (totalDigits % 2 == 0) {
                    currentSize = (totalDigits + 3) / 2;
                } else {
                    currentSize = (totalDigits + 4) / 2;
                }
                size -= currentSize;
            }
            System.out.println(fileName + " generated successfully ! ");
            if (fileName.equals(path)) {
                System.out.println("Generated data is under the current directory");
            } else {
                System.out.println("File path: " + path);
            }
        }
    }

    public static void writeRandomInts(int size, String format, String path) throws Exception {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        File file = new File(path);

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                IonWriter writer = WriteRandomIonValues.formatWriter(format, out)) {
            Random random = new Random();
            // Target about 100MB of data. Ints will average around 8 bytes, and we're
            // writing 4 per iteration.
            for (int i = 0; i < (size / 8 / 3); i++) {
                writer.writeInt(random.nextInt(1024));
                writer.writeInt(random.nextInt());
                long longValue = random.nextLong();
                writer.writeInt(longValue);
                writer.writeInt(BigInteger.valueOf(longValue).multiply(BigInteger.TEN));
            }
        }
        System.out.println(fileName + " generated successfully ! ");
        if (fileName.equals(path)) {
            System.out.println("Generated data is under the current directory");
        } else {
            System.out.println("File path: " + path);
        }
    }

    public static void writeRandomFloats(int size, String format, String path) throws Exception {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        File file = new File(path);
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                IonWriter writer = WriteRandomIonValues.formatWriter(format, out)) {
            Random random = new Random();
            // Target about 100MB of data. Floats will average at 7 bytes, and we're writing
            // 2 per iteration.
            for (int i = 0; i < (size / 7 / 2); i++) {
                writer.writeFloat(Double.longBitsToDouble(random.nextLong()));
                writer.writeFloat(Float.intBitsToFloat(random.nextInt()));
            }
        }
        System.out.println(fileName + " generated successfully ! ");
        if (fileName.equals(path)) {
            System.out.println("Generated data is under the current directory");
        } else {
            System.out.println("File path: " + path);
        }
    }

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

    private static Integer randomLocalOffset(Random random) {
        // Offsets are in minutes, [-23:59, 23:59], i.e. [-1439, 1439].
        // The most common offset is Z (00:00), while unknown (-00:00) may also be
        // common.
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

    private static BigDecimal randomSecondWithFraction(Random rand, int scale) {
        int second = rand.nextInt(60);
        if (scale != 0) {
            StringBuilder coefficientStr = new StringBuilder();
            for (int digit = 0; digit < scale; digit++) {
                coefficientStr.append(rand.nextInt(10));
            }
            BigDecimal fractional = new BigDecimal(coefficientStr.toString());
            BigDecimal fractionalSecond = fractional.scaleByPowerOfTen(scale*(-1));
            return fractionalSecond.add(BigDecimal.valueOf(second));
        } else {
            return BigDecimal.valueOf(second);
        }
    }

    public static void writeRandomTimestamps(int size, String path, String timestampTemplate, String format) throws Exception {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        File file = new File(path);

        try (
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                IonWriter writer = WriteRandomIonValues.formatWriter(format,out)) {
            // Target about 100MB of data. Timestamps will average around 7 bytes.
//            Timestamp.Precision[] precisions = Timestamp.Precision.values();
            if (timestampTemplate != "random"){
                while (size > 0) {
                    IonReaderBuilder readerBuilder = IonReaderBuilder.standard();
                    IonReader reader = readerBuilder.build(timestampTemplate);
                    reader.next();
                    reader.stepIn();
                    reader.next();
                    int count = 0;
                    while (reader.isNullValue() == false){
                        count += 1;
                        Timestamp value = reader.timestampValue();
                        Timestamp.Precision precision = value.getPrecision();
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
                                timestamp = Timestamp.forDay(random.nextInt(9998) + 1, random.nextInt(12) + 1,
                                        random.nextInt(28) + 1 // Use max 28 for simplicity. Not including up to 31 is not going to
                                        // affect the measurement.
                                );
                                break;
                            case MINUTE:
                                timestamp = Timestamp.forMinute(random.nextInt(9998) + 1, random.nextInt(12) + 1,
                                        random.nextInt(28) + 1, // Use max 28 for simplicity. Not including up to 31 is not going to
                                        // affect the measurement.
                                        random.nextInt(24), random.nextInt(60), randomLocalOffset(random));
                                break;
                            case SECOND:
                                //added
                                BigDecimal secondValue = value.getDecimalSecond();
                                int scale = secondValue.scale();
                                timestamp = Timestamp.forSecond(random.nextInt(9998) + 1, random.nextInt(12) + 1,
                                        random.nextInt(28) + 1, // Use max 28 for simplicity. Not including up to 31 is not going to
                                        // affect the measurement.
                                        random.nextInt(24), random.nextInt(60), randomSecondWithFraction(random,scale), randomLocalOffset(random));
                                break;
                            default:
                                throw new IllegalStateException();
                        }
                        writer.writeTimestamp(timestamp);
                        if (reader.next() == null){
                            break;
                        }
                    }
                    int currentSize = 7 * count;
                    size -= currentSize;
                }
            } else {
                Random random = new Random();
                // Target about 100MB of data. Timestamps will average around 7 bytes.
                Timestamp.Precision[] precisions = Timestamp.Precision.values();
                for (int i = 0; i < (size / 7); i++) {
                    Timestamp.Precision precision = precisions[random.nextInt(precisions.length)];
                    Timestamp timestamp;
                    int randomScale = random.nextInt(20);
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
                            timestamp = Timestamp.forMinute(
                                    random.nextInt(9998) + 1,
                                    random.nextInt(12) + 1,
                                    random.nextInt(28) + 1, // Use max 28 for simplicity. Not including up to 31 is not going to affect the measurement.
                                    random.nextInt(24),
                                    random.nextInt(60),
                                    randomLocalOffset(random)
                            );
                            break;
                        case SECOND:
                            timestamp = Timestamp.forSecond(
                                    random.nextInt(9998) + 1,
                                    random.nextInt(12) + 1,
                                    random.nextInt(28) + 1, // Use max 28 for simplicity. Not including up to 31 is not going to affect the measurement.
                                    random.nextInt(24),
                                    random.nextInt(60),
                                    random.nextInt(60),
                                    randomLocalOffset(random)
                            );
                            break;
                        case FRACTION:
                            timestamp = Timestamp.forSecond(
                                    random.nextInt(9998) + 1,
                                    random.nextInt(12) + 1,
                                    random.nextInt(28) + 1, // Use max 28 for simplicity. Not including up to 31 is not going to affect the measurement.
                                    random.nextInt(24),
                                    random.nextInt(60),
                                    randomSecondWithFraction(random,randomScale),
                                    randomLocalOffset(random)
                            );
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    writer.writeTimestamp(timestamp);
                }
            }
            System.out.println(fileName + " generated successfully ! ");
            if (fileName.equals(path)) {
                System.out.println("Generated data is under the current directory");
            } else {
                System.out.println("File path: " + path);
            }
        }
    }

    public static void writeRandomLobs(int size, String type, String format, String path) throws IOException {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        File file = new File(path);

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                IonWriter writer = WriteRandomIonValues.formatWriter(format, out)) {
            Random random = new Random();
            // Target about 100MB of data. Blobs will average around 259 bytes.
            for (int i = 0; i < size / 259; i++) {
                byte[] randomBytes = new byte[random.nextInt(512)];
                random.nextBytes(randomBytes);
                if (type == "blob") {
                    writer.writeBlob(randomBytes);
                } else {
                    writer.writeClob(randomBytes);
                }
            }
        }
        System.out.println(fileName + " generated successfully ! ");
        if (fileName.equals(path)) {
            System.out.println("Generated data is under the current directory");
        } else {
            System.out.println("File path: " + path);
        }
    }

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

    public static void writeRandomSymbolValues(int size, String format, String path) throws IOException {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        File file = new File(path);
        List<String> symbols = new ArrayList<>(500);
        Random random = new Random();
        for (int i = 0; i < size; i++) {
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
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                IonWriter writer = WriteRandomIonValues.formatWriter(format, out)) {
            // Target 100MB of data. Symbol values will average 2 bytes each.
            for (int i = 0; i < size / 2; i++) {
                writer.writeSymbol(symbols.get(random.nextInt(500)));
            }
        }
        System.out.println(fileName + " generated successfully ! ");
        if (fileName.equals(path)) {
            System.out.println("Generated data is under the current directory");
        } else {
            System.out.println("File path: " + path);
        }
    }
}