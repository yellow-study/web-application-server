package controller;

import http.HttpSession;
import http.HttpSessions;
import model.User;
import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import util.HttpRequestUtils;

public class LoginController extends AbstractController {
    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        User user = DataBase.findUserById(request.getParameter("userId"));
        if (user != null) {
            if (user.login(request.getParameter("password"))) {
                String id = HttpRequestUtils.getCookieValue(request.getHeader("Cookie"), "JSESSIONID");
                HttpSession httpSession = HttpSessions.getSession(id);
                httpSession.setAttribute("logined", true);

                response.sendRedirect("/index.html");
            } else {
                response.sendRedirect("/user/login_failed.html");
            }
        } else {
            response.sendRedirect("/user/login_failed.html");
        }
    }
}
