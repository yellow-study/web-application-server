/*
 * Copyright 2019 Naver Corp. All rights Reserved.
 * Naver PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package webserver;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class RequestLineTest {

	@Test
	public void create_method() {
		RequestLine requestLine = new RequestLine("GET /index.html HTTP/1.1");

		assertEquals(HttpMethod.GET, requestLine.getMethod());
		assertEquals("/index.html", requestLine.getPath());

		requestLine = new RequestLine("POST /index.html HTTP/1.1");
		assertEquals(HttpMethod.POST, requestLine.getMethod());
		assertEquals("/index.html", requestLine.getPath());
	}

	@Test
	public void create_path_and_params() {
		RequestLine requestLine = new RequestLine("GET /user/create?userId=test&password=pass HTTP/1.1");

		assertEquals(HttpMethod.GET, requestLine.getMethod());
		assertEquals("/user/create", requestLine.getPath());

		Map<String, String> params = requestLine.getParameter();
		assertEquals(2, params.size());
		assertEquals("test", params.get("userId"));
		assertEquals("pass", params.get("password"));
	}
}
