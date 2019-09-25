package user.controller;

import controller.Controller;
import model.User;
import user.service.UserService;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 26.
 */
public class UserLoginController implements Controller {
    private final UserService userService;

    public UserLoginController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void invoke(HttpRequest httpRequest, HttpResponse httpResponse) {
        String userId = httpRequest.getParameter("userId");
        User user = userService.findUserById(userId);

        if (user == null) {
            httpResponse.sendRedirect("/user/login_failed.html");
        }
        httpResponse.sendRedirect("/index.html");
    }
}
