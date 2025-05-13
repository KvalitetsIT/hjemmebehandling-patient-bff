package dk.kvalitetsit.hjemmebehandling.context;

import jakarta.servlet.http.HttpSession;
import org.openapitools.model.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class UserContextProvider {
    private UserContext context;

    public UserContext getUserContext() {
        return context;
    }

    public void setUserContext(HttpSession httpSession, UserContext context) {
        this.context = context;

    }
}
