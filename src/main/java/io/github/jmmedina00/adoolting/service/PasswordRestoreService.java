package io.github.jmmedina00.adoolting.service;

import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.jmmedina00.adoolting.repository.fromutil.PasswordRestoreTokenRepository;

@Service
public class PasswordRestoreService {
  @Autowired
  PasswordRestoreTokenRepository restoreTokenRepository;

  @Autowired
  PersonService personService;

  @Autowired
  private EmailService emailService;

  @Autowired
  private JobScheduler jobScheduler;

  @Value("${restoretoken.expires.hours}")
  private int expireInHours;
}
