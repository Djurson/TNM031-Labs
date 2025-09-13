import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {
    private static final SecureRandom random = new SecureRandom();

    public static RSAKeyPair generateKeyPair(int bitLength) {
        BigInteger p = BigInteger.probablePrime(bitLength, random);
        BigInteger q = BigInteger.probablePrime(bitLength, random);

        BigInteger n = p.multiply(q);

        BigInteger phiofn = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        BigInteger e = BigInteger.valueOf(65537);
        if (!phiofn.gcd(e).equals(BigInteger.ONE)) {
            throw new RuntimeException("Ogiltigt e, byt primtal!");
        }

        BigInteger d = e.modInverse(phiofn);

        return new RSAKeyPair(n, e, d);
    }

    public static BigInteger encrypt(BigInteger message, BigInteger e, BigInteger n) {
        return message.modPow(e, n);
    }

    public static BigInteger decrypt(BigInteger ciphertext, BigInteger d, BigInteger n) {
        return ciphertext.modPow(d, n);
    }
}
