package model;

public enum HttpStatusCode {
    OK(200),
    REDIRECT(302);

    private int code;

    HttpStatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
