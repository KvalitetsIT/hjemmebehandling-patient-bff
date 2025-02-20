package dk.kvalitetsit.hjemmebehandling.context;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.servlet.http.HttpSession;

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
