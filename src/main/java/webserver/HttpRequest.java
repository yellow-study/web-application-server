package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	private static final String URL_DELIMITER = " ";
	private static final int NOT_EXISTS = -1;
	private String method;
	private String path;
	private Map<String, String> header;
	private Map<String, String> parameter;

	public HttpRequest(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		header = new HashMap<>();

		String requestLine = reader.readLine();
		String[] tokens = requestLine.split(URL_DELIMITER);
		method = tokens[0];
		String url = tokens[1];

		String headerInfo;
		String contentLength = null;

		while (!"".equals(headerInfo = reader.readLine())) {
			HttpRequestUtils.Pair headerPair = HttpRequestUtils.parseHeader(headerInfo);
			String key = headerPair.getKey();
			if (key.equals("Content-Length")) {
				contentLength = headerPair.getValue();
			}
			header.put(key, headerPair.getValue());
		}

		if (method.equals("GET")) {
			int questionMarkIndex = url.indexOf("?");

			if (questionMarkIndex != NOT_EXISTS) {
				url = url.substring(0, questionMarkIndex);
				String queryString = url.substring(questionMarkIndex + 1);
				parameter = HttpRequestUtils.parseQueryString(queryString);
			} else {
				path = url;
			}
		} else if (method.equals("POST")) {
			if (contentLength != null) {
				String requestBody = IOUtils.readData(reader, Integer.parseInt(contentLength));
				parameter = HttpRequestUtils.parseQueryString(requestBody);
			}
		}
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHeader(String key) {
		return header.get(key);
	}

	public void setHeader(Map<String, String> header) {
		this.header = header;
	}

	public String getParameter(String key) {
		if (parameter != null) {
			return parameter.get(key);
		} else {
			return null;
		}
	}

	public void setParameter(Map<String, String> parameter) {
		this.parameter = parameter;
	}

}
