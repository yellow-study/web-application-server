package webserver.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 1.
 */
public class ResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(ResponseHandler.class);

    public static void response200Header(DataOutputStream dos, Map<String, String> header, byte[] body) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Length: " + body.length + "\r\n");
            header.forEach((key, value) -> {
                try {
                    dos.writeBytes(key + ":" + value + "\r\n");
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            });
            dos.writeBytes("\r\n");

            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static void response302Header(DataOutputStream dos, Map<String, String> header, String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + redirectUrl + "\r\n");

            header.forEach((key, value) -> {
                try {
                    dos.writeBytes(key + ":" + value + "\r\n");
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            });

            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
