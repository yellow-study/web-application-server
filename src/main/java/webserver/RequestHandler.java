package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
			connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream(); BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line = br.readLine();
			int contentLength = 0;

			Map<String, String> cookieMap = new HashMap<>();
			cookieMap.put("logined", "false");

			if (line == null) {
				return;
			}

			String url = HttpRequestUtils.getUrl(line);

			while (!"".equals(line)) {
				log.info("http request string : " + line);
				line = br.readLine();

				if (line.contains("Content-Length")) {
					contentLength = Integer.parseInt(line.split(" ")[1]);
				} else if (line.contains("Cookie")) {
					cookieMap = HttpRequestUtils.parseCookies(line.split(" ")[1]);
				}
			}

			if ("/user/create".equals(url)) {
				String paramString = IOUtils.readData(br, contentLength);

				Map<String, String> param = HttpRequestUtils.parseQueryString(paramString);

				User user = (User)getModel(param, User.class);

				DataBase.addUser(user);

				url = "/index.html";

				DataOutputStream dos = new DataOutputStream(out);
				response302Header(dos, url);

			} else if ("/user/login".equals(url)) {
				String paramString = IOUtils.readData(br, contentLength);

				Map<String, String> param = HttpRequestUtils.parseQueryString(paramString);

				User user = DataBase.findUserById(param.get("userId"));

				if (user != null && user.getPassword().equals(param.get("password"))) {

					DataOutputStream dos = new DataOutputStream(out);
					response302LoginSuccessHeader(dos);
				} else {
					responseResource(out, "/user/login_failed.html");
				}

			} else if ("/user/list.html".equals(url)) {
				if (!Boolean.parseBoolean(cookieMap.get("logined"))) {
					responseResource(out, "/user/login.html");

					return;
				}

				Collection<User> users = DataBase.findAll();

				byte[] body = getUserListByHtml(users).toString().getBytes();

				DataOutputStream dos = new DataOutputStream(out);
				response200Header(dos, body.length);
				responseBody(dos, body);

			} else if (url.endsWith(".css")) {
				DataOutputStream dos = new DataOutputStream(out);
				byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
				response200CssHeader(dos, body.length);
				responseBody(dos, body);

			} else {
				DataOutputStream dos = new DataOutputStream(out);
				byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
				response200Header(dos, body.length);
				responseBody(dos, body);
			}

		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	private StringBuffer getUserListByHtml(Collection<User> users) {
		StringBuffer sb = new StringBuffer();
		sb.append("<table border='1'>");

		for (User user : users) {
			sb.append("<tr>")
				.append("<td>" + user.getUserId() + "</td>")
				.append("<td>" + user.getName() + "</td>")
				.append("<td>" + user.getEmail() + "</td>")
				.append("<tr>");
		}

		sb.append("</table>");
		return sb;
	}

	private Object getModel(Map<String, String> param, Class clazz) throws Exception {
		Constructor constructor = clazz.getConstructor();
		Object object = constructor.newInstance();

		Method[] methods = clazz.getMethods();

		for (Method method : methods) {
			if ("set".equals(method.getName().substring(0, 3))) {
				String fieldName = method.getName().substring(3);

				method.invoke(object, param.get(changeToLowerCaseForFirstString(fieldName)));
			}
		}

		return object;
	}

	private String changeToLowerCaseForFirstString(String str) {
		String transString = str.substring(0, 1);
		transString = transString.toLowerCase();
		transString += str.substring(1);

		return transString;
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

	private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/css\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302Header(DataOutputStream dos, String url) {
		try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Location: " + url + " \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302LoginSuccessHeader(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Set-Cookie: logined=true \r\n");
			dos.writeBytes("Location: /index.html \r\n");
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

	private void responseResource(OutputStream out, String url) throws IOException {
		DataOutputStream dos = new DataOutputStream(out);
		byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
		response200Header(dos, body.length);
		responseBody(dos, body);
	}
}
