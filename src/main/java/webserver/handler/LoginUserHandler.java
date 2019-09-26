package webserver.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import db.DataBase;
import model.User;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

/**
 * @author sungryul-yook on 2019-09-26.
 */
public class LoginUserHandler implements HttpRequestHandler {
	@Override
	public HttpResponse execute(HttpRequest request) throws Exception {
		String id = request.getBodyValue("userId");
		String password = request.getBodyValue("password");

		User user = DataBase.findUserById(id);

		boolean isLogin = false;

		if (!Objects.isNull(user)) {
			isLogin = StringUtils.equals(user.getPassword(), password);
		}

		HttpResponse response = new HttpResponse();
		response.setResultStatus("HTTP/1.1 302 Found");
		Map<String, String> header = new HashMap<>();
		header.put("Location", isLogin ? "/index.html" : "/user/login_failed.html");
		header.put("Set-Coookie", "logined=" + isLogin);
		response.setHeader(header);
		return response;
	}
}
