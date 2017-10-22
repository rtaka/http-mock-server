package com.example.mock_http_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.ws.rs.core.Response.Status;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class MockHttpServer {

	private HttpServer httpServer;
	private ExecutorService executor = Executors.newCachedThreadPool();

	public MockHttpServer(int port) throws IOException {
		httpServer = HttpServer.create(new InetSocketAddress(port), 0);
		httpServer.setExecutor(executor);
	}

	public void start() {
		httpServer.start();
	}

	public void stop() {
		httpServer.stop(1);
		executor.shutdown();
	}

	public void mock(Consumer<Mock> custom) {
		Mock mock = new Mock();
		custom.accept(mock);
		handle(mock);
	}

	@SuppressWarnings("restriction")
	private void handle(Mock mock) {
		httpServer.createContext(mock.path, httpExchange -> {
			if (!equalsMethod(httpExchange, mock.method)) {
				httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
				return;
			}
			if (!equalsParam(httpExchange, mock.param)) {
				httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
				return;
			}
			byte[] body = mock.body.getBytes(StandardCharsets.UTF_8);
			httpExchange.sendResponseHeaders(mock.status.getStatusCode(), body.length);
			try (OutputStream os = httpExchange.getResponseBody()) {
				os.write(body);
			}
		});
	}

	private boolean equalsMethod(HttpExchange httpExchange, String expected) {
		return httpExchange.getRequestMethod().equalsIgnoreCase(expected);
	}

	private boolean equalsParam(HttpExchange httpExchange, String expected) throws IOException {
		if (expected == null) {
			return true;
		}

		StringBuilder requestBody = new StringBuilder();
		try (InputStream is = httpExchange.getRequestBody(); //
				InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8); //
				BufferedReader reader = new BufferedReader(isr)) {
			reader.lines().forEach(line -> requestBody.append(line));
		}
		return requestBody.toString().equals(expected);
	}

	static class Mock {
		String method;
		String path;
		String param;
		Status status;
		String body;
	}

}
