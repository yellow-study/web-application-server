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
		User user = User.builder()
			.userId(request.getBodyValue("userId"))
			.password(request.getBodyValue("password"))
			.name(request.getBodyValue("name"))
			.email(request.getBodyValue("email"))
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
