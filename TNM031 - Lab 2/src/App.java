import java.math.BigInteger;
import java.security.SecureRandom;

public class App {
    public static void main(String[] args) throws Exception {
        BigInteger p = BigInteger.probablePrime(512, new SecureRandom());
        BigInteger q = BigInteger.probablePrime(512, new SecureRandom());

        BigInteger n = p.multiply(q);

        BigInteger phiofn = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        BigInteger e = new BigInteger("9007");

    }
}
