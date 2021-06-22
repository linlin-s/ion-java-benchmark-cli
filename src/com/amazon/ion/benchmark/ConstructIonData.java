package com.amazon.ion.benchmark;

import com.amazon.ion.IonReader;
import com.amazon.ion.IonType;
import com.amazon.ion.IonWriter;
import com.amazon.ion.Timestamp;
import com.amazon.ion.system.IonReaderBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This class is used for constructing / writing Ion data which should be written into the target output file.
 */
public class ConstructIonData {

    /**
     * Construct string with the characters which unicode code point is inside of the provided range.
     * @param pointRange is unicode code point range which is in a List format.
     * @return constructed string.
     */
    public static String constructString(List<Integer> pointRange) {
        Random random = new Random();
        int length = random.nextInt(20);
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < length; j++) {
            int codePoint;
            int type;
            do {
                codePoint = random.nextInt(pointRange.get(1) - pointRange.get(0) + 1) + pointRange.get(0);
                type = Character.getType(codePoint);
            } while (type == Character.PRIVATE_USE || type == Character.SURROGATE || type == Character.UNASSIGNED);
            sb.appendCodePoint(codePoint);
        }
        return sb.toString();
    }

    /**
     * Construct the decimal based on the provided exponent range and coefficient digit number range
     * @param expRange the range of exponent when the decimal represented in coefficient * 10 ^ exponent.
     * @param coefficientDigit the range of digit number of coefficient when the decimal represented in coefficient * 10 ^ exponent.
     * @return the constructed decimal.
     */
    public static BigDecimal constructDecimal(String expRange, String coefficientDigit) {
        Random random = new Random();
        List<Integer> expValRange = WriteRandomIonValues.rangeParser(expRange);
        List<Integer> coefficientDigitRange = WriteRandomIonValues.rangeParser(coefficientDigit);
        int exp = random.nextInt((expValRange.get(1) - expValRange.get(0)) + 1) + expValRange.get(0);
        int randDigits = random.nextInt((coefficientDigitRange.get(1) - coefficientDigitRange.get(0)) + 1) + coefficientDigitRange.get(0);

        StringBuilder rs = new StringBuilder();
        for (int digit = 0; digit < randDigits; digit++) {
            rs.append(random.nextInt(9) + 1);
        }
        BigDecimal coefficient = new BigDecimal(rs.toString());
        return coefficient.scaleByPowerOfTen(exp);
    }

    /**
     * Write  random integers which composed by different length of data into the output file
     * @param writer is the Ionwriter which can write Ion data into the targer file.
     * @param random is the random number generator.
     * @throws IOException if error occurs during the writing process.
     */
    public static void constructInt(IonWriter writer, Random random) throws IOException {
        writer.writeInt(random.nextInt(1024));
        writer.writeInt(random.nextInt());
        long longValue = random.nextLong();
        writer.writeInt(longValue);
        writer.writeInt(BigInteger.valueOf(longValue).multiply(BigInteger.TEN));
    }

    /**
     * Write random Ion floats
     * @param writer is the Ionwriter which can write Ion data into the targer file.
     * @param random is the random number generator.
     * @throws IOException if error occurs during the writing process.
     */
    public static void constructFloat(IonWriter writer, Random random) throws IOException {
        writer.writeFloat(Double.longBitsToDouble(random.nextLong()));
        writer.writeFloat(Float.intBitsToFloat(random.nextInt()));
    }

    /**
     * Construct output timestamps follow the timestamp template or generate data randomly
     * @param timestampTemplate is a string which provides a series of template timestamps which data generating process will follow with.
     * @param writer writes specified timestamps into the target file.
     * @throws IOException if an error occurs when writing timestamps.
     */
    public static void constructTimestamp(Object timestampTemplate, IonWriter writer) throws IOException {
        if (timestampTemplate != null) {
            String timestampTemplateStr = timestampTemplate.toString();
            ArrayList timestampsList = new ArrayList(Arrays.asList(timestampTemplateStr.split(",")));

            for (Object o : timestampsList) {
                IonReaderBuilder readerBuilder = IonReaderBuilder.standard();
                IonReader reader = readerBuilder.build(o.toString());
                reader.next();
                Timestamp value;
                if (reader.getType() == IonType.TIMESTAMP) {
                    value = reader.timestampValue();
                } else {
                    throw new IllegalStateException("Please keep the input template in a legal timestamp format, and the templates should be quoted without brackets");
                }
                Timestamp.Precision precision = value.getPrecision();
                WriteRandomIonValues.writeTimestamp(precision, writer, value);
            }
        } else {
            Random random = new Random();
            Timestamp.Precision[] precisions = Timestamp.Precision.values();
            Timestamp.Precision precision = precisions[random.nextInt(precisions.length)];
            WriteRandomIonValues.writeTimestamp(precision, writer, null);
        }

    }

    /**
     * Execute the process of writing clob / blob data into target file.
     * @param random is the random number generator.
     * @param type determines which type of data will be generated [blob | clob].
     * @param writer writes Ion clob/blob data.
     * @throws IOException if an error occurs during the writing process
     */
    public static void constructLobs(Random random, String type, IonWriter writer) throws IOException {
        byte[] randomBytes = new byte[random.nextInt(512)];
        random.nextBytes(randomBytes);
        if (type.equals("blob")) {
            writer.writeBlob(randomBytes);
        } else {
            writer.writeClob(randomBytes);
        }
    }

}
