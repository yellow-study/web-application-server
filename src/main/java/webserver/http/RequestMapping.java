package webserver.http;

import controller.Controller;
import controller.HomeController;
import controller.StaticController;
import user.controller.UserCreateController;
import user.controller.UserListController;
import user.controller.UserLoginController;
import user.service.UserService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 26.
 */
public class RequestMapping {
    private static Map<String, Controller> controllers;
    private static Controller staticController = new StaticController();

    private RequestMapping() {}

    public static Controller getController(String url) {
        if(controllers == null) {
            controllers = new HashMap<>();
            setControllers();
        }

        if(url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".ico")) {
            return staticController;
        }

        return controllers.get(url);
    }

    private static void setControllers () {
        controllers.put("/index.html", new HomeController());
        controllers.put("/", new HomeController());
        setUserControllers();
    }

    private static void setUserControllers() {
        UserService userService = new UserService();
        controllers.put("/user/login", new UserLoginController(userService));
        controllers.put("/user/create", new UserCreateController(userService));
        controllers.put("/user/list", new UserListController(userService));
    }
}
