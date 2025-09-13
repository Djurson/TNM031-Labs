import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;

public class App {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Name the first user: ");
        String user1name = reader.readLine();
        User user1 = new User(user1name, 512);

        System.out.println("\nName the second user:");
        String user2name = reader.readLine();
        User user2 = new User(user2name, 512);

    }
}
