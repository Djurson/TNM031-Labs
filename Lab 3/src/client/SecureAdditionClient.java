// A client-side class that uses a secure TCP/IP socket

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.Scanner;

import javax.annotation.processing.FilerException;
import javax.net.ssl.*;

public class SecureAdditionClient {
	private InetAddress host;
	private int port;

	// This is not a reserved port number
	static final int DEFAULT_PORT = 8189;

	// Clients keystore -> clients private key
	static final String KEYSTORE = "LIUkeystore.ks";
	static final String KEYSTOREPASS = "123456";

	// Client truststore -> keys that the client trusts
	static final String TRUSTSTORE = "LIUtruststore.ks";
	static final String TRUSTSTOREPASS = "abcdef";

	// Client file directory
	static final String CLIENT_FILES_DIR = "files";

	// Static commands sent between server and client
	private static final String FILE_EXISTS = "/FE/";
	private static final String END_OF_FILE = "/EOF/";
	private static final String END_LIST = "/EL/";
	private static final String END_SESSION = "/ES/";
	private static final String UPLOAD_COMPLETE = "/UC/";

	// Constructor @param host Internet address of the host where the server is
	// located
	// @param port Port number on the host where the server is listening
	public SecureAdditionClient(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	// The method used to start a client object
	public void run() {
		try {
			// Loads the clients keystore containing its private key and certificate
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(KEYSTORE), KEYSTOREPASS.toCharArray());

			// Loads the clients truststore containing trusted server certificates
			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(TRUSTSTORE), TRUSTSTOREPASS.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, KEYSTOREPASS.toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			SSLSocketFactory sslFact = sslContext.getSocketFactory();
			SSLSocket client = (SSLSocket) sslFact.createSocket(host, port);

			client.setEnabledCipherSuites(client.getSupportedCipherSuites());
			System.out.println("\n>>>> SSL/TLS handshake completed");

			BufferedReader socketIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter socketOut = new PrintWriter(client.getOutputStream(), true);

			Scanner scan = new Scanner(System.in);

			while (true) {
				System.out.println("\nSelect option\n"
						+ "GET: Download a file from the server\n"
						+ "PUT: Upload a file to the server\n"
						+ "LIST: List all files on the server\n"
						+ "DEL: Delete a file on the server\n"
						+ "QUIT: Quit the program\n");

				String option = scan.nextLine().trim().toLowerCase();

				if (option.equals("quit")) {
					socketOut.println(END_SESSION);
					System.out.println("Exiting client.");
					break;
				}

				socketOut.println(option);

				switch (option.toLowerCase()) {
					case "get":
						System.out.print("Enter filename to download: ");
						String downloadFilename = scan.nextLine();
						socketOut.println(downloadFilename);

						String status = socketIn.readLine(); // Server message if it exists or not
						if (status.equals(FILE_EXISTS)) {
							String fileSizeStr = socketIn.readLine(); // File size
							System.out.printf("File size: %s%n", fileSizeStr);
							GetFile(socketIn, downloadFilename);
						} else {
							System.out.println("Error: " + status);
						}
						break;

					case "list":
						ListFiles(socketIn);
						break;

					case "put":
						System.out.print("Enter filename to upload: ");
						String filename = scan.nextLine();
						File uploadFile = new File(CLIENT_FILES_DIR, filename);

						if (!uploadFile.exists()) {
							System.out.println("Error: File not found locally");
							socketOut.println("ERROR: File not found");
							break;
						}

						socketOut.println(filename);
						PutFile(socketOut, uploadFile);

						String serverResponse = socketIn.readLine();
						if (UPLOAD_COMPLETE.equals(serverResponse)) {
							System.out.println("Upload completed successfully!");
						} else {
							System.out.println("Upload failed: " + serverResponse);
						}
						break;

					case "del":
						System.out.print("Enter filename to delete: ");
						String deleteFilename = scan.nextLine();
						socketOut.println(deleteFilename);

						String deleteResult = socketIn.readLine();
						System.out.printf("Server response: %s %n", deleteResult);
						break;

					default:
						System.out.println("Invalid option");
						socketOut.println("INVALID_OPTION");
						break;
				}
			}

			scan.close();
			client.close();
		} catch (Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
	}

	private void GetFile(BufferedReader socketIn, String filename) {
		try {
			File downloadDir = new File(CLIENT_FILES_DIR);
			if (!downloadDir.exists())
				downloadDir.mkdirs();

			File outputFile = new File(CLIENT_FILES_DIR, filename);
			FileWriter writer = new FileWriter(outputFile);

			String line;
			while (!(line = socketIn.readLine()).equals(END_OF_FILE)) {
				writer.write(line + "\n");
			}

			writer.close();
		} catch (Exception e) {
			System.out.println("Download error: " + e);
		}
	}

	private void ListFiles(BufferedReader socketIn) {
		try {
			String line;
			while (!(line = socketIn.readLine()).equals(END_LIST)) {
				System.out.println(line);
			}
		} catch (Exception e) {
			System.out.println("List error: " + e);
		}
	}

	private void PutFile(PrintWriter socketOut, File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			String line;
			while ((line = reader.readLine()) != null) {
				socketOut.println(line);
			}
			reader.close();
			socketOut.println(END_OF_FILE);
		} catch (Exception e) {
			System.out.println("Upload error: " + e);
		}
	}

	// The test method for the class @param args Optional port number and host name
	public static void main(String[] args) {
		try {
			InetAddress host = InetAddress.getLocalHost();
			int port = DEFAULT_PORT;
			if (args.length > 0) {
				port = Integer.parseInt(args[0]);
			}
			if (args.length > 1) {
				host = InetAddress.getByName(args[1]);
			}
			SecureAdditionClient addClient = new SecureAdditionClient(host, port);
			addClient.run();
		} catch (UnknownHostException uhx) {
			System.out.println(uhx);
			uhx.printStackTrace();
		}
	}
}
