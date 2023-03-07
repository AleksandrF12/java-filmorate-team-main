package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
/*
Если не указать webEnvironment, то приложение в тестовом режиме будет работать на рандомном порту.
Нужно для uri: http://localhost:8080
 */
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DirectorControllerTest {

	private final static String URI = "http://localhost:8080/directors";

	private HttpUriRequest request;
	private HttpResponse response;

	HttpUriRequest createRequest(String method, String uri, String json) {
		if (json != null)
			return RequestBuilder.create(method)
					.setUri(uri)
					.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON))
					.build();
		else
			return RequestBuilder.create(method)
					.setUri(uri)
					.build();
	}

	void fillDB() throws IOException {
		try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
			request = createRequest("POST",
					URI,
					"{\"name\":\"Тарантино\"}"
			);
			client.execute(request);

			request = createRequest("POST",
					URI,
					"{\"name\":\"Рефн\"}"
			);
			client.execute(request);
		}
	}

	@Test
	@Order(1)
	void createDirector() throws IOException {
		try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
			request = createRequest("POST",
					URI,
					"{\"name\":\"Тарковский\"}");
			response = client.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();
			assertEquals(HttpStatus.SC_OK, statusCode);

			String jsonDirector = EntityUtils.toString(response.getEntity());
			assertEquals("{\"id\":1,\"name\":\"Тарковский\"}", jsonDirector);
		}
	}

	@Test
	@Order(2)
	void createDirectorWhoAlreadyAdded() throws IOException {
		try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
			request = createRequest("POST",
					URI,
					"{\"name\":\"Тарковский\"}");
			response = client.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();
			assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode);
		}
	}

	@Test
	@Order(3)
	void getAllDirectors() throws IOException {
		fillDB();
		try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
			request = createRequest("GET",
					URI,
					null);
			response = client.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();
			assertEquals(HttpStatus.SC_OK, statusCode);

			String jsonDirectors = EntityUtils.toString(response.getEntity());
			assertEquals("[" +
							"{\"id\":1,\"name\":\"Тарковский\"}," +
							"{\"id\":2,\"name\":\"Тарантино\"}," +
							"{\"id\":3,\"name\":\"Рефн\"}" +
							"]",
					jsonDirectors);
		}
	}

	@Test
	@Order(4)
	void getDirectorById() throws IOException {
		try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
			request = createRequest("GET",
					URI + "/3",
					null);
			response = client.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();
			assertEquals(HttpStatus.SC_OK, statusCode);

			String jsonDirector = EntityUtils.toString(response.getEntity());
			assertEquals("{\"id\":3,\"name\":\"Рефн\"}", jsonDirector);
		}
	}

	@Test
	@Order(5)
	void getDirectorByIdWhoIsNotExist() throws IOException {
		try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
			request = createRequest("GET",
					URI + "/999",
					null);
			response = client.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();
			assertEquals(HttpStatus.SC_NOT_FOUND, statusCode);
		}
	}

	@Test
	@Order(6)
	void deleteDirector() throws IOException {
		try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
			request = createRequest(
					"DELETE",
					URI + "/1",
					null);
			response = client.execute(request);

			request = createRequest("GET",
					URI,
					null);
			response = client.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();
			assertEquals(HttpStatus.SC_OK, statusCode);

			String jsonDirectors = EntityUtils.toString(response.getEntity());
			assertEquals("[" +
							"{\"id\":2,\"name\":\"Тарантино\"}," +
							"{\"id\":3,\"name\":\"Рефн\"}" +
							"]",
					jsonDirectors);
		}
	}

	@Test
	@Order(7)
	void updateDirector() throws IOException {
		try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
			request = createRequest("PUT",
					URI,
					"{\"id\":2,\"name\":\"Ричи\"}");
			response = client.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();
			assertEquals(HttpStatus.SC_OK, statusCode);

			request = createRequest("GET",
					URI + "/2",
					null);
			response = client.execute(request);

			String jsonDirector = EntityUtils.toString(response.getEntity());
			assertEquals("{\"id\":2,\"name\":\"Ричи\"}", jsonDirector);
		}
	}

	@Test
	@Order(8)
	void updateDirectorWhoIsNotExist() throws IOException {
		try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
			request = createRequest("PUT",
					URI,
					"{\"id\":222,\"name\":\"Ричи\"}");
			response = client.execute(request);

			int statusCode = response.getStatusLine().getStatusCode();
			assertEquals(HttpStatus.SC_NOT_FOUND, statusCode);
		}
	}
}