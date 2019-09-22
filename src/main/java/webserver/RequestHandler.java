package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.sun.tools.javac.util.StringUtils;
import db.DataBase;
import model.HttpRequestHeader;
import model.HttpResponseHeader;
import type.HttpHeaderOption;
import type.HttpStatusCode;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            HttpRequestHeader requestHeader = readHeader(br);

            byte[] body = makeResponseBody(requestHeader.getUrl());

            DataOutputStream dos = new DataOutputStream(out);
            responseHeader(dos, makeResponseHeader(requestHeader, br));
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private HttpRequestHeader readHeader(BufferedReader br) throws IOException {
        HttpRequestHeader header = new HttpRequestHeader();

        String line = br.readLine();
        header.setBasicInfo(line);
        log.debug(line);

        while(!"".equals(line = br.readLine())) {
            if (line == null) break;
            header.setHeaderOptions(line);
            log.debug(line);
        }
        return header;
    }

    private Map<String, String> getRequestBodyByMethod(HttpRequestHeader header, BufferedReader br) throws IOException {
        Map<String, String> requestBody = Maps.newHashMap();
        if ("GET".equals(header.getMethod()) && !header.getQueryStrings().isEmpty()) {
            requestBody = header.getQueryStrings();
        }
        if ("POST".equals(header.getMethod())) {
            int contentLength = Integer.parseInt(header.getHeaderOption("Content-Length"));
            requestBody = HttpRequestUtils.parseQueryString(IOUtils.readData(br, contentLength));
        }
        return requestBody;
    }

    private HttpResponseHeader makeResponseHeader(HttpRequestHeader header, BufferedReader br) throws IOException {
        Map<String, String> requestBody = getRequestBodyByMethod(header, br);

        if ("/user/create".equals(header.getUrl())) {
            DataBase.addUser(new User(requestBody.get("userId"),
                    requestBody.get("name"),
                    requestBody.get("password"),
                    requestBody.get("name"))
            );

            return HttpResponseHeader.builder()
                    .httpStatusCode(HttpStatusCode.REDIRECT)
                    .headerOption(HttpHeaderOption.LOCATION, "/index.html")
                    .build();
        }
        if ("/user/login".equals(header.getUrl())) {
            Optional<User> optionalUser = DataBase.findUserById(requestBody.get("userId"));

            String location;
            String cookie;

            if (optionalUser.filter(user -> user.getPassword().equals(requestBody.get("password"))).isPresent()) {
                location = "/index.html";
                cookie = "logined=true";
            } else {
                location = "/user/login_failed.html";
                cookie = "logined=false";
            }

            return HttpResponseHeader.builder()
                    .httpStatusCode(HttpStatusCode.REDIRECT)
                    .headerOption(HttpHeaderOption.LOCATION, location)
                    .headerOption(HttpHeaderOption.SET_COOKIE, cookie)
                    .build();
        }
        return HttpResponseHeader.builder()
                .httpStatusCode(HttpStatusCode.OK)
                .build();
    }

    private byte[] makeResponseBody(String url) throws IOException {
        byte[] body = new byte[]{};

        if (StringUtils.indexOfIgnoreCase(url, ".html") >= 0) {
            body = Files.readAllBytes(new File("./webapp" + url).toPath());
        }

        return body;
    }

    private void responseHeader(DataOutputStream dos, HttpResponseHeader header) {
        try {
            dos.writeBytes(header.getHttpVersion() + " " + header.getHttpStatusCode().codeWithInformation() + " ");
            dos.writeBytes("\r\n");
            for (Map.Entry<String, String> entries : header.getHeaderOptions().entrySet()) {
                dos.writeBytes(entries.getKey() + ": " + entries.getValue() );
                dos.writeBytes("\r\n");
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
