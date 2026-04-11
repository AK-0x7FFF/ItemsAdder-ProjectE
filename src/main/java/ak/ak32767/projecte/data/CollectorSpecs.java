package ak.ak32767.projecte.data;

import ak.ak32767.projecte.enums.Matter;

import java.math.BigInteger;

public record CollectorSpecs(Matter matter, BigInteger maxGenerateRate, BigInteger maxEMCStorage) {
    public BigInteger maxGRPT() {
        return this.maxGenerateRate;
    }
    public BigInteger maxGRPS() {
        return this.maxGenerateRate.multiply(BigInteger.valueOf(20));
    }
}
