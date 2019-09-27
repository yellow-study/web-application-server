/*
 * Copyright 2019 Naver Corp. All rights Reserved.
 * Naver PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package webserver;

import util.HttpRequestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequest {
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
	private static final String URL_DELIMITER = " ";
	private static final int NOT_EXISTS = -1;
	private Map<String, String> headers;
	private Map<String, String> parameters;
	private String method;
	private String path;

	public HttpRequest(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		String requestLine = reader.readLine();
		String[] tokens = requestLine.split(URL_DELIMITER);
		headers = new HashMap<>();

		method = tokens[0];
		String url = tokens[1];
		String headerLine;

		while (!"".equals(headerLine = reader.readLine())) {
			HttpRequestUtils.Pair headerPair = HttpRequestUtils.parseHeader(headerLine);
			headers.put(headerPair.getKey(), headerPair.getValue());
		}

		if (method.equals(("GET"))) {
			int questionMarkIndex = url.indexOf("?");

			if (questionMarkIndex != NOT_EXISTS) {
				path = url.substring(0, questionMarkIndex);
				String queryString = url.substring(questionMarkIndex + 1);
				parameters = HttpRequestUtils.parseQueryString(queryString);
			} else {
				path = url;
			}
		} else if (method.equals("POST")) {
			path = url;
			String bodyLine = reader.readLine();
			parameters = HttpRequestUtils.parseQueryString(bodyLine);
		}
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public String getParameter(String key) {
		if (parameters != null) {
			return parameters.get(key);
		} else {
			return null;
		}
	}
}
