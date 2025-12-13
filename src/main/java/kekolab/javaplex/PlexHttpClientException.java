package kekolab.javaplex;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;

public class PlexHttpClientException extends PlexException {
    private final ClassicHttpRequest request;
    private final ClassicHttpResponse response;

    PlexHttpClientException(String message, ClassicHttpRequest request, ClassicHttpResponse response) {
        super(message);
        this.request = request;
        this.response = response;
    }

    public PlexHttpClientException(String message, Exception e, ClassicHttpRequest request) {
        super(message, e);
        this.request = request;
        this.response = null;
    }

    public PlexHttpClientException(String message, Exception e) {
        super(message, e);
        this.request = null;
        this.response = null;
    }

    public PlexHttpClientException(String message, Exception e, ClassicHttpRequest request,
            ClassicHttpResponse response) {
        super(message, e);
        this.request = request;
        this.response = response;
    }

    public ClassicHttpRequest getRequest() {
        return request;
    }

    public ClassicHttpResponse getResponse() {
        return response;
    }
}
