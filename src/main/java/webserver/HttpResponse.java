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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
	private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
	private DataOutputStream dos = null;
	private Map<String, String> headers = new HashMap<String, String>();

	public HttpResponse(OutputStream out) {
		dos = new DataOutputStream(out);
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

			response200Header();
			responseBody(body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public void forwordBody(String body) {
		byte[] contents = body.getBytes();

		headers.put("Content-type", "text/html;charset=utf-8");
		headers.put("Content-Length", contents.length + "");

		response200Header();
		responseBody(contents);
	}

	public void response200Header() {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			processHeaders();
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public void responseBody(byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}

	}

	public void sendRedirect(String redirectUrl) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			processHeaders();
			dos.writeBytes("Location: " + redirectUrl + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public void processHeaders() {
		try {
			Set<String> keys = headers.keySet();

			for (String key : keys) {
				dos.writeBytes(key + ": " + headers.get(key) + " \r\n");
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}

	}
}