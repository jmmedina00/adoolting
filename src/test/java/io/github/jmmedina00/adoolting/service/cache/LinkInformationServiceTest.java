package io.github.jmmedina00.adoolting.service.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.Medium;
import io.github.jmmedina00.adoolting.entity.cache.LinkInformation;
import io.github.jmmedina00.adoolting.repository.cache.LinkInformationRepository;
import io.github.jmmedina00.adoolting.service.MediumService;
import io.github.jmmedina00.adoolting.service.util.FileService;
import io.github.jmmedina00.adoolting.util.SelfReturningAnswer;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class LinkInformationServiceTest {
  @MockBean
  private LinkInformationRepository infoRepository;

  @MockBean
  private MediumService mediumService;

  @MockBean
  private FileService fileService;

  @MockBean
  private JobScheduler jobScheduler;

  @Autowired
  private LinkInformationService infoService;

  @Captor
  private ArgumentCaptor<LinkInformation> infoCaptor;

  @Test
  public void getLinkInfoReturnsInfoFromRepository() {
    LinkInformation info = new LinkInformation();
    info.setTitle("Test");

    Mockito.when(infoRepository.findById(125L)).thenReturn(Optional.of(info));

    LinkInformation linkInfo = infoService.getLinkInfo(125L);
    assertEquals("Test", linkInfo.getTitle());
    assertEquals(info, linkInfo);

    verify(jobScheduler, never())
      .enqueue(any(UUID.class), any(JobLambda.class));
  }

  @Test
  public void getLinkInfoReturnsBlankInfoIfNotFoundInRepository() {
    Mockito.when(infoRepository.findById(125L)).thenReturn(Optional.empty());

    LinkInformation info = infoService.getLinkInfo(125L);

    assertEquals("", info.getActualLink());
    assertEquals("", info.getTitle());
  }

  @Test
  public void getLinkInfoEnqueuesJobIfDataNotFoundInRepository() {
    Calendar fixedCalendar = Calendar.getInstance();
    fixedCalendar.setTime(new Date(1655085600000L)); // 2022/06/13 at 2:00AM

    MockedStatic<Calendar> utilities = Mockito.mockStatic(Calendar.class);
    utilities.when(Calendar::getInstance).thenReturn(fixedCalendar);

    infoService.getLinkInfo(125L);

    verify(jobScheduler, times(1))
      .enqueue(
        eq(UUID.nameUUIDFromBytes("Link process: 125 @ 13".getBytes())),
        any(JobLambda.class)
      );
    utilities.closeOnDemand();
  }

  @Test
  public void fetchAndSaveLinkInfoGrabsAllInformationFromFetchedMediumReference()
    throws Exception {
    Medium medium = new Medium();
    medium.setReference("Test");

    Document document = Jsoup.parse(
      new File(getClass().getResource("/link-info/most-content.html").toURI())
    );
    Connection fakeConnection = Mockito.mock(
      Connection.class,
      new SelfReturningAnswer()
    );
    Mockito.when(fakeConnection.get()).thenReturn(document);
    Mockito.when(mediumService.getMedium(125L)).thenReturn(medium);

    MockedStatic<Jsoup> utilities = Mockito.mockStatic(Jsoup.class);
    utilities.when(() -> Jsoup.connect(any())).thenReturn(fakeConnection);

    infoService.fetchAndSaveLinkInfo(125L);

    verify(infoRepository, times(1)).save(infoCaptor.capture());
    LinkInformation info = infoCaptor.getValue();

    assertEquals(125L, info.getId());
    assertEquals("Testing", info.getTitle());
    assertEquals("http://test.local", info.getActualLink());

    utilities.closeOnDemand();
  }

  @Test
  public void fetchAndSaveLinkInfoSendsImageLinkToSaveIfPresent()
    throws Exception {
    Medium medium = new Medium();
    medium.setReference("Test");

    Document document = Jsoup.parse(
      new File(getClass().getResource("/link-info/with-image.html").toURI())
    );

    Connection fakeConnection = Mockito.mock(
      Connection.class,
      new SelfReturningAnswer()
    );
    Mockito.when(fakeConnection.get()).thenReturn(document);
    Mockito.when(mediumService.getMedium(125L)).thenReturn(medium);

    MockedStatic<Jsoup> utilities = Mockito.mockStatic(Jsoup.class);
    utilities.when(() -> Jsoup.connect(any())).thenReturn(fakeConnection);

    infoService.fetchAndSaveLinkInfo(125L);

    verify(infoRepository, times(1)).save(any());
    verify(fileService, times(1))
      .cacheImageForLinkMedium("http://test.local/image", 125L);

    utilities.closeOnDemand();
  }

  @Test
  public void fetchAndSaveLinkInfoDefaultsToBasicInformationWhenTagsWantedAreNotFound()
    throws Exception {
    Medium medium = new Medium();
    medium.setReference("Test");

    Document document = Jsoup.parse(
      new File(getClass().getResource("/link-info/no-content.html").toURI())
    );

    Connection fakeConnection = Mockito.mock(
      Connection.class,
      new SelfReturningAnswer()
    );
    Mockito.when(fakeConnection.get()).thenReturn(document);
    Mockito.when(mediumService.getMedium(125L)).thenReturn(medium);

    MockedStatic<Jsoup> utilities = Mockito.mockStatic(Jsoup.class);
    utilities.when(() -> Jsoup.connect(any())).thenReturn(fakeConnection);

    infoService.fetchAndSaveLinkInfo(125L);

    verify(infoRepository, times(1)).save(infoCaptor.capture());
    LinkInformation info = infoCaptor.getValue();

    assertEquals(125L, info.getId());
    assertEquals("Default title", info.getTitle());
    assertEquals("Test", info.getActualLink());

    utilities.closeOnDemand();
  }
}
