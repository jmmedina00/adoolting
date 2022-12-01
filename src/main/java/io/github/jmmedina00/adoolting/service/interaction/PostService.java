package io.github.jmmedina00.adoolting.service.interaction;

import io.github.jmmedina00.adoolting.dto.interaction.NewPost;
import io.github.jmmedina00.adoolting.dto.interaction.NewPostOnPage;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.repository.interaction.PostRepository;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.MediumService;
import java.util.Objects;
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

  public Post postOnPage(NewPostOnPage newPost, Page page) throws Exception {
    Interactor interactor = interactorService.getInteractor(
      newPost.getPostAs()
    );
    return Objects.equals(interactor.getId(), page.getId())
      ? createPost(page, newPost)
      : postOnProfile(interactor, page, newPost);
  }

  public Post createPost(Interactor interactor, NewPost newPost) {
    Post post = new Post();
    post.setInteractor(interactor);
    post.setContent(newPost.getContents().trim());
    Post saved = postRepository.save(post);
    handleNewPostFiles(newPost, saved);
    return saved;
  }

  public Post postOnProfile(
    Interactor interactor,
    Interactor receiverInteractor,
    NewPost newPost
  ) {
    Post post = new Post();
    post.setInteractor(interactor);
    post.setReceiverInteractor(receiverInteractor);
    post.setContent(newPost.getContents().trim());
    Post saved = postRepository.save(post);
    handleNewPostFiles(newPost, saved);
    return saved;
  }

  private void handleNewPostFiles(NewPost post, Post savedPost) {
    try {
      mediumService.saveAllFiles(post.getMedia(), savedPost);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
