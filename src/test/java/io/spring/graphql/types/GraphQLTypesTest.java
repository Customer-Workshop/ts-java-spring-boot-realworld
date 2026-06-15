package io.spring.graphql.types;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class GraphQLTypesTest {

  @Test
  void should_build_article_type() {
    Profile author = Profile.newBuilder().username("author").build();
    Article article =
        Article.newBuilder()
            .slug("my-article")
            .title("My Article")
            .description("desc")
            .body("body content")
            .favorited(true)
            .favoritesCount(5)
            .createdAt("2023-01-01")
            .updatedAt("2023-01-02")
            .tagList(Arrays.asList("java", "spring"))
            .author(author)
            .build();

    assertEquals("my-article", article.getSlug());
    assertEquals("My Article", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body content", article.getBody());
    assertTrue(article.getFavorited());
    assertEquals(5, article.getFavoritesCount());
    assertEquals("2023-01-01", article.getCreatedAt());
    assertEquals("2023-01-02", article.getUpdatedAt());
    assertEquals(Arrays.asList("java", "spring"), article.getTagList());
    assertEquals(author, article.getAuthor());
    assertNotNull(article.toString());
    assertNotNull(article.hashCode());
  }

  @Test
  void should_test_article_setters() {
    Article article = new Article();
    Profile author = Profile.newBuilder().username("u").build();
    article.setSlug("slug");
    article.setTitle("title");
    article.setDescription("desc");
    article.setBody("body");
    article.setFavorited(false);
    article.setFavoritesCount(3);
    article.setCreatedAt("created");
    article.setUpdatedAt("updated");
    article.setTagList(Arrays.asList("tag"));
    article.setAuthor(author);
    article.setComments(null);

    assertEquals("slug", article.getSlug());
    assertEquals("title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertFalse(article.getFavorited());
    assertEquals(3, article.getFavoritesCount());
    assertNull(article.getComments());
  }

  @Test
  void should_test_article_equals() {
    Article a1 = Article.newBuilder().slug("s").title("t").build();
    Article a2 = Article.newBuilder().slug("s").title("t").build();
    Article a3 = Article.newBuilder().slug("other").build();

    assertEquals(a1, a2);
    assertEquals(a1, a1);
    assertNotEquals(a1, a3);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
  }

  @Test
  void should_build_article_edge() {
    Article article = Article.newBuilder().slug("s").build();
    ArticleEdge edge = ArticleEdge.newBuilder().node(article).cursor("cursor1").build();

    assertEquals(article, edge.getNode());
    assertEquals("cursor1", edge.getCursor());
    assertNotNull(edge.toString());
  }

  @Test
  void should_test_article_edge_setters_and_equals() {
    ArticleEdge edge = new ArticleEdge();
    edge.setNode(Article.newBuilder().slug("x").build());
    edge.setCursor("c");
    assertEquals("c", edge.getCursor());
    assertNotNull(edge.getNode());

    ArticleEdge edge2 = ArticleEdge.newBuilder().node(edge.getNode()).cursor("c").build();
    assertEquals(edge, edge2);
    assertEquals(edge.hashCode(), edge2.hashCode());
  }

  @Test
  void should_build_articles_connection() {
    Article article = Article.newBuilder().slug("s").build();
    ArticleEdge edge = ArticleEdge.newBuilder().node(article).cursor("c").build();
    ArticlesConnection conn = ArticlesConnection.newBuilder().edges(Arrays.asList(edge)).build();

    assertEquals(1, conn.getEdges().size());
    assertNotNull(conn.toString());
  }

  @Test
  void should_test_articles_connection_setters_and_equals() {
    ArticlesConnection conn = new ArticlesConnection();
    conn.setEdges(Arrays.asList());
    assertNotNull(conn.getEdges());

    ArticlesConnection conn2 = new ArticlesConnection();
    conn2.setEdges(Arrays.asList());
    assertEquals(conn, conn2);
    assertEquals(conn.hashCode(), conn2.hashCode());
  }

  @Test
  void should_build_comment_type() {
    Comment comment =
        Comment.newBuilder()
            .id("c1")
            .body("comment body")
            .createdAt("2023-01-01")
            .updatedAt("2023-01-02")
            .build();

    assertEquals("c1", comment.getId());
    assertEquals("comment body", comment.getBody());
    assertEquals("2023-01-01", comment.getCreatedAt());
    assertEquals("2023-01-02", comment.getUpdatedAt());
    assertNotNull(comment.toString());
  }

  @Test
  void should_test_comment_setters_and_equals() {
    Comment c = new Comment();
    c.setId("id");
    c.setBody("body");
    c.setCreatedAt("cr");
    c.setUpdatedAt("up");
    c.setArticle(null);
    c.setAuthor(null);
    assertEquals("id", c.getId());
    assertNull(c.getArticle());
    assertNull(c.getAuthor());

    Comment c2 = Comment.newBuilder().id("id").body("body").createdAt("cr").updatedAt("up").build();
    assertEquals(c, c2);
    assertEquals(c.hashCode(), c2.hashCode());
  }

  @Test
  void should_build_comment_edge() {
    Comment comment = Comment.newBuilder().id("c1").build();
    CommentEdge edge = CommentEdge.newBuilder().node(comment).cursor("cur").build();

    assertEquals(comment, edge.getNode());
    assertEquals("cur", edge.getCursor());
    assertNotNull(edge.toString());
  }

  @Test
  void should_test_comment_edge_setters_and_equals() {
    CommentEdge edge = new CommentEdge();
    edge.setNode(Comment.newBuilder().id("x").build());
    edge.setCursor("c");
    assertEquals("c", edge.getCursor());

    CommentEdge edge2 = CommentEdge.newBuilder().node(edge.getNode()).cursor("c").build();
    assertEquals(edge, edge2);
    assertEquals(edge.hashCode(), edge2.hashCode());
  }

  @Test
  void should_build_comments_connection() {
    Comment comment = Comment.newBuilder().id("c1").build();
    CommentEdge edge = CommentEdge.newBuilder().node(comment).cursor("c").build();
    CommentsConnection conn = CommentsConnection.newBuilder().edges(Arrays.asList(edge)).build();

    assertEquals(1, conn.getEdges().size());
    assertNotNull(conn.toString());
  }

  @Test
  void should_test_comments_connection_setters_and_equals() {
    CommentsConnection conn = new CommentsConnection();
    conn.setEdges(Arrays.asList());
    assertNotNull(conn.getEdges());

    CommentsConnection conn2 = new CommentsConnection();
    conn2.setEdges(Arrays.asList());
    assertEquals(conn, conn2);
    assertEquals(conn.hashCode(), conn2.hashCode());
  }

  @Test
  void should_build_page_info() {
    PageInfo pi =
        PageInfo.newBuilder()
            .hasNextPage(true)
            .hasPreviousPage(false)
            .startCursor("start")
            .endCursor("end")
            .build();

    assertTrue(pi.getHasNextPage());
    assertFalse(pi.getHasPreviousPage());
    assertEquals("start", pi.getStartCursor());
    assertEquals("end", pi.getEndCursor());
    assertNotNull(pi.toString());
  }

  @Test
  void should_test_page_info_setters_and_equals() {
    PageInfo pi = new PageInfo();
    pi.setHasNextPage(true);
    pi.setHasPreviousPage(true);
    pi.setStartCursor("s");
    pi.setEndCursor("e");
    assertTrue(pi.getHasNextPage());
    assertTrue(pi.getHasPreviousPage());

    PageInfo pi2 =
        PageInfo.newBuilder()
            .hasNextPage(true)
            .hasPreviousPage(true)
            .startCursor("s")
            .endCursor("e")
            .build();
    assertEquals(pi, pi2);
    assertEquals(pi.hashCode(), pi2.hashCode());
  }

  @Test
  void should_build_page_info_all_args() {
    PageInfo pi = new PageInfo("end", true, false, "start");
    assertTrue(pi.getHasNextPage());
    assertFalse(pi.getHasPreviousPage());
    assertEquals("start", pi.getStartCursor());
    assertEquals("end", pi.getEndCursor());
  }

  @Test
  void should_build_profile() {
    Profile profile =
        Profile.newBuilder()
            .username("user1")
            .bio("my bio")
            .image("http://img.com/a.jpg")
            .following(true)
            .build();

    assertEquals("user1", profile.getUsername());
    assertEquals("my bio", profile.getBio());
    assertEquals("http://img.com/a.jpg", profile.getImage());
    assertTrue(profile.getFollowing());
    assertNotNull(profile.toString());
  }

  @Test
  void should_test_profile_setters_and_equals() {
    Profile p = new Profile();
    p.setUsername("u");
    p.setBio("b");
    p.setImage("i");
    p.setFollowing(false);
    p.setArticles(null);
    p.setFavorites(null);
    p.setFeed(null);
    assertEquals("u", p.getUsername());
    assertNull(p.getArticles());
    assertNull(p.getFavorites());
    assertNull(p.getFeed());

    Profile p2 = Profile.newBuilder().username("u").bio("b").image("i").following(false).build();
    assertEquals(p, p2);
    assertEquals(p.hashCode(), p2.hashCode());
  }

  @Test
  void should_build_profile_payload() {
    Profile profile = Profile.newBuilder().username("u").build();
    ProfilePayload payload = ProfilePayload.newBuilder().profile(profile).build();

    assertEquals(profile, payload.getProfile());
    assertNotNull(payload.toString());
  }

  @Test
  void should_test_profile_payload_setters_and_equals() {
    ProfilePayload pp = new ProfilePayload();
    pp.setProfile(Profile.newBuilder().username("u").build());
    assertNotNull(pp.getProfile());

    ProfilePayload pp2 = ProfilePayload.newBuilder().profile(pp.getProfile()).build();
    assertEquals(pp, pp2);
    assertEquals(pp.hashCode(), pp2.hashCode());
  }

  @Test
  void should_build_user_type() {
    User user = User.newBuilder().email("e@t.com").username("user1").token("token123").build();

    assertEquals("e@t.com", user.getEmail());
    assertEquals("user1", user.getUsername());
    assertEquals("token123", user.getToken());
    assertNotNull(user.toString());
  }

  @Test
  void should_test_user_setters_and_equals() {
    User u = new User();
    u.setEmail("e");
    u.setUsername("u");
    u.setToken("t");
    u.setProfile(null);
    assertEquals("e", u.getEmail());
    assertNull(u.getProfile());

    User u2 = User.newBuilder().email("e").username("u").token("t").build();
    assertEquals(u, u2);
    assertEquals(u.hashCode(), u2.hashCode());
  }

  @Test
  void should_build_user_payload() {
    User user = User.newBuilder().email("e").build();
    UserPayload payload = UserPayload.newBuilder().user(user).build();

    assertEquals(user, payload.getUser());
    assertNotNull(payload.toString());
  }

  @Test
  void should_test_user_payload_setters_and_equals() {
    UserPayload up = new UserPayload();
    up.setUser(User.newBuilder().email("e").build());
    assertNotNull(up.getUser());

    UserPayload up2 = UserPayload.newBuilder().user(up.getUser()).build();
    assertEquals(up, up2);
    assertEquals(up.hashCode(), up2.hashCode());
  }

  @Test
  void should_test_user_result_is_interface() {
    UserPayload payload = UserPayload.newBuilder().user(User.newBuilder().build()).build();
    assertTrue(payload instanceof UserResult);

    Error error = Error.newBuilder().message("err").build();
    assertTrue(error instanceof UserResult);
  }

  @Test
  void should_build_deletion_status() {
    DeletionStatus status = DeletionStatus.newBuilder().success(true).build();
    assertTrue(status.getSuccess());
    assertNotNull(status.toString());
  }

  @Test
  void should_test_deletion_status_setters_and_equals() {
    DeletionStatus ds = new DeletionStatus();
    ds.setSuccess(false);
    assertFalse(ds.getSuccess());

    DeletionStatus ds2 = DeletionStatus.newBuilder().success(false).build();
    assertEquals(ds, ds2);
    assertEquals(ds.hashCode(), ds2.hashCode());
  }

  @Test
  void should_build_create_article_input() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();

    assertEquals("title", input.getTitle());
    assertEquals("desc", input.getDescription());
    assertEquals("body", input.getBody());
    assertEquals(Arrays.asList("java"), input.getTagList());
    assertNotNull(input.toString());
  }

  @Test
  void should_test_create_article_input_setters_and_equals() {
    CreateArticleInput i = new CreateArticleInput();
    i.setTitle("t");
    i.setDescription("d");
    i.setBody("b");
    i.setTagList(Arrays.asList("x"));
    assertEquals("t", i.getTitle());

    CreateArticleInput i2 =
        CreateArticleInput.newBuilder()
            .title("t")
            .description("d")
            .body("b")
            .tagList(Arrays.asList("x"))
            .build();
    assertEquals(i, i2);
    assertEquals(i.hashCode(), i2.hashCode());
  }

  @Test
  void should_build_update_article_input() {
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("new title")
            .description("new desc")
            .body("new body")
            .build();

    assertEquals("new title", input.getTitle());
    assertEquals("new desc", input.getDescription());
    assertEquals("new body", input.getBody());
    assertNotNull(input.toString());
  }

  @Test
  void should_test_update_article_input_setters_and_equals() {
    UpdateArticleInput i = new UpdateArticleInput();
    i.setTitle("t");
    i.setDescription("d");
    i.setBody("b");
    assertEquals("t", i.getTitle());

    UpdateArticleInput i2 =
        UpdateArticleInput.newBuilder().title("t").description("d").body("b").build();
    assertEquals(i, i2);
    assertEquals(i.hashCode(), i2.hashCode());
  }

  @Test
  void should_build_create_user_input() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("e@t.com").username("user").password("pass").build();

    assertEquals("e@t.com", input.getEmail());
    assertEquals("user", input.getUsername());
    assertEquals("pass", input.getPassword());
    assertNotNull(input.toString());
  }

  @Test
  void should_test_create_user_input_setters_and_equals() {
    CreateUserInput i = new CreateUserInput();
    i.setEmail("e");
    i.setUsername("u");
    i.setPassword("p");
    assertEquals("e", i.getEmail());

    CreateUserInput i2 =
        CreateUserInput.newBuilder().email("e").username("u").password("p").build();
    assertEquals(i, i2);
    assertEquals(i.hashCode(), i2.hashCode());
  }

  @Test
  void should_build_update_user_input() {
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("e@t.com")
            .username("user")
            .password("pass")
            .bio("bio")
            .image("img")
            .build();

    assertEquals("e@t.com", input.getEmail());
    assertEquals("user", input.getUsername());
    assertEquals("pass", input.getPassword());
    assertEquals("bio", input.getBio());
    assertEquals("img", input.getImage());
    assertNotNull(input.toString());
  }

  @Test
  void should_test_update_user_input_setters_and_equals() {
    UpdateUserInput i = new UpdateUserInput();
    i.setEmail("e");
    i.setUsername("u");
    i.setPassword("p");
    i.setBio("b");
    i.setImage("i");
    assertEquals("e", i.getEmail());

    UpdateUserInput i2 =
        UpdateUserInput.newBuilder()
            .email("e")
            .username("u")
            .password("p")
            .bio("b")
            .image("i")
            .build();
    assertEquals(i, i2);
    assertEquals(i.hashCode(), i2.hashCode());
  }

  @Test
  void should_build_error() {
    ErrorItem item = ErrorItem.newBuilder().key("field").value(Arrays.asList("err1")).build();
    Error error = Error.newBuilder().message("BAD_REQUEST").errors(Arrays.asList(item)).build();

    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(1, error.getErrors().size());
    assertNotNull(error.toString());
  }

  @Test
  void should_test_error_setters_and_equals() {
    Error e = new Error();
    e.setMessage("msg");
    e.setErrors(Arrays.asList());
    assertEquals("msg", e.getMessage());

    Error e2 = Error.newBuilder().message("msg").errors(Arrays.asList()).build();
    assertEquals(e, e2);
    assertEquals(e.hashCode(), e2.hashCode());
  }

  @Test
  void should_build_error_item() {
    List<String> values = Arrays.asList("v1", "v2");
    ErrorItem item = ErrorItem.newBuilder().key("field").value(values).build();

    assertEquals("field", item.getKey());
    assertEquals(values, item.getValue());
    assertNotNull(item.toString());
  }

  @Test
  void should_test_error_item_setters_and_equals() {
    ErrorItem ei = new ErrorItem();
    ei.setKey("k");
    ei.setValue(Arrays.asList("v"));
    assertEquals("k", ei.getKey());

    ErrorItem ei2 = ErrorItem.newBuilder().key("k").value(Arrays.asList("v")).build();
    assertEquals(ei, ei2);
    assertEquals(ei.hashCode(), ei2.hashCode());
  }

  @Test
  void should_build_article_payload() {
    Article article = Article.newBuilder().slug("s").build();
    ArticlePayload payload = ArticlePayload.newBuilder().article(article).build();

    assertEquals(article, payload.getArticle());
    assertNotNull(payload.toString());
  }

  @Test
  void should_test_article_payload_setters_and_equals() {
    ArticlePayload ap = new ArticlePayload();
    ap.setArticle(Article.newBuilder().slug("s").build());
    assertNotNull(ap.getArticle());

    ArticlePayload ap2 = ArticlePayload.newBuilder().article(ap.getArticle()).build();
    assertEquals(ap, ap2);
    assertEquals(ap.hashCode(), ap2.hashCode());
  }

  @Test
  void should_build_comment_payload() {
    Comment comment = Comment.newBuilder().id("c1").build();
    CommentPayload payload = CommentPayload.newBuilder().comment(comment).build();

    assertEquals(comment, payload.getComment());
    assertNotNull(payload.toString());
  }

  @Test
  void should_test_comment_payload_setters_and_equals() {
    CommentPayload cp = new CommentPayload();
    cp.setComment(Comment.newBuilder().id("c1").build());
    assertNotNull(cp.getComment());

    CommentPayload cp2 = CommentPayload.newBuilder().comment(cp.getComment()).build();
    assertEquals(cp, cp2);
    assertEquals(cp.hashCode(), cp2.hashCode());
  }

  @Test
  void should_build_article_with_all_args_constructor() {
    Profile author = Profile.newBuilder().username("a").build();
    CommentsConnection cc = CommentsConnection.newBuilder().edges(Arrays.asList()).build();
    Article article =
        new Article(
            author, "body", cc, "cr", "desc", true, 3, "slug", Arrays.asList("t"), "title", "up");

    assertEquals(author, article.getAuthor());
    assertEquals("body", article.getBody());
    assertEquals(cc, article.getComments());
    assertEquals("cr", article.getCreatedAt());
    assertEquals("desc", article.getDescription());
    assertTrue(article.getFavorited());
    assertEquals(3, article.getFavoritesCount());
    assertEquals("slug", article.getSlug());
    assertEquals("title", article.getTitle());
    assertEquals("up", article.getUpdatedAt());
  }
}
