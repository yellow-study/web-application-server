package webserver.handler;

import java.util.HashMap;
import java.util.Map;

import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

/**
 * @author sungryul-yook on 2019-09-19.
 */
public interface HttpRequestHandler {
	HttpResponse execute(HttpRequest request) throws Exception;
}
