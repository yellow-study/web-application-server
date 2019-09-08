package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final String GET = "GET";
	private static final String POST = "POST";
	private static final String URL_DELIMITER = " ";
	private static final int NOT_EXISTS = -1;
	private static final String JOIN_URI = "/user/create";
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
			connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

			//TODO service method - http method 를 판별하여 doXXXMethod를 호출한다. 
			String firstLine = reader.readLine();

			if (firstLine == null) {
				return;
			}

			log.debug(firstLine);

			String headerInfo;
			HttpRequestUtils.Pair contentLength = null;

			while (!"".equals(headerInfo = reader.readLine())) {
				if (headerInfo.startsWith("Content-Length")) {
					contentLength = HttpRequestUtils.parseHeader(headerInfo);
				}

				log.debug(headerInfo);
			}

			String[] tokens = firstLine.split(URL_DELIMITER);

			String method = tokens[0];
			String url = tokens[1];

			int statusCode = 0;

			if (GET.equals(method)) {
				int questionMarkIndex = url.indexOf("?");

				if (questionMarkIndex != NOT_EXISTS) {
					String uri = url.substring(0, questionMarkIndex);
					String queryString = url.substring(questionMarkIndex + 1);
					Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);

					if (JOIN_URI.equals(uri)) {
						User user = new User(params.get("userId"), params.get("password"), params.get("name"),
							params.get("email"));
						DataBase.addUser(user);
						statusCode=302;
						url = "/index.html";
					}
				}
			}

			if (POST.equals(method)) {
				if (contentLength != null) {
					String body = IOUtils.readData(reader, Integer.parseInt(contentLength.getValue()));
					Map<String, String> params = HttpRequestUtils.parseQueryString(body);

					if (JOIN_URI.equals(url)) {
						User user = new User(params.get("userId"), params.get("password"), params.get("name"),
							params.get("email"));
						DataBase.addUser(user);
						statusCode=302;
						url = "/index.html";
					}
				}
			}
			responseClient(out, statusCode, url);

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseClient(OutputStream out, int statusCode, String DestinationUrl) throws IOException {
		DataOutputStream dos = new DataOutputStream(out);

		if(statusCode == 302) {
			response302Header(dos, DestinationUrl);
		}else {
			byte[] body = Files.readAllBytes(new File("./webapp" + DestinationUrl).toPath());
			response200Header(dos, body.length);
			responseBody(dos, body);
		}
	}
	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302Header(DataOutputStream dos, String location) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: "+ location +"\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}