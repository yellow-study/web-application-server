package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHandler extends Thread {

	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
			connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

			HttpRequest request = new HttpRequest(in);
			HttpResponse response = new HttpResponse(out);
			String path = getDefualtPath(request.getPath());

			if ("/user/create".equals(path)) {
				User user = new User(
					request.getParameter("userId"),
					request.getParameter("password"),
					request.getParameter("name"),
					request.getParameter("email"));
				DataBase.addUser(user);

				response.sendRedirect("/index.html");
			} else if ("/user/login".equals(path)) {
				User user = DataBase.findUserById(request.getParameter("userId"));

				if (user == null) {
					response.sendRedirect("/user/login_failed.html");
					return;
				}

				if (user.getPassword().equals(request.getParameter("password"))) {
					response.addHeader("Set-Cookie", "logined=true;path=/");
					response.sendRedirect("/index.html");
				} else {
					response.sendRedirect("/user/login_failed.html");
				}

			} else if ("/user/list".equals(path)) {

				if (!isLogin(request.getHeader("Cookie"))) {
					response.sendRedirect("/user/login.html");
					return;
				}
				String html = getUserListPage();
				response.forwordBody(html);

			} else {
				response.forward(path);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private boolean isLogin(String cookieValue) {
		Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieValue);
		String value = cookies.get("logined");

		if (value == null) {
			return false;
		}
		return Boolean.parseBoolean(value);
	}

	private String getDefualtPath(String path) {
		if (path.equals("/")) {
			return "/index.html";
		}
		return path;
	}

	private String getUserListPage() {
		List<User> userList = new ArrayList<>(DataBase.findAll());

		StringBuilder sb = new StringBuilder(
			"<html><head><title>사용자 목록</title></head><body><h1>사용자목록</h1>\n");

		for (User user : userList) {
			sb.append("<div>" + user.toString() + "</div>");
		}

		return sb.append("</body></html>").toString();
	}
}