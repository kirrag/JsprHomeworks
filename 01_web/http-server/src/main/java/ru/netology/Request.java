package ru.netology;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class Request {
	private String method;
	private String path;
	private String version;
	private final List<String> headers;
	private final String body;
	private final List<NameValuePair> queryParams;

	public Request(BufferedInputStream in) throws IOException, URISyntaxException {
		final var limit = 4096;
		in.mark(limit);
		final var buffer = new byte[limit];
		final var read = in.read(buffer);
		final var requestLineDelimiter = new byte[] { '\r', '\n' };
		final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
		final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

		this.method = requestLine[0];
		final var pathAndQuery = requestLine[1];
		var path = pathAndQuery;
		if (pathAndQuery.contains("?")) {
			path = pathAndQuery.split("/?")[0];
		}
		this.path = path;
		this.version = requestLine[2];

		final var headersDelimiter = new byte[] { '\r', '\n', '\r', '\n' };
		final var headersStart = requestLineEnd + requestLineDelimiter.length;
		final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);

		in.reset();
		in.skip(headersStart);

		final var headersBytes = in.readNBytes(headersEnd - headersStart);
		this.headers = Arrays.asList(new String(headersBytes).split("\r\n"));

		String body = null;
		if (!method.equals("GET")) {
			in.skip(headersDelimiter.length);
			final var contentLength = extractHeader(headers, "Content-Length");
			if (contentLength.isPresent()) {
				final var length = Integer.parseInt(contentLength.get());
				final var bodyBytes = in.readNBytes(length);

				body = new String(bodyBytes);
			}
		}
		this.body = body;

		this.queryParams = new URIBuilder(pathAndQuery).getQueryParams();
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public String getVersion() {
		return version;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}


	public List<NameValuePair> getQueryParam(String name) {
		return queryParams
				.stream()
				.filter(o -> Objects.equals(o.getName(), name))
				.collect(Collectors.toList());
	}

	public List<NameValuePair> getQueryParams() {
		return queryParams;
	}

	private static Optional<String> extractHeader(List<String> headers, String header) {
		return headers.stream()
				.filter(o -> o.startsWith(header))
				.map(o -> o.substring(o.indexOf(" ")))
				.map(String::trim)
				.findFirst();
	}

	private static int indexOf(byte[] array, byte[] target, int start, int max) {
		outer: for (int i = start; i < max - target.length + 1; i++) {
			for (int j = 0; j < target.length; j++) {
				if (array[i + j] != target[j]) {
					continue outer;
				}
			}
			return i;
		}
		return -1;
	}
}
