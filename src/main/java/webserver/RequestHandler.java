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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import model.HttpRequestHeader;
import model.HttpResponse;
import model.HttpStatusCode;
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
            HttpRequestHeader header = readHeader(br);

            HttpResponse httpResponse = makeResponse(header, br);
            HttpStatusCode httpStatusCode = httpResponse.getHttpStatusCode();
            byte[] body = httpResponse.getBody();

            DataOutputStream dos = new DataOutputStream(out);
            if (httpStatusCode == HttpStatusCode.REDIRECT) {
                response302Header(dos, "/index.html");
            } else if (httpStatusCode == HttpStatusCode.OK) {
                response200Header(dos, body.length);
            }
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

    private HttpResponse makeResponse(HttpRequestHeader header, BufferedReader br) throws IOException {
        Map<String, String> queryStrings = Maps.newHashMap();
        if ("GET".equals(header.getMethod()) && !header.getQueryStrings().isEmpty()) {
            queryStrings = header.getQueryStrings();
        }
        if ("POST".equals(header.getMethod())) {
            int contentLength = Integer.parseInt(header.getHeaderOption("Content-Length"));
            queryStrings = HttpRequestUtils.parseQueryString(IOUtils.readData(br, contentLength));
        }

        if ("/user/create".equals(header.getUrl())) {
            new User(queryStrings.get("userId"),
                    queryStrings.get("name"),
                    queryStrings.get("password"),
                    queryStrings.get("name"));
            return new HttpResponse(HttpStatusCode.REDIRECT, new byte[]{});
        }

        return new HttpResponse(HttpStatusCode.OK, Files.readAllBytes(new File("./webapp" + header.getUrl()).toPath()));
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
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
