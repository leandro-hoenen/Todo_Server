package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Model {
	/*
	private String ipAddress = null;
	private int port = 0;
	
	

	try (Scanner in = new Scanner(System.in)) {
		// Read IP address (no validation)
		System.out.println("Enter the address of the server");
		ipAddress = in.nextLine();

		// Read port number (validated)
		boolean valid = false;
		while (!valid) {
			System.out.println("Enter the port number on the server (1024-65535)");
			String strPort = in.nextLine();
			valid = validatePortNumber(strPort);
			if (valid) port = Integer.parseInt(strPort);
		}

		try (Socket socket = new Socket(ipAddress, port);
			BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStreamWriter socketOut = new OutputStreamWriter(socket.getOutputStream())) {
			System.out.println("Connected");
			// Create thread to read incoming messages
			Runnable r = new Runnable() {
				@Override
				public void run() {
					String msg = ""; // Anything except null
					while (msg != null) { // Will be null if the server closes the socket
						try {
							msg = socketIn.readLine();
							System.out.println("Received: " + msg);
						} catch (IOException e) {
							msg = null; // end loop if we have a communications error
						}
					}
				}
			};
			Thread t = new Thread(r);
			t.start();

			// Allowing the user to send messages to the server
			// Note: We are using the same Scanner defined earlier
			System.out.println("Enter commands to server or ctrl-D to quit");
			while (in.hasNext()) {
				String line = in.nextLine();
				socketOut.write(line + "\n");
				socketOut.flush();
				System.out.println("Sent: " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean validatePortNumber(String portText) {
		boolean formatOK = false;
		try {
			int portNumber = Integer.parseInt(portText);
			formatOK = (portNumber >= 1024 & portNumber <= 65535);
		} catch (NumberFormatException e) {
		}
		return formatOK;
	}
	*/
}
