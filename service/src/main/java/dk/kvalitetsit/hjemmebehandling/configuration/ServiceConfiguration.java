package dk.kvalitetsit.hjemmebehandling.configuration;

import ca.uhn.fhir.context.FhirContext;
import dk.kvalitetsit.hjemmebehandling.context.*;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ServiceConfiguration {
    @Value("${user.mock.context.cpr}")
    private String mockContextCpr;

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    @Bean
    public FhirClient getFhirClient() {
        FhirContext context = FhirContext.forR4();
        return new FhirClient(context, fhirServerUrl);
    }

    @Bean
    public WebMvcConfigurer getWebMvcConfigurer(@Autowired FhirClient client, @Value("${allowed_origins}") String allowedOrigins, @Autowired UserContextProvider userContextProvider, @Autowired IUserContextHandler userContextHandler) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins(allowedOrigins).allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
            }

            @Override
            public void addInterceptors(@NotNull InterceptorRegistry registry) {
                registry.addInterceptor(new UserContextInterceptor(client, userContextProvider, userContextHandler));
            }
        };
    }

    @Bean
    public IUserContextHandler userContextHandler(@Value("${user.context.handler}") String userContextHandler) {
        return switch (userContextHandler) {
            case "DIAS" -> new DIASUserContextHandler();
            case "MOCK" -> new MockContextHandler(mockContextCpr);
            default ->
                    throw new IllegalArgumentException(String.format("Unknown userContextHandler value: %s", userContextHandler));
        };
    }
}
