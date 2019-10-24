package http;

import java.util.HashMap;
import java.util.Map;

public class HttpSessions {
    public static Map<String, HttpSession> httpSessions = new HashMap<>();

    private HttpSessions() {

    }

    public static HttpSession getSession(String id) {
        return httpSessions.get(id);
    }

    public static void createSession(String id) {
        httpSessions.put(id, new HttpSession(id));
    }

    public static boolean isLogin(String id) {
        return (boolean)httpSessions.get(id).getAttribute("logined");
    }
}
