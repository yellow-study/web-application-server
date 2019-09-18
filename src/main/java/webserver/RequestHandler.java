package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String URL_DELIMITER = " ";
    private static final int NOT_EXISTS = -1;
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String firstLine = reader.readLine();

            if (firstLine == null) {
                return;
            }

            log.debug(firstLine);

            String headerInfo;
            HttpRequestUtils.Pair contentLength = null;

            while (!"".equals(headerInfo = reader.readLine())) {
                if (headerInfo.startsWith("Content-Length")) {
                    contentLength = HttpRequestUtils.parseHeader(headerInfo);
                }

                log.debug(headerInfo);
            }


            String[] tokens = firstLine.split(URL_DELIMITER);

            String method = tokens[0];
            String url = tokens[1];

            DataOutputStream dos = new DataOutputStream(out);

            if (GET.equals(method)) {
                int questionMarkIndex = url.indexOf("?");

                if (questionMarkIndex != NOT_EXISTS) {
                    String uri = url.substring(0, questionMarkIndex);
                    String queryString = url.substring(questionMarkIndex + 1);
                    Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);

                    if ("/user/create".equals(uri)) {
                        User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                        DataBase.addUser(user);
                        log.debug("uri "+uri);
                    }
                }
            }

            if (POST.equals(method)) {
                if (contentLength != null) {
                    String body = IOUtils.readData(reader, Integer.parseInt(contentLength.getValue()));
                    Map<String, String> params = HttpRequestUtils.parseQueryString(body);

                    if ("/user/create".equals(url)) {
                        User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                        DataBase.addUser(user);
                        url = "/index.html";

                        response302Header(dos,"../index.html");
                    }
                }
            }

            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);

            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
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
