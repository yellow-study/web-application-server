package webserver.http;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by juhyung0818@naver.com on 2019. 9. 5.
 */

@ToString
@Getter
@Setter
public class HttpResponse {
    private String resultStatus;
    private Map<String, String> header;
    private byte[] body;  //setBody 시 Content-Length를 헤더값에 추가하도록 하자. 하도록 하자
}
