package io.spring.application.comment;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Read-model / persistence tier tests for the comment feature, per feature spec issue #188. Backs
 * the criteria that depend on real query-service + database behavior: AC7 (viewer follow state),
 * AC8 (anonymous viewer follow state), and AC14 (author profile + creation timestamp per comment).
 */
@Import({
  MyBatisCommentRepository.class,
  MyBatisUserRepository.class,
  CommentQueryService.class,
  MyBatisArticleRepository.class
})
public class CommentReadModelTest extends DbTestBase {
  @Autowired private CommentRepository commentRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CommentQueryService commentQueryService;
  @Autowired private ArticleRepository articleRepository;

  private User viewer;
  private User followedAuthor;
  private User strangerAuthor;
  private Article article;

  @BeforeEach
  public void setUp() {
    viewer = new User("viewer@test.com", "viewer", "123", "", "");
    followedAuthor =
        new User("followed@test.com", "followed", "123", "followed bio", "followed.png");
    strangerAuthor = new User("stranger@test.com", "stranger", "123", "", "");
    userRepository.save(viewer);
    userRepository.save(followedAuthor);
    userRepository.save(strangerAuthor);

    article = new Article("title", "desc", "body", Arrays.asList("java"), viewer.getId());
    articleRepository.save(article);
  }

  // AC7: for an authenticated viewer, each comment's author.following reflects the viewer's follow
  // state.
  @Test
  @DisplayName("AC7: viewer's follow state drives per-comment author.following")
  public void followingReflectsViewerFollowState() {
    userRepository.saveRelation(new FollowRelation(viewer.getId(), followedAuthor.getId()));

    commentRepository.save(new Comment("from followed", followedAuthor.getId(), article.getId()));
    commentRepository.save(new Comment("from stranger", strangerAuthor.getId(), article.getId()));

    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), viewer);

    Map<String, Boolean> followingByAuthor =
        comments.stream()
            .collect(
                Collectors.toMap(
                    c -> c.getProfileData().getUsername(), c -> c.getProfileData().isFollowing()));
    Assertions.assertEquals(2, comments.size());
    Assertions.assertTrue(followingByAuthor.get(followedAuthor.getUsername()));
    Assertions.assertFalse(followingByAuthor.get(strangerAuthor.getUsername()));
  }

  // AC8: an anonymous viewer (null user) sees author.following = false for every comment.
  @Test
  @DisplayName("AC8: anonymous viewer sees following=false for all comments")
  public void anonymousViewerSeesFollowingFalse() {
    userRepository.saveRelation(new FollowRelation(viewer.getId(), followedAuthor.getId()));
    commentRepository.save(new Comment("from followed", followedAuthor.getId(), article.getId()));

    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), null);

    Assertions.assertEquals(1, comments.size());
    Assertions.assertFalse(comments.get(0).getProfileData().isFollowing());
  }

  // AC14: the read model returns the correct author profile and creation timestamp per comment.
  @Test
  @DisplayName("AC14: read model returns correct author profile and createdAt per comment")
  public void readModelReturnsAuthorProfileAndTimestampPerComment() {
    Comment fromFollowed = new Comment("from followed", followedAuthor.getId(), article.getId());
    Comment fromStranger = new Comment("from stranger", strangerAuthor.getId(), article.getId());
    commentRepository.save(fromFollowed);
    commentRepository.save(fromStranger);

    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), viewer);
    Map<String, CommentData> byId =
        comments.stream().collect(Collectors.toMap(CommentData::getId, Function.identity()));

    CommentData followedData = byId.get(fromFollowed.getId());
    Assertions.assertNotNull(followedData);
    Assertions.assertEquals(
        followedAuthor.getUsername(), followedData.getProfileData().getUsername());
    Assertions.assertEquals(followedAuthor.getBio(), followedData.getProfileData().getBio());
    Assertions.assertEquals(followedAuthor.getImage(), followedData.getProfileData().getImage());
    Assertions.assertNotNull(followedData.getCreatedAt());

    CommentData strangerData = byId.get(fromStranger.getId());
    Assertions.assertNotNull(strangerData);
    Assertions.assertEquals(
        strangerAuthor.getUsername(), strangerData.getProfileData().getUsername());
    Assertions.assertNotNull(strangerData.getCreatedAt());

    // Each comment maps to its own author's profile, not another's.
    Assertions.assertNotEquals(
        followedData.getProfileData().getUsername(), strangerData.getProfileData().getUsername());
  }
}
