package webserver.handler;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

/**
 * @author sungryul-yook on 2019-09-19.
 */
public class StaticResourceHandler implements HttpRequestHandler {
	@Override
	public HttpResponse execute(HttpRequest request) throws Exception {
		HttpResponse httpResponse = new HttpResponse();
		String url = request.getUrl();
		httpResponse.setBody(Files.readAllBytes(new File("./webapp" + url).toPath()));
		httpResponse.setResultStatus("HTTP/1.1 200 OK");

		Map<String, String> header = new HashMap<>();
		header.put("Content-Type", "text/html;charset=utf-8");
		if (ArrayUtils.isNotEmpty(httpResponse.getBody())) {
			header.put("Content-Length", String.valueOf(ArrayUtils.getLength(httpResponse.getBody())));
		}
		httpResponse.setHeader(header);
		return httpResponse;
	}
}
