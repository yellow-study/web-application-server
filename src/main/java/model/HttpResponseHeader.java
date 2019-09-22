package model;

import java.util.Map;

import com.google.common.collect.Maps;
import type.HttpHeaderOption;
import type.HttpStatusCode;

public class HttpResponseHeader {
    private String httpVersion;
    private HttpStatusCode httpStatusCode;
    private Map<String, String> headerOptions;

    private HttpResponseHeader(String httpVersion, HttpStatusCode httpStatusCode, Map<String, String> headerOptions) {
        this.httpVersion =httpVersion;
        this.httpStatusCode = httpStatusCode;
        this.headerOptions = headerOptions;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }

    public Map<String, String> getHeaderOptions() {
        return headerOptions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String httpVersion;
        private HttpStatusCode httpStatusCode;
        private Map<String, String> headerOptions;

        public Builder() {
            httpVersion = "HTTP/1.1";
            headerOptions = Maps.newHashMap();
        }

        public Builder httpVersion(String httpVersion) {
            this.httpVersion = httpVersion;
            return this;
        }

        public Builder httpStatusCode(HttpStatusCode httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder headerOption(HttpHeaderOption option, String value) {
            this.headerOptions.put(option.getName(), value);
            return this;
        }

        public HttpResponseHeader build() {
            return new HttpResponseHeader(httpVersion, httpStatusCode, headerOptions);
        }
    }
}
