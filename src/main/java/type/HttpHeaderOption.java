package type;

public enum HttpHeaderOption {
    CONTENT_LENGTH("Content-Length"),
    SET_COOKIE("Set-Cookie"),
    CONTENT_TYPE("Content-Type"),
    LOCATION("Location");

    private String name;

    HttpHeaderOption(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
