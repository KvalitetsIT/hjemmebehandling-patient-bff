package dk.kvalitetsit.hjemmebehandling.api;

import java.util.List;

public class CallToActionDTO {
  private List<String> callToActions;

  public List<String> getCallToActions() {
    return callToActions;
  }

  public void setCallToActions(List<String> callToActions) {
    this.callToActions = callToActions;
  }
}
