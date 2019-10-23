package http;

import com.google.common.collect.Maps;

import java.util.Map;

public class HttpSessions {
    private static Map<String, HttpSession> httpSessions = Maps.newHashMap();

    public static void setHttpSession(HttpSession httpSession) {
        httpSessions.put(httpSession.getId(), httpSession);
    }

    public static HttpSession getHttpSession(String id) {
        return httpSessions.get(id);
    }

    public static void remove(String id) {
        httpSessions.remove(id);
    }
}
