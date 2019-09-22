package type;

public enum HttpStatusCode {
    OK(200, "Ok"),
    REDIRECT(302, "Found");

    private int code;
    private String information;

    HttpStatusCode(int code, String information) {
        this.code = code;
        this.information = information;
    }

    public String codeWithInformation() {
        return code + " " + information;
    }
}
