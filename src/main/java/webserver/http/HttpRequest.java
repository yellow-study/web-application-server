package webserver.http;

import org.apache.commons.lang3.StringUtils;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 22.
 */

public class HttpRequest {
    private static final String COOKIE = "Cookie";

    private static final int METHOD = 0;
    private static final int URL = 1;

    private String method;
    private String url;

    private Map<String, String> header = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();
    private Map<String, String> body = new HashMap<>();

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String line = bufferedReader.readLine();
        setRequestLine(line);

        line = bufferedReader.readLine();
        while (StringUtils.isNotBlank(line)) {
            setHeader(HttpRequestUtils.parseHeader(line));
            line = bufferedReader.readLine();
        }

        if (header.containsKey(COOKIE)) {
            setCookies(header.get(COOKIE));
        }

        if (header.containsKey("Content-Length")) {
            int contentLength = Integer.valueOf(getHeader("Content-Length"));
            setBody(bufferedReader, contentLength);
        }
    }

    private void setRequestLine(String line) throws IOException {
        if (line == null) {
            throw new IOException("request line is null");
        }

        String[] requestLine = line.split(StringUtils.SPACE);

        setMethod(requestLine[METHOD]);

        if (requestLine[URL].contains("?")) {
            int index = requestLine[URL].indexOf("?");
            setUrl(requestLine[URL].substring(0, index));
            setBody(requestLine[URL].substring(index + 1));
        } else {
            setUrl(requestLine[URL]);
        }
    }

    private void setMethod(String method) {
        this.method = method;
    }

    private void setUrl(String url) {
        this.url = url;
    }

    private void setHeader(HttpRequestUtils.Pair pair) {
        header.put(pair.getKey(), pair.getValue());
    }

    private void setCookies(String cookieLine) {
        cookies = HttpRequestUtils.parseCookies(cookieLine);
    }

    private void setBody(String paramString) {
        body.putAll(HttpRequestUtils.parseQueryString(paramString));
    }

    private void setBody(BufferedReader bufferedReader, int contentLength) throws IOException {
        String paramString = IOUtils.readData(bufferedReader, contentLength);
        setBody(paramString );
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getHeader(String key) {
        return header.get(key);
    }

    public String getCookie(String key) {
        return cookies.get(key);
    }

    public String getParameter(String key) {
        return body.get(key);
    }

}
