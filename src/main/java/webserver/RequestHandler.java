package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = handleRequest(in);
            HttpResponse httpResponse = handleAction(request.getUrl(), request.getBody());
            handleResponse(out, httpResponse.getBody());

            log.info("users : {}", DataBase.findAll());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private HttpRequest handleRequest(InputStream in) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))) {
            String line = br.readLine();

            HttpRequest request = HttpRequest.create(line);
            log.debug("request line : {}", request);

            while (!"".equals(line)) {
                log.debug(line);
                line = br.readLine();
            }
            return request;
        }
    }

    private HttpResponse handleAction(String url, Map<String, String> body) throws IOException {
        HttpResponse httpResponse = new HttpResponse();
        if (url.equals("/user/create")) {
            User user = User.builder()
                    .userId(body.get("userId"))
                    .password(body.get("password"))
                    .name(body.get("name"))
                    .email(body.get("email"))
                    .build();
            DataBase.addUser(user);
            httpResponse.setBody("SUCCESS".getBytes());
        } else {
            httpResponse.setBody(Files.readAllBytes(new File("./webapp" + url).toPath()));
        }

        return httpResponse;
    }

    private void handleResponse(OutputStream out, byte[] body) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
