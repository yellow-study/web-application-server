/*
 * Copyright 2019 Naver Corp. All rights Reserved.
 * Naver PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package webserver;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class RequestLine {
	private static final Logger log = LoggerFactory.getLogger(RequestLine.class);
	private static final String URL_DELIMITER = " ";
	private static final int NOT_EXISTS = -1;
	private static final int REQUEST_LINE_DATA_SIZE = 3;

	private HttpMethod method;
	private String path;
	private Map<String, String> parameters = new HashMap<String, String>();

	public RequestLine(String requestLine) {
		log.debug("request line : {}", requestLine);

		String[] tokens = requestLine.split(URL_DELIMITER);

		if (tokens.length != REQUEST_LINE_DATA_SIZE) {
			throw new IllegalArgumentException(requestLine + "이형식에 맞지 않습니다.");
		}

		method = HttpMethod.valueOf(tokens[0]);
		String url = tokens[1];

		if (method.isPost()) {
			path = url;
			return;
		}

		int questionMarkIndex = url.indexOf("?");

		if (questionMarkIndex == NOT_EXISTS) {
			path = url;
		} else {
			path = url.substring(0, questionMarkIndex);
			String queryString = url.substring(questionMarkIndex + 1);
			parameters = HttpRequestUtils.parseQueryString(queryString);
		}
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public Map<String, String> getParameter() {
		return parameters;
	}
}
