package webserver.http;

import com.sun.deploy.net.HttpUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import util.HttpRequestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 5.
 */
@Getter
@Setter
@ToString
public class HttpRequest {
	private String method;
	private String url;
	private String httpVersion;

	private Map<String, String> body;
	private Map<String, String> header;

	public static HttpRequest create(String line) {
		HttpRequest httpRequest = new HttpRequest();
		String[] request = line.split(" ");
		httpRequest.setMethod(request[0]);
		httpRequest.setUrl(request[1]);
		httpRequest.setHttpVersion(request[2]);

		String urlAndParam = request[1];

		int index = urlAndParam.indexOf("?");

		if (index >= 0) {
			String params = urlAndParam.substring(index + 1);
			httpRequest.setUrl(urlAndParam.substring(0, index));
			httpRequest.setBody(HttpRequestUtils.parseQueryString(params));
		}

		return httpRequest;
	}

	public void parseHeaderLine(String line) {
		if (StringUtils.isBlank(line)) {
			return;
		}

		if (header == null) {
			header = new HashMap<>();
		}

		String[] data = StringUtils.split(line, ":");
		if (header.containsKey(data[0])) {
			header.replace(data[0], data[1].trim());
		} else {
			header.put(data[0], data[1].trim());
		}
	}
}
