package model;

public class HttpResponse {
    private HttpStatusCode httpStatusCode;
    private byte[] body;

    public HttpResponse(HttpStatusCode httpStatusCode, byte[] body) {
        this.httpStatusCode = httpStatusCode;
        this.body = body;
    }

    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(HttpStatusCode httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
