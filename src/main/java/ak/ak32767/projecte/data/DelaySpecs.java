package ak.ak32767.projecte.data;

import ak.ak32767.projecte.enums.Matter;

import java.math.BigInteger;

public record DelaySpecs(Matter matter, BigInteger maxOutputRate, BigInteger maxStorage) {
    public BigInteger maxORPT() {
        return this.maxOutputRate;
    }
    public BigInteger maxORPS() {
        return this.maxOutputRate.multiply(BigInteger.valueOf(20));
    }
}
