package ak.ak32767.projecte.enums;

import ak.ak32767.projecte.data.CollectorSpecs;
import ak.ak32767.projecte.data.DelaySpecs;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public enum Matter {
    BASIC(1, null),
    DARK(2, "projecte:dark_matter"),
    RED(3, "projecte:red_matter"),
    ORANGE(4, "projecte:orange_matter"),
    YELLOW(5, "projecte:yellow_matter"),
    LIME(6, "projecte:lime_matter"),
    GREEN(7, "projecte:green_matter"),
    BLUE(8, "projecte:blue_matter"),
    CYAN(9, "projecte:cyan_matter"),
    VIOLET(10, "projecte:violet_matter"),
    PURPLE(11, "projecte:purple_matter"),
    MAGENTA(12, "projecte:magenta_matter"),
    PINK(13, "projecte:pink_matter"),
    WHITE(14, "projecte:white_matter"),
    SINGULARITY(15, "projecte:singularity");

    private final int index;
    private final String namespaceID;
    Matter(int index, @Nullable String namespacedID) {
        this.index = index;
        this.namespaceID = namespacedID;
    }

    private static final List<BigInteger> FIB = new ArrayList<>();
    static {
        FIB.add(BigInteger.ONE);
        FIB.add(BigInteger.ONE);
        for (int i = 2; i < Matter.values().length; ++i)
            FIB.add(FIB.get(i - 1).add(FIB.get(i - 2)));
    }

    // MaxGenerationRatePerTick
    private static final List<BigInteger> collectorMaxGRPT = new ArrayList<>();
    static {
        collectorMaxGRPT.add(BigInteger.valueOf(4));
        collectorMaxGRPT.add(BigInteger.valueOf(12));
        collectorMaxGRPT.add(BigInteger.valueOf(40));
        for (int i = 3; i < Matter.values().length; ++i) {
            BigInteger sum = collectorMaxGRPT.get(i - 1).add(collectorMaxGRPT.get(i - 2));
            collectorMaxGRPT.add(sum.multiply(BigInteger.valueOf(15 + (2L * i))).divide(BigInteger.TEN));
        }
    }

    private static final List<BigInteger> collectorMaxEMCStorage = new ArrayList<>();
    static {
        collectorMaxEMCStorage.add(BigInteger.valueOf(10_000));
        collectorMaxEMCStorage.add(BigInteger.valueOf(30_000));
        collectorMaxEMCStorage.add(BigInteger.valueOf(60_000));
        for (int i = 3; i < Matter.values().length; ++i)
            collectorMaxEMCStorage.add(collectorMaxEMCStorage.get(i - 1).multiply(FIB.get(i)));
    }

    // MaxOutputRatePerTick
    private static final List<BigInteger> relayMaxORPT = new ArrayList<>();
    static {
        relayMaxORPT.add(BigInteger.valueOf(64));
        relayMaxORPT.add(BigInteger.valueOf(192));
        relayMaxORPT.add(BigInteger.valueOf(640));
        for (int i = 3; i < Matter.values().length; ++i) {
            BigInteger sum = relayMaxORPT.get(i - 1).add(relayMaxORPT.get(i - 2));
            relayMaxORPT.add(sum.multiply(BigInteger.valueOf(15 + (6L * i))).divide(BigInteger.TEN));
        }
    }

    private static final List<BigInteger> relayMaxStorage = new ArrayList<>();
    static {
        relayMaxStorage.add(BigInteger.valueOf(100_000));
        relayMaxStorage.add(BigInteger.valueOf(1_000_000));
        relayMaxStorage.add(BigInteger.valueOf(10_000_000));
        for (int i = 3; i < Matter.values().length; ++i) {
            BigInteger factor = BigInteger.TEN.pow(2 + (FIB.get(i >> 1).intValue() >> 1));
            relayMaxStorage.add(relayMaxStorage.get(i - 1).multiply(factor));
        }
    }

    public int getIndex() {
        return index;
    }

    public String getNamespaceID() {
        return namespaceID;
    }

    public CollectorSpecs getCollectorSpecs() {
        int index = this.index - 1;
        return new CollectorSpecs(this, Matter.collectorMaxGRPT.get(index), Matter.collectorMaxEMCStorage.get(index));
    }

    public DelaySpecs getDelaySpecs() {
        int index = this.index - 1;
        return new DelaySpecs(this, Matter.relayMaxORPT.get(index), Matter.relayMaxStorage.get(index));
    }
}
