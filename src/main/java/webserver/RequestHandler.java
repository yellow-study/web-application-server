package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import util.HttpRequestUtils;
import util.IOUtils;
import webserver.handler.HttpRequestHandler;
import webserver.handler.StaticResourceHandler;
import webserver.handler.UserRegisterHandler;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	private Map<String, HttpRequestHandler> handlers;

	private HttpRequestHandler staticResourceHandler;

	private static final String LINE_BREAK = "\r\n";

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
		initConfig();
	}

	private void initConfig() {
		staticResourceHandler = new StaticResourceHandler();
		handlers = new HashMap<>();
		handlers.put("/user/create", new UserRegisterHandler());
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
			connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
			HttpRequest request = handleRequest(br);
			HttpResponse httpResponse = handleAction(request.getUrl(), request);
			handleResponse(out, httpResponse);

			log.info("users : {}", DataBase.findAll());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private HttpRequest handleRequest(BufferedReader br) throws IOException {
		String line = br.readLine();

		HttpRequest request = HttpRequest.create(line);

		while (!"".equals(line)) {
			line = br.readLine();
			request.parseHeaderLine(line);
		}

		Integer contentLength = MapUtils.getInteger(request.getHeader(), "Content-Length");
		if (StringUtils.equalsIgnoreCase(request.getMethod(), "POST") && contentLength != null) {
			String data = IOUtils.readData(br, contentLength);
			request.setBody(HttpRequestUtils.parseValues(data, "&"));
		}
		log.debug("request : {}", request);
		return request;
	}

	private HttpResponse handleAction(String url, HttpRequest request) throws Exception {
		HttpResponse httpResponse = null;

		if (handlers.containsKey(url)) {
			HttpRequestHandler handler = handlers.get(url);
			httpResponse = handler.execute(request);
		} else {
			httpResponse = staticResourceHandler.execute(request);
		}

		return httpResponse;
	}

	private void handleResponse(OutputStream out, HttpResponse response) throws IOException {
		byte[] body = response.getBody();
		DataOutputStream dos = new DataOutputStream(out);
		responseHeader(dos, response.getResultStatus(), response.getHeader());
		responseBody(dos, body);
	}

	private void responseHeader(DataOutputStream dos, String resultStatus, Map<String, String> header) {
		try {
			dos.writeBytes(resultStatus + LINE_BREAK);
			Set<String> keys = header.keySet();
			for (String key : keys) {
				String value = header.get(key);
				dos.writeBytes(key + ": " + value + LINE_BREAK);
			}
			dos.writeBytes(LINE_BREAK);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}
