package webserver;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import user.service.UserService;
import util.HttpRequestUtils;
import util.IOUtils;
import webserver.Response.ResponseHandler;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private UserService userService;

    private static final int METHOD = 0;
    private static final int URL = 1;
    private static final int HTTP_VERSION = 2;

    private static final String GET = "GET";
    private static final String POST = "POST";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        this.userService = new UserService();
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))) {

            String line = bufferedReader.readLine();
            String[] requestLine = line.split(StringUtils.SPACE);

            DataOutputStream dos = new DataOutputStream(out);

            if (StringUtils.equals(requestLine[METHOD], GET)) {
                getMapping(dos, requestLine);
            } else if (StringUtils.equals(requestLine[METHOD], POST)) {
                String body = getBody(bufferedReader);
                postMapping(dos, requestLine, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getBody(BufferedReader bufferedReader) throws IOException {
        String body = StringUtils.EMPTY;
        String headerLine = bufferedReader.readLine();
        while (StringUtils.isNotBlank(headerLine)) {
            headerLine = bufferedReader.readLine();
            if (headerLine.contains("Content-Length")) {
                int contentLength = Integer.parseInt(HttpRequestUtils.parseHeader(headerLine).getValue());
                body = IOUtils.readData(bufferedReader, contentLength);
                break;
            }
        }
        return body;
    }

    private void getMapping(DataOutputStream dos, String[] requestLine) throws IOException {
        String url = requestLine[URL];
        String requestPath = url;
        String paramsString = StringUtils.EMPTY;

        if (url.contains("?")) {
            int index = url.indexOf("?");
            requestPath = url.substring(0, index);
            paramsString = url.substring(index + 1);
        }

        if (StringUtils.equals(requestPath, "/")) {
            byte[] body = "Hello World".getBytes();
            ResponseHandler.response200Header(dos, body.length);
            ResponseHandler.responseBody(dos, body);
        } else if (StringUtils.equals(requestPath, "/user/create")) {
            Map<String, String> params = HttpRequestUtils.parseQueryString(paramsString);
            userService.addUser(params);
            log.info("user : {}", userService.findAll());
            ResponseHandler.response302Header(dos, "/index.html");
        } else {
            byte[] bytes = Files.readAllBytes(viewResolver(url));
            ResponseHandler.response200Header(dos, bytes.length);
            ResponseHandler.responseBody(dos, bytes);
        }
    }

    private void postMapping(DataOutputStream dos, String[] requestLine, String body) throws IOException {
        if (StringUtils.equals(requestLine[URL], "/user/create")) {
            Map<String, String> params = HttpRequestUtils.parseQueryString(body);
            userService.addUser(params);
            log.info("user : {}", userService.findAll());
            ResponseHandler.response302Header(dos, "/index.html");
        }
    }

    private Path viewResolver(String url) {
        if (StringUtils.equals(url, "/")) {
            return new File("./webapp/index.html").toPath();
        }
        return new File("./webapp" + url).toPath();
    }
}
