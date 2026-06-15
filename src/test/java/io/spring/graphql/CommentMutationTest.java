package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class CommentMutationTest {

  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;
  private CommentMutation commentMutation;
  private User user;

  @BeforeEach
  void setUp() {
    articleRepository = mock(ArticleRepository.class);
    commentRepository = mock(CommentRepository.class);
    commentQueryService = mock(CommentQueryService.class);
    commentMutation =
        new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_comment_successfully() {
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    CommentData commentData =
        new CommentData(
            "cid",
            "Great article!",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("title", "Great article!");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(commentData, result.getLocalContext());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_throw_when_creating_comment_without_auth() {
    AnonymousAuthenticationToken anon =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anon);
    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("slug", "body"));
  }

  @Test
  void should_throw_when_creating_comment_on_nonexistent_article() {
    when(articleRepository.findBySlug("nope")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment("nope", "body"));
  }

  @Test
  void should_delete_comment_successfully() {
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("title", comment.getId());

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_when_deleting_comment_without_auth() {
    AnonymousAuthenticationToken anon =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anon);
    assertThrows(AuthenticationException.class, () -> commentMutation.removeComment("slug", "cid"));
  }

  @Test
  void should_throw_when_deleting_comment_on_nonexistent_article() {
    when(articleRepository.findBySlug("nope")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment("nope", "cid"));
  }

  @Test
  void should_throw_when_deleting_nonexistent_comment() {
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("title", "nonexistent"));
  }

  @Test
  void should_throw_when_deleting_comment_without_authorization() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    Comment comment = new Comment("body", otherUser.getId(), article.getId());
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("title", comment.getId()));
  }
}
