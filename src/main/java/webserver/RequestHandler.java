package webserver;

import model.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import user.service.UserService;
import util.HttpRequestUtils;
import util.IOUtils;
import webserver.Response.ResponseHandler;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private UserService userService;

    private static final int METHOD = 0;
    private static final int URL = 1;

    private static final String GET = "GET";
    private static final String POST = "POST";

    private static final String LOGIN_KEY = "logined";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        this.userService = new UserService();
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            String line = bufferedReader.readLine();
            String[] requestLine = line.split(StringUtils.SPACE);

            boolean logined = false;
            int contentLength = 0;
            Map<String, String> cookies;
            while (StringUtils.isNotBlank(line)) {
                line = bufferedReader.readLine();
                if (line.contains("Cookie")) {
                    String cookieValue = line.split(":")[1].trim();
                    cookies = HttpRequestUtils.parseCookies(cookieValue);
                    logined = isLogin(cookies);
                } else if (line.contains("Content-Length")) {
                    contentLength = Integer.parseInt(HttpRequestUtils.parseHeader(line).getValue());
                }
            }

            DataOutputStream dos = new DataOutputStream(out);

            if (StringUtils.equals(requestLine[METHOD], GET)) {
                getMapping(dos, requestLine, logined);
            } else if (StringUtils.equals(requestLine[METHOD], POST)) {
                String body = IOUtils.readData(bufferedReader, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(body);
                postMapping(dos, requestLine, params, logined);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void getMapping(DataOutputStream dos, String[] requestLine, boolean logined) throws IOException {
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
            ResponseHandler.response200Header(dos, body.length);
            ResponseHandler.responseBody(dos, body);
        } else if (StringUtils.equals(requestPath, "/user/create")) {
            Map<String, String> params = HttpRequestUtils.parseQueryString(paramsString);
            userService.addUser(params);
            ResponseHandler.response302Header(dos, "/index.html", logined);
        } else if (StringUtils.equals(requestPath, "/user/list")) {
            if (!logined) {
                ResponseHandler.response302Header(dos, "/user/login.html", false);
                return;
            }

            Collection<User> users = userService.findAll();

            StringBuilder userTable = new StringBuilder("<table border='1'>");
            userTable.append("<thead>" +
                                    "<tr>" +
                                        "<th>" + "Id" + "</th>" +
                                        "<th>" + "Name" + "</th>" +
                                        "<th>" + "Email" + "</th>" +
                                    "</tr>" +
                            "</thead></tbody>");
            for (User user : users) {
                userTable.append("<tr>");
                userTable.append("<td>" + user.getUserId() + "</td>");
                userTable.append("<td>" + user.getName() + "</td>");
                userTable.append("<td>" + user.getPassword() + "</td>");
                userTable.append("</tr>");
            }
            userTable.append("</tbody></table>");

            byte[] bytes = userTable.toString().getBytes();
            ResponseHandler.response200Header(dos, bytes.length);
            ResponseHandler.responseBody(dos, bytes);
        } else if (requestPath.endsWith(".css")) {
            byte[] bytes = Files.readAllBytes(viewResolver(url));
            ResponseHandler.response200HeaderForCss(dos, bytes.length);
            ResponseHandler.responseBody(dos, bytes);
        } else {
            byte[] bytes = Files.readAllBytes(viewResolver(url));
            ResponseHandler.response200Header(dos, bytes.length);
            ResponseHandler.responseBody(dos, bytes);
        }
    }

    private void postMapping(DataOutputStream dos, String[] requestLine, Map<String, String> params, boolean logined) throws IOException {
        if (StringUtils.equals(requestLine[URL], "/user/create")) {
            userService.addUser(params);
            ResponseHandler.response302Header(dos, "/index.html", logined);
        } else if (StringUtils.equals(requestLine[URL], "/user/login")) {
            User user = userService.findUserById(params.get("userId"));

            if (user == null) {
                ResponseHandler.response302Header(dos, "/user/login_failed.html", false);
            }
            ResponseHandler.response302Header(dos, "/index.html", true);
        }
    }

    private boolean isLogin(Map<String, String> cookies) {
        return Optional.ofNullable(cookies.get(LOGIN_KEY))
                .map(Boolean::valueOf)
                .orElse(false);
    }

    private Path viewResolver(String url) {
        if (StringUtils.equals(url, "/")) {
            return new File("./webapp/index.html").toPath();
        }
        return new File("./webapp" + url).toPath();
    }
}
