package com.temenos.useragent.generic;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.temenos.useragent.generic.http.HttpClient;
import com.temenos.useragent.generic.http.HttpHeader;
import com.temenos.useragent.generic.http.HttpMethod;
import com.temenos.useragent.generic.http.HttpRequest;
import com.temenos.useragent.generic.http.HttpResponse;
import com.temenos.useragent.generic.http.HttpResponseImpl;
import com.temenos.useragent.generic.http.HttpResult;
import com.temenos.useragent.generic.mediatype.HalJsonPayloadHandler;

public class DefaultInteractionSessionTest {

	private InteractionSession session = DefaultInteractionSession.newSession();
	private VerifiableMockHttpClient verifiableHttpClient = new VerifiableMockHttpClient();

	@Before
	public void setUp() {
		session.useHttpClient(verifiableHttpClient);
	}

	@Test
	public void testHttpInvocationProperties() {
		session.use(new HalJsonPayloadHandler().entity());
		verifiableHttpClient.responseToBeReturned = newHalJsonOKResponse();

		session.url("POST-test-url").post();
		assertEquals(HttpMethod.POST, verifiableHttpClient.invokedMethod);
		assertEquals("POST-test-url", verifiableHttpClient.invokedUrl);

		session.url("GET-test-url").get();
		assertEquals(HttpMethod.GET, verifiableHttpClient.invokedMethod);
		assertEquals("GET-test-url", verifiableHttpClient.invokedUrl);

		session.url("PUT-test-url").put();
		assertEquals(HttpMethod.PUT, verifiableHttpClient.invokedMethod);
		assertEquals("PUT-test-url", verifiableHttpClient.invokedUrl);

		session.url("DELETE-test-url").delete();
		assertEquals(HttpMethod.DELETE, verifiableHttpClient.invokedMethod);
		assertEquals("DELETE-test-url", verifiableHttpClient.invokedUrl);

	}

	@Test
	public void testSetForNewlyBuiltHalJsonEntity() {
		session.use(new HalJsonPayloadHandler().entity());
		session.set("foo", "bar");
		verifiableHttpClient.responseToBeReturned = newHalJsonOKResponse();

		session.url("http://mybank/services/BankServices/customers").post();
		assertTrue(verifiableHttpClient.invokedRequest.payload().contains(
				"\"foo\" : \"bar\""));
	}

	// returns a general purpose hal+json OK response
	private HttpResponseImpl newHalJsonOKResponse() {
		HttpHeader header = new HttpHeader();
		header.set("Content-Type", "application/hal+json");
		return new HttpResponseImpl(header, "{}", new HttpResult(200, "OK"));
	}

	// A HttpClient implementation for unit testing. This implementation
	// supports verification of the Http method calls by allowing the request
	// for inspection by intercepting the call and accepting response to be
	// returned.
	private static class VerifiableMockHttpClient implements HttpClient {

		// Http method that got invoked recently
		private HttpMethod invokedMethod;
		// Http url that got invoked recently
		private String invokedUrl;
		// Http request that was received during last invocation
		private HttpRequest invokedRequest;
		// Http response which is expected to be returned as a response for last
		// invocation
		private HttpResponse responseToBeReturned;

		@Override
		public HttpResponse get(String url, HttpRequest request) {
			invokedRequest = request;
			invokedUrl = url;
			invokedMethod = HttpMethod.GET;
			return responseToBeReturned;
		}

		@Override
		public HttpResponse post(String url, HttpRequest request) {
			invokedRequest = request;
			invokedUrl = url;
			invokedMethod = HttpMethod.POST;
			return responseToBeReturned;
		}

		@Override
		public HttpResponse put(String url, HttpRequest request) {
			invokedRequest = request;
			invokedUrl = url;
			invokedMethod = HttpMethod.PUT;
			return responseToBeReturned;
		}

		@Override
		public HttpResponse delete(String url, HttpRequest request) {
			invokedRequest = request;
			invokedUrl = url;
			invokedMethod = HttpMethod.DELETE;
			return responseToBeReturned;
		}
	}

}
