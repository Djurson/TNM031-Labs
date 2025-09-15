import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class App {
    public static void main(String[] args) throws IOException {
        User bob = new User("Bob");
        User alice = new User("Alice");

        System.out.println("Send a message to Alice:");
        String bobMessageString = (new BufferedReader(new InputStreamReader(System.in))).readLine();

        bob.SendMessage(bobMessageString, alice);

        System.out.println("\nSend a message back to Bob:");
        String aliceMessageString = (new BufferedReader(new InputStreamReader(System.in))).readLine();

        alice.SendMessage(aliceMessageString, bob);
    }
}
