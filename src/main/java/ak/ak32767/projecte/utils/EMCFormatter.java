package ak.ak32767.projecte.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class EMCFormatter {
    private static final DecimalFormat formatter = new DecimalFormat("#,###");
    private static final String[] UNITS = {"", "un", "duo", "tre", "quattuor", "quin", "sex", "septen", "octo", "novem"};
    private static final String[] TENS = {"", "deci", "viginti", "triginta", "quadraginta", "quinquaginta", "sexaginta", "septuaginta", "octoginta", "nonaginta"};
    private static final String[] HUNDREDS = {"", "centi", "ducenti", "trecenti", "quadringenti", "quingenti", "sescenti", "septingenti", "octingenti", "nongenti"};

    public static String commaFormat(BigInteger value) {
        return formatter.format(value);
    }

    public static String numberNameFormat(BigInteger value) {
        if (value.compareTo(BigInteger.valueOf(1_000_000)) < 0)
            return value.toString();

        int digit = value.toString().length();
        return shortedNum(value) + " " + getNumberName(digit);
    }

//    public static String scientificFormat(BigInteger value) {
//        int digit = value.toString().length();
//        return a(value) + " " + getNumberName(digit - 1);
//    }

    private static String shortedNum(BigInteger value) {
        int digit = value.toString().length();
        BigDecimal mantissa = new BigDecimal(value).movePointLeft((digit - 1) / 3 * 3);

        return new DecimalFormat("#.##").format(mantissa);
    }

    private static String getNumberName(int exponent) {
        if (exponent < 3) return "";
        if (exponent == 6) return "MILLION";

        int n = (exponent - 3) / 3;
        if (n == 1) return "MILLION";
        if (n == 2) return "BILLION";
        if (n == 3) return "TRILLION";

        int u = n % 10;
        int t = (n / 10) % 10;
        int h = (n / 100) % 10;

        String name = UNITS[u] + TENS[t] + HUNDREDS[h];
        return name.toUpperCase() + "ILLION";
    }

}
