// This file is part of the RECODER library and protected by the LGPL.

package recoder.service;

import recoder.Service;
import recoder.abstraction.EnumConstant;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.Type;
import recoder.java.Expression;

/**
 * Constant folder to evaluate Java compile-time constants.
 * 
 * @author AL
 */
public interface ConstantEvaluator extends Service {

    /**
     * Encoding for relevant types - Java has no typeswitch instruction and
     * cannot easily afford to pass wrapper objects.
     */
    int BOOLEAN_TYPE = 0, BYTE_TYPE = 1, SHORT_TYPE = 2, CHAR_TYPE = 3, INT_TYPE = 4, LONG_TYPE = 5, FLOAT_TYPE = 6,
            DOUBLE_TYPE = 7, STRING_TYPE = 8, ENUM_CONSTANT_TYPE = 9;

    /**
     * Carrier for intermediate evaluation results. Explicit replacement for a
     * familiy of polymorphic types, helps to prevent expensive object
     * allocations.
     */
    final class EvaluationResult {

        private int type;

        private boolean booleanValue;

        /*
         * it might be possible to reuse most values by keeping everything
         * shorter than int in the intValue
         */
        private byte byteValue;

        private short shortValue;

        private char charValue;

        private int intValue;

        private long longValue;

        private float floatValue;

        private double doubleValue;

        private String stringValue;
        
        private EnumConstant enumConstant;

        public EvaluationResult() {
            type = -1;
        }

        public PrimitiveType getPrimitiveType(NameInfo ni) {
            return DefaultConstantEvaluator.translateType(type, ni);
        }

        public int getTypeCode() {
            return type;
        }

        public boolean getBoolean() {
            return booleanValue;
        }

        public byte getByte() {
            return byteValue;
        }

        public short getShort() {
            return shortValue;
        }

        public char getChar() {
            return charValue;
        }

        public int getInt() {
            return intValue;
        }

        public long getLong() {
            return longValue;
        }

        public float getFloat() {
            return floatValue;
        }

        public double getDouble() {
            return doubleValue;
        }

        public String getString() {
            return stringValue;
        }
        
        public EnumConstant getEnumConstant() {
        	return enumConstant;
        }

        public void setByte(byte value) {
            byteValue = value;
            type = BYTE_TYPE;
        }

        public void setShort(short value) {
            shortValue = value;
            type = SHORT_TYPE;
        }

        public void setChar(char value) {
            charValue = value;
            type = CHAR_TYPE;
        }

        public void setInt(int value) {
            intValue = value;
            type = INT_TYPE;
        }

        public void setLong(long value) {
            longValue = value;
            type = LONG_TYPE;
        }

        public void setFloat(float value) {
            floatValue = value;
            type = FLOAT_TYPE;
        }

        public void setDouble(double value) {
            doubleValue = value;
            type = DOUBLE_TYPE;
        }

        public void setBoolean(boolean value) {
            booleanValue = value;
            type = BOOLEAN_TYPE;
        }

        // internalizes strings (required for String constant comparations!)
        public void setString(String value) {
            stringValue = (value == null) ? null : value.intern();
            type = STRING_TYPE;
        }
        
        public void setEnumConstant(EnumConstant enumConstant) {
        	this.enumConstant = enumConstant;
        	type = ENUM_CONSTANT_TYPE;
        }

        public String toString() {
            switch (type) {
            case BOOLEAN_TYPE:
                return String.valueOf(booleanValue);
            case BYTE_TYPE:
                return String.valueOf(byteValue);
            case SHORT_TYPE:
                return String.valueOf(shortValue);
            case CHAR_TYPE:
                return String.valueOf(charValue);
            case INT_TYPE:
                return String.valueOf(intValue);
            case LONG_TYPE:
                return String.valueOf(longValue);
            case FLOAT_TYPE:
                return String.valueOf(floatValue);
            case DOUBLE_TYPE:
                return String.valueOf(doubleValue);
            case STRING_TYPE:
                return "\"" + stringValue + "\"";
            case ENUM_CONSTANT_TYPE:
            	return enumConstant.getFullName();
            default:
                return "Unknown type";
            }
        }
    }

    /**
     * Returns the type of a constant expression if it is a compile-time
     * constant as defined in the Java language specification, or <CODE>null
     * </CODE> if it is not.
     * 
     * @param expr
     *            the expression to evaluate.
     * @return the type of the expression, or <CODE>null</CODE> if the
     *         expression is not constant.
     */
    Type getCompileTimeConstantType(Expression expr);

    /**
     * Checks if the given expression is a compile-time constant as defined in
     * the Java language specification.
     * 
     * @param expr
     *            the expression to evaluate.
     * @return <CODE>true</CODE>, if the expression is a compile-time
     *         constant, <CODE>false</CODE> otherwise.
     */
    boolean isCompileTimeConstant(Expression expr);

    /**
     * Checks if the given expression is a compile-time constant as defined in
     * the Java language specification, and derives the result.
     * 
     * @param expr
     *            the expression to evaluate.
     * @param res
     *            the result of the evaluation; contains the type encoding and
     *            the result value.
     * @return <CODE>true</CODE>, if the expression is a compile-time
     *         constant, <CODE>false</CODE> otherwise.
     */
    boolean isCompileTimeConstant(Expression expr, EvaluationResult res);
}