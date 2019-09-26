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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

            //TODO service method - http method 를 판별하여 doXXXMethod를 호출한다.
            String requestLine = reader.readLine();

            if (requestLine == null) {
                return;
            }

            log.debug(requestLine);

            String requestHeader;
            HttpRequestUtils.Pair contentLength = null;
            HttpRequestUtils.Pair contentTypePair = null;
            String contentType = null;

            Map<String, String> cookies = new HashMap<String, String>();
            cookies.put("logined", "false");

            while (!"".equals(requestHeader = reader.readLine())) {
                if (requestHeader.startsWith("Content-Length")) {
                    contentLength = HttpRequestUtils.parseHeader(requestHeader);

                } else if (requestHeader.startsWith("Accept:")) {//content type check
                    contentTypePair = HttpRequestUtils.parseHeader(requestHeader);
                    int contentTypeIndex = contentTypePair.getValue().indexOf(",");

                    if (contentTypeIndex <= NOT_EXISTS) {
                        contentType = contentTypePair.getValue();
                    } else {
                        contentType = contentTypePair.getValue().substring(0, contentTypeIndex);
                    }
                } else if (requestHeader.startsWith("Cookie")) {//cookie check
                    cookies = HttpRequestUtils.parseCookies(HttpRequestUtils.parseHeader(requestHeader).getValue());
                }
                log.debug(requestHeader);
            }

            String[] tokens = requestLine.split(URL_DELIMITER);

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
                        User user = new User(params.get("userId"), params.get("password"), params.get("name"),
                                params.get("email"));
                        DataBase.addUser(user);

                        url = "/index.html";
                        response302Header(dos, url);
                    }
                } else {

                    if ("/user/list".equals(url)) {
                        boolean isLogined = Boolean.parseBoolean(cookies.get("logined"));

                        if (isLogined) {
                            String html = getUserListPage();
                            makeUserList200Response(dos, contentType, html.getBytes());
                        } else {
                            response302LoginHeader(dos, isLogined, "login.html");
                        }
                    } else {
                        make200Response(dos, contentType, url);
                    }
                }
            }

            if (POST.equals(method)) {
                if (contentLength != null) {
                    String body = IOUtils.readData(reader, Integer.parseInt(contentLength.getValue()));
                    Map<String, String> params = HttpRequestUtils.parseQueryString(body);

                    if ("/user/create".equals(url)) {
                        User user = new User(params.get("userId"), params.get("password"), params.get("name"),
                                params.get("email"));
                        DataBase.addUser(user);

                        url = "/index.html";
                        response302Header(dos, url);

                    } else if ("/user/login".equals(url)) {

                        User user = DataBase.findUserById(params.get("userId"));
                        boolean logined = false;

                        if (user == null) {
                            url = "/user/login_failed.html";
                        } else {
                            if (user.getPassword().equals(params.get("password"))) {
                                url = "/index.html";
                                logined = true;
                            } else {
                                url = "/user/login_failed.html";
                            }
                        }
                        response302LoginHeader(dos, logined, url);
                    } else {
                        make200Response(dos, contentType, url);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getUserListPage() {
        List<User> userList = new ArrayList<>(DataBase.findAll());

        StringBuilder sb = new StringBuilder(
                "<html><head><title>사용자 목록</title></head><body><h1>사용자목록</h1>\n");

        for (User user : userList) {
            sb.append("<div>" + user.toString() + "</div>");
        }

        return sb.append("</body></html>").toString();
    }

    private void makeUserList200Response(DataOutputStream dos, String contentType, byte[] body)
            throws IOException {

        response200Header(dos, contentType, body.length);
        responseBody(dos, body);

    }

    private void make200Response(DataOutputStream dos, String contentType, String url)
            throws IOException {

        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, contentType, body.length);
        responseBody(dos, body);
    }

    private void response200Header(DataOutputStream dos, String contentType, int bodyLength) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + bodyLength + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302LoginHeader(DataOutputStream dos, boolean status, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Set-Cookie: logined=" + status + "; Path=/\r\n");
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