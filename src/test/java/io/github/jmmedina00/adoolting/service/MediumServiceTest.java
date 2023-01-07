package io.github.jmmedina00.adoolting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.Medium;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.exception.MediumNotFoundException;
import io.github.jmmedina00.adoolting.repository.MediumRepository;
import io.github.jmmedina00.adoolting.service.util.FileService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class MediumServiceTest {
  @MockBean
  private MediumRepository mediumRepository;

  @MockBean
  private FileService fileService;

  @Autowired
  private MediumService mediumService;

  @Captor
  private ArgumentCaptor<Medium> mediumCaptor;

  @Test
  public void getMediumFetchesMediumFromRepository()
    throws MediumNotFoundException {
    Medium medium = new Medium();
    Mockito
      .when(mediumRepository.findById(12L))
      .thenReturn(Optional.of(medium));

    Medium returned = mediumService.getMedium(12L);
    assertEquals(medium, returned);
  }

  @Test
  public void getMediumThrowsWhenMediumCannotBeFoundInRepository() {
    Mockito.when(mediumRepository.findById(12L)).thenReturn(Optional.empty());

    assertThrows(
      MediumNotFoundException.class,
      () -> {
        mediumService.getMedium(12L);
      }
    );
  }

  @Test
  public void getThumbnailLinkForMediumCallsFileServiceToGetPath()
    throws MediumNotFoundException {
    Medium medium = new Medium();
    medium.setId(5L);
    medium.setReference("cdn:.jpg");

    Mockito.when(mediumRepository.findById(5L)).thenReturn(Optional.of(medium));
    Mockito
      .when(fileService.getExistingPathForFile("5.jpg", 512))
      .thenReturn("Correct");

    String path = mediumService.getThumbnailLinkForMedium(5L, 512);
    assertEquals("Correct", path);
  }

  @Test
  public void getThumbnailLinkForMediumAlwaysCallsWithPngWhenReferenceIsALink()
    throws MediumNotFoundException {
    Medium medium = new Medium();
    medium.setId(5L);
    medium.setReference("http://test.local/image");

    Mockito.when(mediumRepository.findById(5L)).thenReturn(Optional.of(medium));
    Mockito
      .when(fileService.getExistingPathForFile("5.png", 512))
      .thenReturn("Correct");

    String path = mediumService.getThumbnailLinkForMedium(5L, 512);
    assertEquals("Correct", path);
  }

  @Test
  public void saveLinkMediumCreatesMediumWithLinkAndProvidedInteraction() {
    Post post = new Post();

    Mockito
      .when(mediumRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    Medium medium = mediumService.saveLinkMedium(
      "http://test.local/image",
      post
    );

    assertEquals(post, medium.getInteraction());
    assertEquals("http://test.local/image", medium.getReference());
  }

  private static Stream<Arguments> fileNamesAndExpectedOutcomes() {
    return Stream.of(
      Arguments.of("alalal.jpg", ".jpg"),
      Arguments.of("123.png", ".png"),
      Arguments.of("h.jpeg", ".jpeg")
    );
  }

  @ParameterizedTest
  @MethodSource("fileNamesAndExpectedOutcomes")
  public void saveImageMediumSavesExtensionToDatabaseAndCallsFileServiceWithNewFileName(
    String fileName,
    String expectedExtension
  )
    throws Exception {
    Medium medium = new Medium();
    medium.setId(7L);
    MultipartFile file = Mockito.mock(MultipartFile.class);

    Mockito.when(file.getOriginalFilename()).thenReturn(fileName);
    Mockito
      .when(mediumRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Medium saved = mediumService.saveImageMedium(medium, file);
    assertEquals(medium, saved);
    assertEquals("cdn:" + expectedExtension, medium.getReference());

    verify(fileService, times(1)).saveImage(file, "7" + expectedExtension);
  }

  @Test
  public void saveAllFilesGeneratesMediaForAllFilesProvided() throws Exception {
    Post post = new Post();

    List<String> fileNames = List.of("123.jpg", "rt.png", "twitter.jpeg");
    List<String> expectedExtensions = List.of(".jpg", ".png", ".jpeg");
    List<MultipartFile> files = fileNames
      .stream()
      .map(
        name -> {
          MultipartFile file = Mockito.mock(MultipartFile.class);
          Mockito.when(file.getOriginalFilename()).thenReturn(name);
          return file;
        }
      )
      .toList();

    mediumService.saveAllFiles(files, post);

    verify(mediumRepository, times(3)).save(mediumCaptor.capture());

    List<Medium> media = mediumCaptor.getAllValues();

    for (Medium medium : media) {
      int index = media.indexOf(medium);
      verify(fileService, times(1))
        .saveImage(eq(files.get(index)), anyString());

      assertEquals(post, medium.getInteraction());
      assertEquals(
        "cdn:" + expectedExtensions.get(index),
        medium.getReference()
      );
    }
  }

  private static Stream<Arguments> selectedIdAndExpectedSlice() {
    return Stream.of(
      Arguments.of(12L, List.of(12L, 15L, 18L, 31L, 39L, 47L, 55L)),
      Arguments.of(15L, List.of(12L, 15L, 18L, 31L, 39L, 47L, 55L)),
      Arguments.of(15L, List.of(12L, 15L, 18L, 31L, 39L, 47L, 55L)),
      Arguments.of(18L, List.of(12L, 15L, 18L, 31L, 39L, 47L, 55L)),
      Arguments.of(31L, List.of(12L, 15L, 18L, 31L, 39L, 47L, 55L)),
      Arguments.of(39L, List.of(15L, 18L, 31L, 39L, 47L, 55L, 85L)),
      Arguments.of(47L, List.of(18L, 31L, 39L, 47L, 55L, 85L, 94L)),
      Arguments.of(55L, List.of(31L, 39L, 47L, 55L, 85L, 94L, 102L)),
      Arguments.of(85L, List.of(39L, 47L, 55L, 85L, 94L, 102L, 112L)),
      Arguments.of(94L, List.of(47L, 55L, 85L, 94L, 102L, 112L, 223L)),
      Arguments.of(102L, List.of(55L, 85L, 94L, 102L, 112L, 223L, 290L)),
      Arguments.of(112L, List.of(55L, 85L, 94L, 102L, 112L, 223L, 290L)),
      Arguments.of(223L, List.of(55L, 85L, 94L, 102L, 112L, 223L, 290L)),
      Arguments.of(290L, List.of(55L, 85L, 94L, 102L, 112L, 223L, 290L))
    );
  }

  @ParameterizedTest
  @MethodSource("selectedIdAndExpectedSlice")
  public void getPicturesForPictureViewerReturnsCorrectSliceOfMediumList(
    Long mediumId,
    List<Long> expectedReturnedIds
  ) {
    Medium a = new Medium();
    a.setId(12L);
    Medium b = new Medium();
    b.setId(15L);
    Medium c = new Medium();
    c.setId(18L);
    Medium d = new Medium();
    d.setId(31L);
    Medium e = new Medium();
    e.setId(39L);
    Medium f = new Medium();
    f.setId(47L);
    Medium g = new Medium();
    g.setId(55L);
    Medium h = new Medium();
    h.setId(85L);
    Medium i = new Medium();
    i.setId(94L);
    Medium j = new Medium();
    j.setId(102L);
    Medium k = new Medium();
    k.setId(112L);
    Medium l = new Medium();
    l.setId(223L);
    Medium m = new Medium();
    m.setId(290L);

    Mockito
      .when(mediumRepository.findAllPicturesFromTheSameInteractor(mediumId))
      .thenReturn(List.of(a, b, c, d, e, f, g, h, i, j, k, l, m));

    List<Medium> result = mediumService.getPicturesForPictureViewer(mediumId);
    List<Long> resultingIds = result
      .stream()
      .map(medium -> medium.getId())
      .toList();

    assertEquals(expectedReturnedIds, resultingIds);
  }

  public void getPicturesForPictureViewerReturnsTheWholeListIfItIsSmallEnough() {
    Medium a = new Medium();
    a.setId(12L);
    Medium b = new Medium();
    b.setId(15L);
    Medium c = new Medium();
    c.setId(18L);
    Medium d = new Medium();
    d.setId(31L);

    List<Medium> media = List.of(a, b, c, d);

    Mockito
      .when(mediumRepository.findAllPicturesFromTheSameInteractor(15L))
      .thenReturn(media);

    List<Medium> result = mediumService.getPicturesForPictureViewer(15L);
    assertEquals(media, result);
  }
}
