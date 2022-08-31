package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

public class Request {
    private final String method;
    private final String path;
    private final String version;
    private final List<String> headers;
    private final String body;


    public Request(String method, String path, String version, List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {

        return method;
    }

    public String getPath() {
        return path;
    }

    public String requstToString() {
        return method + "\n"
                + path + "\n"
                + version + "\n"
                + headers;

    }

    public List<NameValuePair> getParams() {
        return URLEncodedUtils.parse(body, Charset.forName("UTF-8"));
    }

    public Optional<String> getQueryParam(String queryParam) {
        List<NameValuePair> l = URLEncodedUtils.parse(body, Charset.forName("UTF-8"));
        return l.stream()
                .filter(o -> o.getName().equals(queryParam))
                .map(o -> o.getValue())
                .findFirst();

    }
}

