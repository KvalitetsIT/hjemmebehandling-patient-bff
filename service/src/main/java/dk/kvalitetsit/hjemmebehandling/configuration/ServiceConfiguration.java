package dk.kvalitetsit.hjemmebehandling.configuration;

import dk.kvalitetsit.hjemmebehandling.context.UserContextInterceptor;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.service.access.AccessValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ca.uhn.fhir.context.FhirContext;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirMapper;
import dk.kvalitetsit.hjemmebehandling.service.QuestionnaireResponseService;

@Configuration
public class ServiceConfiguration {
	
	@Value("${user.context.handler}")
	private String userContextHandler;
	
	@Value("${fhir.server.url}")
	private String fhirServerUrl;

    @Bean
    public QuestionnaireResponseService getQuestionnaireResponseService(@Autowired FhirClient client, @Autowired FhirMapper mapper, @Autowired AccessValidator accessValidator) {
        return new QuestionnaireResponseService(client, mapper, accessValidator);
    }

    @Bean
    public FhirClient getFhirClient() {
        FhirContext context = FhirContext.forR4();
        return new FhirClient(context, fhirServerUrl);
    }

    @Bean
    public WebMvcConfigurer getWebMvcConfigurer(@Value("${allowed_origins}") String allowedOrigins, @Autowired UserContextProvider userContextProvider) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins(allowedOrigins).allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new UserContextInterceptor(userContextProvider,userContextHandler));
            }
        };
    }
}
