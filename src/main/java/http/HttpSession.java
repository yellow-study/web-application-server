package http;

import com.google.common.collect.Maps;

import java.util.Map;

public class HttpSession {
    private String id;
    private Map<String, Object> attributes;

    public HttpSession(String id) {
        this.id = id;
        attributes = Maps.newHashMap();
    }

    public String getId() {
        return id;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void invalidate() {
        HttpSessions.remove(id);
    }
}
