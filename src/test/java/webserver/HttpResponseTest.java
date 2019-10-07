/*
 * Copyright 2019 Naver Corp. All rights Reserved.
 * Naver PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package webserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

public class HttpResponseTest {
	private String testDirectory = "./src/test/resources/";

	@Test
	public void responseForward() throws Exception {
		HttpResponse response = new HttpResponse(createOutputStream("Http_Forward.txt"));
		response.forward("/index.html");
	}
	
	@Test
	public void responseRedirect() throws Exception {
		HttpResponse response = new HttpResponse(createOutputStream("Http_Redirect.txt"));
		response.sendRedirect("/index.html");
	}
	
	@Test
	public void responseCookies() throws Exception {
		HttpResponse response = new HttpResponse(createOutputStream("Http_Cookies.txt"));
		response.addHeader("Set-Cookie", "logined=true;path=/");
		response.sendRedirect("/index.html");
	}
	
	private OutputStream createOutputStream(String fileName) throws FileNotFoundException {
		return new FileOutputStream(new File(testDirectory + fileName));
	}
}