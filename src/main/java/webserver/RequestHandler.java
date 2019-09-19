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
import java.util.Collection;
import java.util.List;
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
	private static final String LOGIN_URI = "/user/login";

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
			String requestLine = reader.readLine();

			if (requestLine == null) {
				return;
			}

			log.debug(requestLine);

			String requestHeader;
			HttpRequestUtils.Pair contentLength = null;
			HttpRequestUtils.Pair contentTypePair = null;
			String contentType = null;

			while (!"".equals(requestHeader = reader.readLine())) {
				if (requestHeader.startsWith("Content-Length")) {
					contentLength = HttpRequestUtils.parseHeader(requestHeader);

				} else if (requestHeader.startsWith("Accept:")) {
					contentTypePair = HttpRequestUtils.parseHeader(requestHeader);
					int contentTypeIndex = contentTypePair.getValue().indexOf(",");

					if (contentTypeIndex <= NOT_EXISTS) {
						contentType = contentTypePair.getValue();
					} else {
						contentType = contentTypePair.getValue().substring(0, contentTypeIndex);
					}
				}
				log.debug(requestHeader);
			}

			String[] tokens = requestLine.split(URL_DELIMITER);

			String method = tokens[0];
			String url = tokens[1];

			DataOutputStream dos = new DataOutputStream(out);

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

						url = "/index.html";
						response302Header(dos, url);
					}
				} else {

					if("USER_LIST_URL".equals(url)) {
						Collection<User> users = DataBase.findAll();
						for(User user : users) {
							
						}
					} else {
						make200Response(dos, contentType, url);
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

						url = "/index.html";
						response302Header(dos, url);

					} else if (LOGIN_URI.equals(url)) {

						User user = DataBase.findUserById(params.get("userId"));

						if (user == null) {
							url = "/user/login_failed.html";
							response302LoginHeader(dos, false, url);
						} else {
							if (user.getPassword().equals(params.get("password"))) {
								url = "/index.html";
								response302LoginHeader(dos, true, url);
							} else {
								url = "/user/login_failed.html";
								response302LoginHeader(dos, false, url);
							}
						}
					} else {
						make200Response(dos, contentType, url);
					}
				}
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void make200Response(DataOutputStream dos, String contentType, String url)
		throws IOException {

		byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
		response200Header(dos, contentType, body.length);
		responseBody(dos, body);

	}

	private void response200Header(DataOutputStream dos, String contentType, int bodyLength) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + bodyLength + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302Header(DataOutputStream dos, String url) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: " + url + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302LoginHeader(DataOutputStream dos, boolean status, String url) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Set-Cookie: logined=" + status + "; Path=/\r\n");
			dos.writeBytes("Location: " + url + "\r\n");
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