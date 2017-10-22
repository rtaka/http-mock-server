package com.example.mock_http_server;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import lombok.AllArgsConstructor;
import lombok.Data;

public class HttpClientTest {
	private MockHttpServer httpServer;

	@BeforeClass
	public void setup() throws IOException {
		httpServer = new MockHttpServer(8080);
		httpServer.start();
	}

	@AfterClass
	public void teardown() {
		httpServer.stop();
	}

	@Test
	public void get() {
		httpServer.mock(mock -> {
			mock.method = "get";
			mock.status = Status.OK;
			mock.path = "/get/test";
			mock.body = "Hello World";
		});
		Response response = ClientBuilder.newClient() //
				.target("http://localhost:8080") //
				.path("/get/test") //
				.request() //
				.get();
		assertEquals(response.getStatus(), 200);
		assertEquals(response.readEntity(String.class), "Hello World");
	}

	@Test
	public void post() {
		httpServer.mock(mock -> {
			mock.method = "post";
			mock.status = Status.OK;
			mock.path = "/post/test";
			mock.param = "{\"id\":1}";
			mock.body = "Hello World";
		});
		Response response = ClientBuilder.newClient() //
				.target("http://localhost:8080") //
				.path("/post/test") //
				.request() //
				.post(Entity.json(new Param(1)));
		assertEquals(response.getStatus(), 200);
		assertEquals(response.readEntity(String.class), "Hello World");
	}
	
	@Data
	@AllArgsConstructor
	static class Param {
		private int id;
	}
}
