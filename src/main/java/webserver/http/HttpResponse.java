package webserver.http;

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
    private byte[] body;
}
