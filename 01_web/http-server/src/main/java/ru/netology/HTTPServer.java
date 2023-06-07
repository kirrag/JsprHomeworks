package ru.netology;

import java.io.*;
import java.net.ServerSocket;
/*
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
*/
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class HTTPServer implements Runnable {
	private final ServerSocket serverSocket;
	private final ExecutorService pool;

	private HTTPServer(int port, int poolSize) throws IOException {
		serverSocket = new ServerSocket(port);
		pool = Executors.newFixedThreadPool(poolSize);
	}

	public void run() { // run the service
		try {
			for (;;) {
				pool.execute(new Handler(serverSocket.accept()));
			}
		} catch (IOException ex) {
			pool.shutdown();
		}
	}

	void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ex) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	private static HTTPServer INSTANCE = null;

	public static synchronized HTTPServer getInstance() throws IOException {

		if (INSTANCE == null) {
			synchronized (HTTPServer.class) {
				if (INSTANCE == null) {
					INSTANCE = new HTTPServer(9999, 64);
				}
			}
		}
		return INSTANCE;
	}
/*
	public static void start() {
		final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
				"/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

		try (final var serverSocket = new ServerSocket(9999)) {
			while (true) {
				try (
						final var socket = serverSocket.accept();
						final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						final var out = new BufferedOutputStream(socket.getOutputStream());
				) {
					// read only request line for simplicity
					// must be in form GET /path HTTP/1.1
					final var requestLine = in.readLine();
					final var parts = requestLine.split(" ");

					if (parts.length != 3) {
						// just close socket
						continue;
					}

					final var path = parts[1];
					if (!validPaths.contains(path)) {
						out.write(("HTTP/1.1 404 Not Found\r\n" +
								"Content-Length: 0\r\n" +
								"Connection: close\r\n" +
								"\r\n").getBytes());
						out.flush();
						continue;
					}

					final var filePath = Path.of(".", "public", path);
					final var mimeType = Files.probeContentType(filePath);

					// special case for classic
					if (path.equals("/classic.html")) {
						final var template = Files.readString(filePath);
						final var content = template.replace(
								"{time}",
								LocalDateTime.now().toString()).getBytes();
						out.write(("HTTP/1.1 200 OK\r\n" +
								"Content-Type: " + mimeType + "\r\n" +
								"Content-Length: " + content.length + "\r\n" +
								"Connection: close\r\n" +
								"\r\n").getBytes());
						out.write(content);
						out.flush();
						continue;
					}

					final var length = Files.size(filePath);
					out.write(("HTTP/1.1 200 OK\r\n" +
							"Content-Type: " + mimeType + "\r\n" +
							// "Content-Type: " + "text/plain" + "\r\n" +
							"Content-Length: " + length + "\r\n" +
							"Connection: close\r\n" +
							"\r\n").getBytes());
					Files.copy(filePath, out);
					out.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void stop() {

	}
	*/
}
