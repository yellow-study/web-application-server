package webserver;

import db.DataBase;
import model.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    private static final int METHOD = 0;
    private static final int URL = 1;
    private static final int HTTP_VERSION = 2;

    private static final String SPACE = StringUtils.SPACE;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))) {

            String line = bufferedReader.readLine();
            String requestLine = line;

            while (StringUtils.isNotBlank(line)) {
                line = bufferedReader.readLine();
            }

            DataOutputStream dos = new DataOutputStream(out);
            requestMapping(requestLine, dos);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void requestMapping(String line, DataOutputStream dos) throws IOException {
        String[] requestLine = line.split(SPACE);
        String url = requestLine[URL];
        String requestPath = url;
        String paramsString = StringUtils.EMPTY;

        if (url.contains("?")) {
            int index = url.indexOf("?");
            requestPath = url.substring(0, index);
            paramsString = url.substring(index + 1);
        }

        if (StringUtils.equals(requestPath, "/")) {
            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } else if(StringUtils.equals(requestPath, "/user/create")) {
            Map<String, String> params= HttpRequestUtils.parseQueryString(paramsString);
            User user = User.builder()
                    .userId(params.get("userId"))
                    .password(params.get("password"))
                    .name(params.get("name"))
                    .email(params.get("email")).build();
            DataBase.addUser(user);

            redirectIndexView(dos);
        } else {
            byte[] bytes = Files.readAllBytes(viewResolver(url));
            response200Header(dos, bytes.length);
            responseBody(dos, bytes);
        }
    }

    private Path viewResolver(String url) {
        if (StringUtils.equals(url, "/")) {
            return new File("./webapp/index.html").toPath();
        }
        return new File("./webapp" + url).toPath();
    }

    private void redirectIndexView(DataOutputStream dos) throws IOException {
        byte[] bytes = Files.readAllBytes(viewResolver("/"));
        response200Header(dos, bytes.length);
        responseBody(dos, bytes);
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
            log.error(e.getMessage());
        }
    }
}
