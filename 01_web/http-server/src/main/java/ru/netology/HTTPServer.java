package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class HTTPServer implements Runnable {

	private static HTTPServer INSTANCE = null;
	private final ServerSocket serverSocket;
	private final ExecutorService pool;

	private HTTPServer(int port, int poolSize) throws IOException {
		serverSocket = new ServerSocket(port);
		pool = Executors.newFixedThreadPool(poolSize);
	}

	public static synchronized HTTPServer getInstance(int port, int poolSize) throws IOException {

		if (INSTANCE == null) {
			synchronized (HTTPServer.class) {
				if (INSTANCE == null) {
					INSTANCE = new HTTPServer(port, poolSize);
				}
			}
		}
		return INSTANCE;
	}

	public void run() { // run the service
		try {
			while (true) {
				pool.execute(new Handler(serverSocket.accept()));
			}
		} catch (IOException ex) {
			pool.shutdown();
		}
	}
}
