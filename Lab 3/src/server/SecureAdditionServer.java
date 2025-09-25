
// An example class that uses the secure server socket class

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.net.ssl.*;
import java.security.*;
import java.util.Objects;

public class SecureAdditionServer {
	private int port;

	// This is not a reserved port number
	static final int DEFAULT_PORT = 8189;

	// Server keystore -> servers private key
	static final String KEYSTORE = "LIUkeystore.ks";
	static final String KEYSTOREPASS = "123456";

	// Server truststore -> keys that the server trusts
	static final String TRUSTSTORE = "LIUtruststore.ks";
	static final String TRUSTSTOREPASS = "abcdef";

	// Server file directory
	static final String SERVER_FILES_DIR = "files";

	// Static commands sent between server and client
	private static final String FILE_EXISTS = "/FE/";
	private static final String END_OF_FILE = "/EOF/";
	private static final String END_LIST = "/EL/";
	private static final String END_SESSION = "/ES/";
	private static final String UPLOAD_COMPLETE = "/UC/";

	private

	/**
	 * Constructor
	 * 
	 * @param port The port where the server
	 *             will listen for requests
	 */
	SecureAdditionServer(int port) {
		this.port = port;
	}

	/** The method that does the work for the class */
	public void run() {
		try {
			// Loads the servers keystore containing its private key and certificate
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(KEYSTORE), KEYSTOREPASS.toCharArray());

			// Loads the servers truststore containing trusted client certificates
			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(TRUSTSTORE), TRUSTSTOREPASS.toCharArray());

			// Creates a factory that manages the server's keys for SSL handshake
			// Handles the server-side of the cryptographic handshake
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, KEYSTOREPASS.toCharArray());

			// Creates a factory that decides which client certificates to trust
			// Implements the server's trust policy
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);

			// Creates the main SSL context that combines keys and trust settings
			// This is the core SSL engine that handles all secure communication
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			// Creates a secure server socket that listens on the specified port
			// This is where clients will connect securely
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket(port);

			// Server authenticates the client
			sss.setNeedClientAuth(true);
			sss.setEnabledCipherSuites(sss.getSupportedCipherSuites());

			System.out.println("\n>>>> SecureAdditionServer: active ");

			while (true) {
				SSLSocket incoming = (SSLSocket) sss.accept();
				System.out.println("Client connected!");

				new ClientHandler(incoming).start();
			}
		} catch (Exception x) {
			System.out.println("Server error:" + x);
			x.printStackTrace();
		}
	}

	/**
	 * The test method for the class
	 * 
	 * @param args[0] Optional port number in place of
	 *                the default
	 */
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		SecureAdditionServer addServe = new SecureAdditionServer(port);
		addServe.run();
	}

	/**
	 * Client Handler Class - Inorder to have each client connection on its own
	 * thread
	 */
	private class ClientHandler extends Thread {
		private SSLSocket socket;

		/**
		 * Constructor
		 * 
		 * @param SSLSocket - Which SSL socket the client is connected to
		 * 
		 */
		public ClientHandler(SSLSocket socket) {
			this.socket = socket;
		}

		/** Main method for the Client Handler Class */
		public void run() {
			try (
					// Creates streams to read from and write to the client
					// All data through these streams is automatically encrypted/decrypted by SSL
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);) {

				String option;
				while ((option = in.readLine()) != null) {
					if (option.equals(END_SESSION)) {
						System.out.println("Client session ended normally");
						break;
					}

					System.out.printf("Client selected option: %s %n", option);

					switch (option.toLowerCase()) {
						case "get":
							HandleGet(in, out);
							break;
						case "list":
							HandleList(in, out);
							break;
						case "put":
							HandlePut(in, out);
							break;
						case "del":
							HandleDel(in, out);
							break;
						default:
							out.println("ERROR: Invalid option");
							break;
					}
				}
			} catch (Exception e) {
				System.out.println("Client handling error: " + e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}

		private void HandleGet(BufferedReader in, PrintWriter out) {
			try {
				String filename = in.readLine();
				File file = new File(SERVER_FILES_DIR, filename);

				// Check if the file exists if it doesn't -> send error to the client
				if (!file.exists()) {
					out.println("ERROR: File not found\n");
					System.out.printf("Download failed: File %s not found %n", filename);
					return;
				}

				System.out.printf("Sending file: %s (%s)%n", filename, formatFileSize(file.length()));

				// Send that the file exists and file size
				out.println(FILE_EXISTS);
				out.println(formatFileSize(file.length()));
				System.out.printf("Sending file: %s (%s) %n", filename, formatFileSize(file.length()));

				// Read the file and send the read content
				BufferedReader fileReader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = fileReader.readLine()) != null) {
					out.println(line);
				}

				// Close reader and send END_OF_FILE statement
				fileReader.close();
				out.println(END_OF_FILE);

				System.out.printf("File %s sent successfully (%s)%n", filename, formatFileSize(file.length()));
			} catch (Exception e) {
				System.out.println("Download handling error: " + e);
				out.println("ERROR:Server error during download");
			}
		}

		private void HandleList(BufferedReader in, PrintWriter out) {
			try {
				// Check if directory exists (if not creates it)
				Path dirPath = Paths.get(SERVER_FILES_DIR);
				Files.createDirectories(dirPath);

				File[] files = Objects.requireNonNull(dirPath.toFile().listFiles());
				if (files.length > 0) {
					for (File file : files) {
						System.out.printf("Found file: %s (%s)%n", file.getName(), formatFileSize(file.length()));
						out.printf("%s (%s)%n", file.getName(), formatFileSize(file.length()));
					}
				} else {
					out.println("Directory is empty");
				}
				out.println(END_LIST);

				System.out.println("Listed all files");
			} catch (Exception e) {
				System.out.println("Error when listing files: " + e);
			}
		}

		private void HandlePut(BufferedReader in, PrintWriter out) {
			try {
				String filename = in.readLine();

				if (filename.startsWith("ERROR:")) {
					System.out.println("Client Error: " + filename);
					return;
				}

				File uploadDir = new File(SERVER_FILES_DIR);
				if (!uploadDir.exists())
					uploadDir.mkdirs();

				File file = new File(SERVER_FILES_DIR, filename);
				FileWriter writer = new FileWriter(file);
				System.out.printf("Receiving file: %s %n", filename);

				String line;
				while (!(line = in.readLine()).equals(END_OF_FILE)) {
					writer.write(line + "\n");
				}
				writer.close();

				System.out.printf("File %s received successfully %n", filename);
				out.println(UPLOAD_COMPLETE);
			} catch (Exception e) {
				System.out.println("Upload handling error: " + e);
				out.println("ERROR: Server error during upload");
			}
		}

		private void HandleDel(BufferedReader in, PrintWriter out) {
			try {
				String filename = in.readLine();
				File file = new File(SERVER_FILES_DIR, filename);

				if (file.delete()) {
					out.printf("File %s was deleted successfully %n", filename);
					System.out.printf("File %s deleted %n", filename);
				} else {
					out.println("ERROR: File not found or cannot be deleted");
					System.out.printf("Delete failed for file: %s %n", filename);
				}
			} catch (Exception e) {
				System.out.println("Delete handling error: " + e);
				out.println("ERROR: Server error during deletion");
			}
		}

		// Basic format function for formatting file sizes
		private static String formatFileSize(long sizeBytes) {
			if (sizeBytes < 1024) {
				return sizeBytes + " B";
			} else if (sizeBytes < 1024 * 1024) {
				return String.format("%.2f KB", sizeBytes / 1024.0);
			} else if (sizeBytes < 1024L * 1024 * 1024) {
				return String.format("%.2f MB", sizeBytes / (1024.0 * 1024));
			} else {
				return String.format("%.2f GB", sizeBytes / (1024.0 * 1024 * 1024));
			}
		}
	}
}
