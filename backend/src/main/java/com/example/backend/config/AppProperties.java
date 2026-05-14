package com.example.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String frontendOrigin = "http://localhost:5173";
    private boolean cookieSecure;
    private Jwt jwt = new Jwt();
    private Resend resend = new Resend();
    private Gemini gemini = new Gemini();
    private String cloudinaryUrl;
    private Oauth oauth = new Oauth();
    private Shipping shipping = new Shipping();

    @Getter
    @Setter
    public static class Jwt {
        private String secret = "change-me-in-env";
        private long accessTokenSeconds = 900;
        private long refreshTokenSeconds = 1_209_600;
    }

    @Getter
    @Setter
    public static class Resend {
        private String apiKey;
        private String fromEmail = "no-reply@shoes.local";
    }

    @Getter
    @Setter
    public static class Gemini {
        private String apiKey;
        private String model = "gemini-1.5-flash";
    }

    @Getter
    @Setter
    public static class Oauth {
        private String googleClientId;
        private String appleClientId;
    }

    @Getter
    @Setter
    public static class Shipping {
        private double originLatitude = 10.7769;
        private double originLongitude = 106.7009;
        private int baseFee = 15_000;
        private int perKmFee = 4_500;
    }
}
