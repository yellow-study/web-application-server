package user.controller;

import controller.Controller;
import model.User;
import user.service.UserService;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 26.
 */
public class UserCreateController implements Controller{
    private final UserService userService;

    public UserCreateController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void invoke(HttpRequest httpRequest, HttpResponse httpResponse) {
        User user = User.builder()
                        .userId(httpRequest.getParameter("userId"))
                        .password(httpRequest.getParameter("password"))
                        .name(httpRequest.getParameter("name"))
                        .email(httpRequest.getParameter("email"))
                        .build();

        userService.addUser(user);
        httpResponse.sendRedirect("/index.html");
    }
}
