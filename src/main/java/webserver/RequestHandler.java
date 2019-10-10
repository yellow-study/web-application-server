package webserver;

import db.DataBase;
import model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHandler extends Thread {
	private static final String GET = "GET";
	private static final String POST = "POST";
	private static final String URL_DELIMITER = " ";
	private static final int NOT_EXISTS = -1;
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
				connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

			HttpRequest httpRequest = new HttpRequest(in);
			HttpResponse httpResponse = new HttpResponse(out);

			if ("/user/create".equals(httpRequest.getPath())) {
				User user = new User(httpRequest.getParameter("userId"),
                                     httpRequest.getParameter("password"),
                                     httpRequest.getParameter("name"),
                                     httpRequest.getParameter("email"));
				DataBase.addUser(user);

				httpResponse.sendRedirect("../index.html", false);
			}

			if ("/user/login".equals(httpRequest.getPath())) {
				User user = DataBase.findUserById(httpRequest.getParameter("userId"));
				String url;
				boolean logined = false;

				if (user == null) {
					url = "login_failed.html";
				} else {
					if (user.getPassword().equals(httpRequest.getParameter("password"))) {
						url = "../index.html";
						logined = true;
					} else {
						url = "login_failed.html";
					}
				}

				httpResponse.sendRedirect(url, logined);
			}

            if ("/user/list".equals(httpRequest.getPath())) {
                if (httpRequest.getParameter()) {
                    String html = getUserListPage();
                    responseBody = html.getBytes();

                } else {
                    response302Header(dos, "login.html", isLogined);
                }
            }

//            if (GET.equals(method)) {
//                doGet(cookies, url, dos);
//            } else if (POST.equals(method)) {
//                doPost(reader, contentLength, url, dos);
//            }

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void doPost(BufferedReader reader, HttpRequestUtils.Pair contentLength, String url, DataOutputStream dos) throws IOException {
		if (contentLength != null) {
			String requestBody = IOUtils.readData(reader, Integer.parseInt(contentLength.getValue()));
			Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);

			if ("/user/create".equals(url)) {
				User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
				DataBase.addUser(user);
				url = "/index.html";

				HttpResponse.sendRedirect(dos, "../index.html", false);
			}

			if ("/user/login".equals(url)) {
				User user = DataBase.findUserById(params.get("userId"));
				boolean logined = false;

				if (user == null) {
					url = "login_failed.html";
				} else {
					if (user.getPassword().equals(params.get("password"))) {
						url = "../index.html";
						logined = true;
					} else {
						url = "login_failed.html";
					}
				}

				response302Header(dos, url, logined);
			}
		}
	}

	private void doGet(Map<String, String> cookies, String url, DataOutputStream dos) throws IOException {
		byte[] responseBody;
		int questionMarkIndex = url.indexOf("?");

		if (questionMarkIndex != NOT_EXISTS) {
			String uri = url.substring(0, questionMarkIndex);
			String queryString = url.substring(questionMarkIndex + 1);
			Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);

			if ("/user/create".equals(uri)) {
				User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
				DataBase.addUser(user);
				log.debug("uri " + uri);
			}
		} else {
			String contentType = "text/html";
			responseBody = Files.readAllBytes(new File("./webapp" + url).toPath());

			if ("/user/list".equals(url)) {
				boolean isLogined = Boolean.parseBoolean(cookies.get("logined"));

				if (isLogined) {
					String html = getUserListPage();
					responseBody = html.getBytes();

				} else {
					response302Header(dos, "login.html", isLogined);
				}
			} else if (url.endsWith(".css")) {
				contentType = "text/css";
			}

			response200Header(dos, contentType, responseBody.length);
			responseBody(dos, responseBody);
		}
	}

	private String getUserListPage() {
		List<User> userList = new ArrayList<>(DataBase.findAll());

		StringBuilder sb = new StringBuilder(
				"<html><head><title>사용자 목록</title></head><body><h1>사용자목록</h1>\n");

		for (int index = 0; index < userList.size(); index++) {
			sb.append("<div>" + index + ". " + userList.get(index) + "</div>");
		}

		return sb.append("</body></html>").toString();
	}

//    private void response200Header(DataOutputStream dos, String contentType, int lengthOfBodyContent) {
//        try {
//            dos.writeBytes("HTTP/1.1 200 OK \r\n");
//            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
//            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
//            dos.writeBytes("\r\n");
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }
//
//    private void response302Header(DataOutputStream dos, String url, boolean logined) {
//        try {
//            dos.writeBytes("HTTP/1.1 302 Found \r\n");
//            dos.writeBytes("Set-Cookie: logined=" + logined + "; Path=/\r\n");
//            dos.writeBytes("Location: " + url + "\r\n");
//            dos.writeBytes("\r\n");
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }
//
//    private void responseBody(DataOutputStream dos, byte[] body) {
//        try {
//            dos.write(body, 0, body.length);
//            dos.flush();
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }
}
