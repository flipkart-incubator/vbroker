package com.flipkart.vbroker.app;

import com.xebialabs.restito.semantics.ConditionWithApplicables;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockHttp {
    public static final int MOCK_HTTP_SERVER_PORT = 17230;
    public static final String MOCK_RESPONSE_BODY = "{\"msg\":\"mock message\"}";

    public enum MockURI {
        URI_200("/200"),
        URI_204("/204"),
        URI_400("/400"),
        URI_404("/404"),
        URI_429("/429"),
        URI_500("/500"),
        URI_504("/504"),
        URI_MOCK_APP("/url"),
        URI_MOCK_APP2("/url2"),
        SLEEP_200("/sleep200"),
        SLEEP_404("/sleep404");

        private final String uri;

        MockURI(String uri) {
            this.uri = uri;
        }

        public String uri() {
            return uri;
        }

        public String url() {
            return String.format("http://localhost:%d%s", MOCK_HTTP_SERVER_PORT, uri);
        }
    }

    public static ConditionWithApplicables post(MockURI mockURI) {
        return com.xebialabs.restito.semantics.ConditionWithApplicables.post(mockURI.uri());
    }

    public static ConditionWithApplicables put(MockURI mockURI) {
        return com.xebialabs.restito.semantics.ConditionWithApplicables.post(mockURI.uri());
    }

    public static ConditionWithApplicables patch(MockURI mockURI) {
        return com.xebialabs.restito.semantics.ConditionWithApplicables.post(mockURI.uri());
    }

    public static void main(String args[]) {
        log.info("Equals: {}", MockURI.URI_200.uri().equals("/200"));
    }
}
