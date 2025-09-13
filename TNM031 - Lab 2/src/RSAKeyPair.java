import java.math.BigInteger;

public class RSAKeyPair {
    private final BigInteger n, e, d;

    public RSAKeyPair(BigInteger n, BigInteger e, BigInteger d) {
        this.n = n;
        this.e = e;
        this.d = d;
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getE() {
        return e;
    }

    public BigInteger getD() {
        return d;
    }
}
