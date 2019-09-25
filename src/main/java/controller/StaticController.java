package controller;

import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

import java.io.IOException;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 26.
 */
public class StaticController implements Controller {
    @Override
    public void invoke(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        httpResponse.forward(httpRequest.getUrl());
    }
}
