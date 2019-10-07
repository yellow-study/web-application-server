/*
 * Copyright 2019 Naver Corp. All rights Reserved.
 * Naver PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, String> parameters = new HashMap<String, String>();
	private RequestLine requestLine;

	public HttpRequest(InputStream in) {

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			String line = reader.readLine();

			if (line == null) {
				return;
			}
			requestLine = new RequestLine(line);

			String headerLine;

			while (!"".equals(headerLine = reader.readLine())) {
				log.debug("header line : {}", headerLine);
				HttpRequestUtils.Pair headerPair = HttpRequestUtils.parseHeader(headerLine);
				headers.put(headerPair.getKey(), headerPair.getValue());
			}

			if (getMethod() == HttpMethod.POST) {
				if (headers.containsKey("Content-Length") || true) {
					String body = IOUtils.readData(reader, Integer.parseInt(headers.get("Content-Length")));
					parameters = HttpRequestUtils.parseQueryString(body);
				}
			} else {
				parameters = requestLine.getParameter();
			}
		} catch (IOException io) {
			log.error(io.getMessage());
		}

	}

	public HttpMethod getMethod() {
		return requestLine.getMethod();
	}

	public String getPath() {
		return requestLine.getPath();
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public String getParameter(String key) {
		return parameters.get(key);
	}
}
