import java.math.BigInteger;
import java.security.SecureRandom;

public class User {
    public final String name;
    private BigInteger p, q, d, phiofn;

    public BigInteger n, e;

    public User(String _name) {
        name = _name;
        p = BigInteger.probablePrime(512, new SecureRandom());
        q = BigInteger.probablePrime(512, new SecureRandom());

        while (q.equals(p)) {
            q = BigInteger.probablePrime(512, new SecureRandom());
        }

        n = p.multiply(q);
        phiofn = (p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)));
        e = BigInteger.probablePrime(512 / 2, new SecureRandom());

        while (!phiofn.gcd(e).equals(BigInteger.ONE)) {
            e = BigInteger.probablePrime(512 / 2, new SecureRandom());
        }

        d = e.modInverse(phiofn);
    }

    private BigInteger encryptMessage(String message, User toUser) {
        return new BigInteger(message.getBytes()).modPow(toUser.e, toUser.n);
    }

    private String decryptMessage(BigInteger cipherMessage) {
        BigInteger m = cipherMessage.modPow(d, n);
        return new String(m.toByteArray());
    }

    public void SendMessage(String message, User toUser) {
        BigInteger cipher = encryptMessage(message, toUser);
        System.out.println("\n" + name + " skickade till " + toUser.name + " (krypterat): " + cipher);
        toUser.RecieveMessage(cipher, this);
    }

    private void RecieveMessage(BigInteger cipherMessage, User sentFromUser) {
        String plain = decryptMessage(cipherMessage);
        System.out.println(name + " tog emot från " + sentFromUser.name + ": " + plain);
    }
}
