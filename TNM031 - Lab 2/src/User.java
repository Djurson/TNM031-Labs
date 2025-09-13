import java.math.BigInteger;

public class User {
    private final String name;
    private final RSAKeyPair keyPair;

    public User(String name, int bitLength) {
        this.name = name;
        this.keyPair = RSA.generateKeyPair(bitLength);
    }

    public String getName() {
        return name;
    }

    public RSAKeyPair getKeyPair() {
        return keyPair;
    }

    public BigInteger sendMessage(String message, User recipient) {
        BigInteger m = new BigInteger(message.getBytes());

        return RSA.encrypt(m, recipient.getKeyPair().getE(), recipient.getKeyPair().getN());
    }

    public String recieveMessage(BigInteger ciphertext) {
        BigInteger plain = RSA.decrypt(ciphertext, keyPair.getD(), keyPair.getN());

        return new String(plain.toByteArray());
    }
}
