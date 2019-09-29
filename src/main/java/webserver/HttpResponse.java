/*
 * Copyright 2019 Naver Corp. All rights Reserved.
 * Naver PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
	private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
	private DataOutputStream dos;
	private Map<String, String> headers;

	public HttpResponse(OutputStream out) {
		dos = new DataOutputStream(out);
		headers = new HashMap<String, String>();
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	public void forward(String url) {

		try {
			byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

			if (url.endsWith(".css")) {
				addHeader("Content-type", "text/css");
			} else if (url.endsWith(".js")) {
				addHeader("Content-type", "application/javascript");
			} else {
				addHeader("Content-type", "text/html;charset=utf-8");
			}
			addHeader("Content-Length", body.length + "");
			responseBody(body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public void forwardBody(String body) {

	}

	public void resonse200Header(int bodyLength) {

	}

	public void responseBody(byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}

	}

	public void sendRedirect(String path) {

	}

	public void processHeaders() {

	}
}
