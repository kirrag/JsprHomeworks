package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

	private static final int PORT = 9999;
	private static final int POOL_SIZE = 64;

	private static final String PAGES_DIR = "public";
	private static final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png",
			"/resources.html",
			"/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

	public static void main(String[] args) throws IOException {
		final var server = HTTPServer.getInstance(PORT, POOL_SIZE);

		// добавление хендлеров (обработчиков)
		server.addHandler("GET", "/messages", new Handler() {
			public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
				String content = "Hello, World from GET!";
				responseStream.write(("HTTP/1.1 200 OK\r\n" +
						"Content-Type: " + "text/plain" + "\r\n" +
						"Content-Length: " + content.length() + "\r\n" +
						"Connection: close\r\n" +
						"\r\n").getBytes());

				responseStream.write(content.getBytes());
			}
		});

		server.addHandler("POST", "/messages", new Handler() {
			public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
				String content = "Hello, World from POST!";
				responseStream.write(("HTTP/1.1 200 OK\r\n" +
						"Content-Type: " + "text/plain" + "\r\n" +
						"Content-Length: " + content.length() + "\r\n" +
						"Connection: close\r\n" +
						"\r\n").getBytes());

				responseStream.write(content.getBytes());
			}
		});

		server.addHandler("GET", "/classic.html", new Handler() {
			public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
				final var filePath = Path.of(".", "public", "classic.html");
				final var mimeType = Files.probeContentType(filePath);
				final var template = Files.readString(filePath);
				// final var length = Files.size(filePath);
				final var content = template.replace(
						"{time}",
						LocalDateTime.now().toString()).getBytes();
				responseStream.write(("HTTP/1.1 200 OK\r\n" +
						"Content-Type: " + mimeType + "\r\n" +
						"Content-Length: " + content.length + "\r\n" +
						"Connection: close\r\n" +
						"\r\n").getBytes());
				responseStream.write(content);

			}
		});

		for (String validPath : VALID_PATHS) {

			server.addHandler("GET", validPath, new Handler() {
				public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
					final var filePath = Path.of(".", PAGES_DIR, validPath);
					final var mimeType = Files.probeContentType(filePath);

					final var length = Files.size(filePath);

					responseStream.write(("HTTP/1.1 200 OK\r\n" +
							"Content-Type: " + mimeType + "\r\n" +
							"Content-Length: " + length + "\r\n" +
							"Connection: close\r\n" +
							"\r\n").getBytes());
					Files.copy(filePath, responseStream);
				}
			});
		}

		server.run();
	}
}
