package webserver.http;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 26.
 */
public class HttpResponse {
    private static final Logger LOG = LoggerFactory.getLogger(HttpResponse.class);

    private Map<String, String> header = new HashMap<>();

    private DataOutputStream dos;

    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
    }

    public void forward(String url) throws IOException {
        if (url.endsWith(".css")) {
            header.put("Content-Type:", "text/css");
        } else if (url.endsWith(".js")) {
            header.put("Content-Type:", "application/javascript");
        } else {
            header.put("Content-Type:", "text/html;charset=utf-8");
        }

        byte[] body = Files.readAllBytes(viewResolver(url));
        ResponseHandler.response200Header(dos, header, body);
    }

    public void responseBody(byte[] body) throws IOException {
        header.put("Content-Type:", "text/html;charset=utf-8");
        ResponseHandler.response200Header(dos, header, body);
    }

    public void sendRedirect(String redirectUrl) {
        ResponseHandler.response302Header(dos, header, redirectUrl);
    }

    private Path viewResolver(String url) {
        if (StringUtils.equals(url, "/")) {
            return new File("./webapp/index.html").toPath();
        }
        return new File("./webapp" + url).toPath();
    }
}
