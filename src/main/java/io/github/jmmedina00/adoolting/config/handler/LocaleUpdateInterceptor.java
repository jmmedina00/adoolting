package io.github.jmmedina00.adoolting.config.handler;

import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Component
public class LocaleUpdateInterceptor extends LocaleChangeInterceptor {
  @Autowired
  private PersonLocaleConfigService configService;

  private static final String paramName = "lang";
  private static final Logger logger = LoggerFactory.getLogger(
    LocaleUpdateInterceptor.class
  );

  public LocaleUpdateInterceptor() {
    setParamName(paramName);
  }

  @Override
  public void postHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler,
    ModelAndView modelAndView
  )
    throws Exception {
    String langParam = request.getParameter(paramName);

    if (langParam == null) {
      super.postHandle(request, response, handler, modelAndView);
      return;
    }

    Object principal = SecurityContextHolder
      .getContext()
      .getAuthentication()
      .getPrincipal();

    if (principal instanceof PersonDetails) {
      Long personId = ((PersonDetails) principal).getPerson().getId();
      logger.debug(
        "Detected language change for person {}. Applying...",
        personId
      );
      configService.refreshForPerson(personId);
    }

    super.postHandle(request, response, handler, modelAndView);
  }
}
