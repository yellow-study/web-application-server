package http;

import java.util.HashMap;
import java.util.Map;

public class HttpSession {
    private Map<String, Object> data = new HashMap<>();

    public HttpSession(String id) {
        data.put("JSESSIONID", id);
    }

    public String getId() {
        String id = String.valueOf(data.get("JSESSIONID"));
        return id.equals("null") ? null : id;
    }

    public void setAttribute(String name, Object value) {
        data.put(name, value);
    }

    public Object getAttribute(String name) {
        return data.get(name);
    }

    public void removeAttribute(String name) {
        data.remove(name);
    }

    public void invalidate() {
        data.clear();
    }
}
