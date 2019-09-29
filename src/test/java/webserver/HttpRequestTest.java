/*
 * Copyright 2019 Naver Corp. All rights Reserved.
 * Naver PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package webserver;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.hamcrest.Description;
import org.junit.Test;

public class HttpRequestTest {
	private String testDirectory = "./src/test/resources/";

	@Test
	public void request_GET() throws Exception {

		InputStream in = new FileInputStream(new File(testDirectory + "Http_GET.txt"));
		HttpRequest request = new HttpRequest(in);

		assertEquals(HttpMethod.GET, request.getMethod());
		assertEquals("/user/create", request.getPath());
		assertEquals("test", request.getParameter("userId"));
		assertEquals("keep-alive", request.getHeader("Connection"));
	}

	@Test
	public void request_POST() throws Exception {

		InputStream in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
		HttpRequest request = new HttpRequest(in);

		assertEquals(HttpMethod.POST, request.getMethod());
		assertEquals("/user/create", request.getPath());
		assertEquals("keep-alive", request.getHeader("Connection"));
		assertEquals("test", request.getParameter("userId"));
	}
}
