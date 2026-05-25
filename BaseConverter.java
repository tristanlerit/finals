package Tann;

/**
 * BaseConverter — Logic Engine
 * Handles all arithmetic and base conversion operations.
 */
public class BaseConverter {

    // =========================================================
    // VALIDATE INPUT
    // =========================================================

    /**
     * Validates whether the given string is a valid number in the specified base.
     *
     * @param value the string to validate
     * @param base  the number base (2, 8, 10, 16)
     * @return true if valid, false otherwise
     */
    public boolean validateInput(String value, int base) {

        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String regex;

        switch (base) {
            case 2:  regex = "[01]+";          break;
            case 8:  regex = "[0-7]+";         break;
            case 10: regex = "[0-9]+";         break;
            case 16: regex = "[0-9A-Fa-f]+";   break;
            default: return false;
        }

        return value.trim().matches(regex);
    }

    // =========================================================
    // CONVERT TO DECIMAL
    // =========================================================

    /**
     * Converts a string number of the given base to a decimal (long) value.
     *
     * @param value the string representation of the number
     * @param base  the number base
     * @return decimal (long) equivalent
     */
    public long convertToDecimal(String value, int base) {
        return Long.parseLong(value.trim(), base);
    }

    // =========================================================
    // CONVERT FROM DECIMAL
    // =========================================================

    /**
     * Converts a decimal (long) value to all supported base representations.
     *
     * @param value the decimal value
     * @return formatted string showing Binary, Octal, Decimal, and Hexadecimal
     */
    public String convertFromDecimal(long value) {

        StringBuilder sb = new StringBuilder();

        sb.append("Binary      :  ").append(Long.toBinaryString(value)).append("\n");
        sb.append("Octal       :  ").append(Long.toOctalString(value)).append("\n");
        sb.append("Decimal     :  ").append(value).append("\n");
        sb.append("Hexadecimal :  ").append(Long.toHexString(value).toUpperCase());

        return sb.toString();
    }

    // =========================================================
    // CONVERT TO SPECIFIC BASE STRING
    // =========================================================

    /**
     * Converts a decimal value to a specific target base string.
     *
     * @param decimal  the decimal value
     * @param toBase   the target base (2, 8, 10, 16)
     * @return string representation in the target base
     */
    public String convertToBase(long decimal, int toBase) {

        switch (toBase) {
            case 2:  return Long.toBinaryString(decimal);
            case 8:  return Long.toOctalString(decimal);
            case 10: return String.valueOf(decimal);
            case 16: return Long.toHexString(decimal).toUpperCase();
            default: return String.valueOf(decimal);
        }
    }

    // =========================================================
    // PERFORM ARITHMETIC
    // =========================================================

    /**
     * Performs arithmetic on two numbers of (possibly different) bases,
     * returning results in all base formats.
     *
     * @param n1  first operand string
     * @param n2  second operand string
     * @param b1  base of first operand
     * @param b2  base of second operand
     * @param op  operator: "+", "-", "*", "/"
     * @return formatted multi-base result string
     * @throws ArithmeticException if division by zero
     */
    public String performArithmetic(String n1, String n2, int b1, int b2, String op) {

        long val1 = convertToDecimal(n1, b1);
        long val2 = convertToDecimal(n2, b2);
        long result;

        switch (op) {
            case "+": result = val1 + val2; break;
            case "-": result = val1 - val2; break;
            case "*": result = val1 * val2; break;
            case "/":
                if (val2 == 0) {
                    throw new ArithmeticException("Division by zero is not allowed.");
                }
                result = val1 / val2;
                break;
            default:
                throw new IllegalArgumentException("Unknown operator: " + op);
        }

        return convertFromDecimal(result);
    }

    // =========================================================
    // GET DECIMAL RESULT ONLY
    // =========================================================

    /**
     * Returns just the decimal long result of arithmetic (used for display).
     */
    public long getArithmeticDecimal(String n1, String n2, int b1, int b2, String op) {

        long val1 = convertToDecimal(n1, b1);
        long val2 = convertToDecimal(n2, b2);

        switch (op) {
            case "+": return val1 + val2;
            case "-": return val1 - val2;
            case "*": return val1 * val2;
            case "/":
                if (val2 == 0) throw new ArithmeticException("Division by zero.");
                return val1 / val2;
            default:
                throw new IllegalArgumentException("Unknown op: " + op);
        }
    }
}