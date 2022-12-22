package io.github.jmmedina00.adoolting.config.handler;

import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LocaleUpdaterSuccessHandler
  extends SavedRequestAwareAuthenticationSuccessHandler {
  @Autowired
  private PersonLocaleConfigService configService;

  @Override
  public void onAuthenticationSuccess(
    HttpServletRequest request,
    HttpServletResponse response,
    Authentication authentication
  )
    throws ServletException, IOException {
    int offsetFromUTC = Integer.parseInt(request.getParameter("offsetFromUTC"));
    Long personId =
      ((PersonDetails) authentication.getPrincipal()).getPerson().getId();
    configService.updateUTCOffset(personId, offsetFromUTC);

    super.onAuthenticationSuccess(request, response, authentication);
  }
}
