package webserver.http;

import com.sun.deploy.net.HttpUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 5.
 */


@ToString
public class HttpRequest {
	@Getter
	private String method;
	@Getter
	private String url;
	@Getter
	private String httpVersion;

	private Map<String, String> body;
	private Map<String, String> header;

	public static HttpRequest create(BufferedReader bufferedReader) throws IOException {
		HttpRequest httpRequest = new HttpRequest();

		String line = bufferedReader.readLine();
		String[] request = line.split(" ");
		httpRequest.method = request[0];
		httpRequest.url = request[1];
		httpRequest.httpVersion = request[2];

		String urlAndParam = request[1];

		int index = urlAndParam.indexOf("?");

		if (index >= 0) {
			String params = urlAndParam.substring(index + 1);
			httpRequest.url = urlAndParam.substring(0, index);
			httpRequest.body = HttpRequestUtils.parseQueryString(params);
		}

		while (!"".equals(line)) {
			line = bufferedReader.readLine();
			httpRequest.parseHeaderLine(line);
		}

		if(StringUtils.equalsIgnoreCase(httpRequest.method, "POST")) {
			Integer contentLength = MapUtils.getInteger(httpRequest.header, "Content-Length");
			String data = IOUtils.readData(bufferedReader, contentLength);
			httpRequest.body = HttpRequestUtils.parseValues(data, "&");
		}

		return httpRequest;
	}

	private void parseHeaderLine(String line) {
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

	public String getHeaderValue(String key) {
		return MapUtils.getString(this.header, (key));
	}

	public String getBodyValue(String key) {
		return MapUtils.getString(this.body, (key));
	}
}
