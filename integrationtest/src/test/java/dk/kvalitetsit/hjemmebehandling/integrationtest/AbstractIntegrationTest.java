package dk.kvalitetsit.hjemmebehandling.integrationtest;

import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractIntegrationTest {
    private static String host = "localhost";
    private static String exposedServicePort = "8080";

    @BeforeAll
    public static void
    setupEnvironment() throws Exception {
        System.out.print("Inside AbstractIntegrationTest.setupEnvironment");

        if(System.getProperty("patient-bff-host") != null) {
            host = System.getProperty("patient-bff-host");
            System.out.println("Changed host to: " + host);
        }
    }

    protected String enhanceBasePath(String basePath) {
        return basePath.replace("localhost", host).replace("8586", exposedServicePort);
    }
}
