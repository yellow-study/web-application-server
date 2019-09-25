package user.controller;

import controller.Controller;
import model.User;
import user.service.UserService;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 26.
 */
public class UserListController implements Controller {
    private static final String LOGIN_KEY = "logined";

    private final UserService userService;

    public UserListController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void invoke(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        boolean logined = isLogin(httpRequest.getCookie(LOGIN_KEY));

        if (!logined) {
            httpResponse.sendRedirect("/login.html");
            return;
        }

        Collection<User> users = userService.findAll();
        String view = userService.getUserListView(users);

        httpResponse.forward(httpRequest.getUrl());

        byte[] body = view.getBytes();
        httpResponse.responseBody(body);
    }

    private boolean isLogin(String loginValue) {
        return Optional.ofNullable(loginValue)
                       .map(Boolean::valueOf)
                       .orElse(false);
    }
}
