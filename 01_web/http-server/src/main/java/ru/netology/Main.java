package ru.netology;

import java.io.IOException;

public class Main {

	private static final int port = 9999;
	private static final int poolSize = 64;

	public static void main(String[] args) throws IOException{
		final var server = HTTPServer.getInstance(port, poolSize);
		server.run();
  }
}

