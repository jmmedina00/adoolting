package io.github.jmmedina00.adoolting.service.interaction;

import io.github.jmmedina00.adoolting.dto.interaction.NewPost;
import io.github.jmmedina00.adoolting.dto.interaction.NewPostOnPage;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.interaction.PostRepository;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.MediumService;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {
  @Autowired
  private PostRepository postRepository;

  @Autowired
  private MediumService mediumService;

  @Autowired
  private InteractorService interactorService;

  public Post postOnPage(NewPostOnPage newPost, Long pageId)
    throws NotAuthorizedException {
    return postOnProfile(newPost.getPostAs(), pageId, newPost);
  }

  public Post createPost(Long interactorId, NewPost newPost) {
    Interactor interactor = interactorService.getInteractor(interactorId);

    Post post = new Post();
    post.setInteractor(interactor);
    post.setContent(newPost.getContents().trim());
    Post saved = postRepository.save(post);
    handleNewPostFiles(newPost, saved);
    return saved;
  }

  public Post postOnProfile(
    Long interactorId,
    Long receiverInteractorId,
    NewPost newPost
  )
    throws NotAuthorizedException {
    if (Objects.equals(interactorId, receiverInteractorId)) {
      return createPost(interactorId, newPost);
    }

    Interactor interactor = interactorService.getInteractor(interactorId);
    Interactor receiverInteractor = interactorService.getInteractor(
      receiverInteractorId
    );

    if (interactor instanceof Page && receiverInteractor instanceof Person) {
      throw new NotAuthorizedException();
    }

    Post post = new Post();
    post.setInteractor(interactor);
    post.setReceiverInteractor(receiverInteractor);
    post.setContent(newPost.getContents().trim());
    Post saved = postRepository.save(post);
    handleNewPostFiles(newPost, saved);
    return saved;
  }

  private void handleNewPostFiles(NewPost post, Post savedPost) {
    if (!Optional.of(post.getUrl()).orElse("").isEmpty()) {
      mediumService.saveLinkMedium(post.getUrl(), savedPost);
      return;
    }

    try {
      mediumService.saveAllFiles(post.getMedia(), savedPost);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
