package controller;

import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.io.IOException;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 26.
 */
public interface Controller {
    void invoke(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException;
}
