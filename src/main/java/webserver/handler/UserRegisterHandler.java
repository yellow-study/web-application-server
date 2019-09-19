package webserver.handler;

import java.util.HashMap;
import java.util.Map;

import db.DataBase;
import model.User;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

/**
 * @author sungryul-yook on 2019-09-19.
 */
public class UserRegisterHandler implements HttpRequestHandler {
	@Override
	public HttpResponse execute(HttpRequest request) throws Exception {
		Map<String, String> body = request.getBody();

		User user = User.builder()
			.userId(body.get("userId"))
			.password(body.get("password"))
			.name(body.get("name"))
			.email(body.get("email"))
			.build();
		DataBase.addUser(user);

		HttpResponse response = new HttpResponse();
		response.setResultStatus("HTTP/1.1 302 Found");
		Map<String, String> header = new HashMap<>();
		header.put("Location", "/index.html");
		response.setHeader(header);
		return response;
	}
}
