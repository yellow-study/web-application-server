/*
 * Copyright 2019 Naver Corp. All rights Reserved.
 * Naver PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package webserver;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final String URL_DELIMITER = " ";
    private static final int NOT_EXISTS = -1;
    private Map<String, String> header;
    private Map<String, String> parameter;
    private String method;
    private String path;

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String requestLine = reader.readLine();
        String[] tokens = requestLine.split(URL_DELIMITER);
        header = new HashMap<>();

        method = tokens[0];
        String url = tokens[1];
        String requestHeader;

        while (!"".equals(requestHeader = reader.readLine())) {
            HttpRequestUtils.Pair headerPair = HttpRequestUtils.parseHeader(requestHeader);
            header.put(headerPair.getKey(), headerPair.getValue());
        }

        if (method.equals(("GET"))) {
            int questionMarkIndex = url.indexOf("?");

            if (questionMarkIndex != NOT_EXISTS) {
                path = url.substring(0, questionMarkIndex);
                String queryString = url.substring(questionMarkIndex + 1);
                parameter = HttpRequestUtils.parseQueryString(queryString);
            } else {
                path = url;
            }
        } else if (method.equals("POST")) {

        }



    }


    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String key) {
        return header.get(key);
    }

    public String getParameter(String key) {
        if (parameter != null) {
            return parameter.get(key);
        } else {
            return null;
        }
    }
}
