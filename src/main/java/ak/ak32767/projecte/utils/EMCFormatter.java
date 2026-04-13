package ak.ak32767.projecte.utils;

import org.apache.commons.lang3.StringUtils;

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
        return shortedNum(value) + " " + StringUtils.capitalize(getNumberName(digit));
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

    private static String getNumberName(int digit) {
        if (digit < 3) return "";

        int n = (digit - 3) / 3;
        if (n == 1) return "million";
        if (n == 2) return "billion";
        if (n == 3) return "trillion";

        String name = UNITS[n % 10] + TENS[(n / 10) % 10] + HUNDREDS[(n / 100) % 10];
        return name + "illion";
    }

}
