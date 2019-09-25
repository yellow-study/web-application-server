package webserver;

import model.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import user.service.UserService;
import user.view.UserView;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private UserService userService;
    private UserView userView;

    private static final int METHOD = 0;
    private static final int URL = 1;

    private static final String GET = "GET";
    private static final String POST = "POST";

    private static final String LOGIN_KEY = "logined";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        this.userService = new UserService();
        this.userView = new UserView();
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()) {

            HttpRequest request = new HttpRequest(in);

            DataOutputStream dos = new DataOutputStream(out);

            if (StringUtils.equals(request.getMethod(), GET)) {
                getMapping(dos, request);
            } else if (StringUtils.equals(request.getMethod(), POST)) {
                postMapping(dos, request);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void getMapping(DataOutputStream dos, HttpRequest request) throws IOException {
        String url = request.getUrl();
        boolean logined = isLogin(request.getCookie(LOGIN_KEY));

        HttpResponse httpResponse = new HttpResponse();

        if (StringUtils.equals(url, "/user/create")) {
            User user = User.builder()
                            .userId(request.getParameter("userId"))
                            .password(request.getParameter("password"))
                            .name(request.getParameter("name"))
                            .email(request.getParameter("email"))
                            .build();

            userService.addUser(user);
            httpResponse.sendRedirect(dos, "/index.html");
        } else if (StringUtils.equals(url, "/user/list")) {
            if (!logined) {
                httpResponse.sendRedirect(dos, "/login.html");
                return;
            }

            Collection<User> users = userService.findAll();
            String view = userView.getUserListView(users);

            httpResponse.forward(dos, url);

            byte[] body = view.getBytes();
            httpResponse.responseBody(dos, body);
        } else {
            httpResponse.forward(dos, url);
        }
    }

    private void postMapping(DataOutputStream dos, HttpRequest request) throws IOException {
        String url = request.getUrl();

        HttpResponse httpResponse = new HttpResponse();

        if (url.contains("?")) {
            int index = url.indexOf("?");
            url = url.substring(0, index);
        }
        if (StringUtils.equals(url, "/user/create")) {
            User user = User.builder()
                            .userId(request.getParameter("userId"))
                            .password(request.getParameter("password"))
                            .name(request.getParameter("name"))
                            .email(request.getParameter("email"))
                            .build();

            userService.addUser(user);
            httpResponse.sendRedirect(dos, "/index.html");
        } else if (StringUtils.equals(url, "/user/login")) {
            String userId = request.getParameter("userId");
            User user = userService.findUserById(userId);

            if (user == null) {
                httpResponse.sendRedirect(dos, "/user/login_failed.html");
            }
            httpResponse.sendRedirect(dos, "/index.html");
        }
    }

    private boolean isLogin(String loginValue) {
        return Optional.ofNullable(loginValue)
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
