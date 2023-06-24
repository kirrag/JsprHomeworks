package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class HTTPServer implements Runnable {

	private static HTTPServer INSTANCE = null;
	private final ServerSocket serverSocket;
	private final ExecutorService pool;

	public final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> getHandlers() {
		return handlers;
	}

	private ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

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
				pool.execute(new ConnectionHandler(serverSocket.accept(), this));
			}
		} catch (IOException ex) {
			pool.shutdown();
		}
	}

	public void addHandler(String method, String path, Handler handler) {
		var methodMap = handlers.get(method);

		if (methodMap == null) {
			methodMap = new ConcurrentHashMap<>();
			handlers.put(method, methodMap);
		}

		if (!methodMap.containsKey(path)) {
			methodMap.put(path, handler);
		}
	}
}
