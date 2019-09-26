package webserver.handler;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

/**
 * @author sungryul-yook on 2019-09-19.
 */
@Slf4j
public class StaticResourceHandler implements HttpRequestHandler {
	@Override
	public HttpResponse execute(HttpRequest request) throws Exception {
		HttpResponse httpResponse = new HttpResponse();
		String url = request.getUrl();
		httpResponse.setBody(Files.readAllBytes(new File("./webapp" + url).toPath()));
		httpResponse.setResultStatus("HTTP/1.1 200 OK");

		Map<String, String> header = new HashMap<>();

		if (!Objects.isNull(request.getHeaderValue("Accept"))) {
			String accept = request.getHeaderValue("Accept");
			String contentType = StringUtils.split(accept, ",")[0];
			if (StringUtils.equals(contentType, "text/css")) {
				header.put("Content-Type", "text/css;charset=utf-8");
			}
		} else {
			header.put("Content-Type", "text/html;charset=utf-8");
		}

		if (ArrayUtils.isNotEmpty(httpResponse.getBody())) {
			header.put("Content-Length", String.valueOf(ArrayUtils.getLength(httpResponse.getBody())));
		}
		httpResponse.setHeader(header);
		return httpResponse;
	}
}
