package model;

import java.util.Map;

import com.google.common.collect.Maps;
import util.HttpRequestUtils;

public class HttpRequestHeader {
    private String method;
    private String url;
    private Map<String, String> queryStrings;
    private Map<String, String> options;

    public HttpRequestHeader() {
        queryStrings = Maps.newHashMap();
        options = Maps.newHashMap();
    }

    public void setBasicInfo(String line) {
        String[] basicInfo = line.split(" ");

        method = basicInfo[0];
        url = basicInfo[1];

        if (line.contains("?")) {
            url = basicInfo[1].split("\\?")[0];
            queryStrings = HttpRequestUtils.parseQueryString(basicInfo[1].split("\\?")[1]);
        }
    }

    public void setHeaderOptions(String line) {
        HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
        options.put(pair.getKey(), pair.getValue());
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getQueryStrings() {
        return queryStrings;
    }

    public String getHeaderOption(String key) {
        return options.get(key);
    }
}
