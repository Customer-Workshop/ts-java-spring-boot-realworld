package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.application.data.CommentData;
import io.spring.application.facade.CommentApiFacade;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private CommentApiFacade commentApiFacade;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    CommentData commentData = commentApiFacade.createComment(slug, newCommentParam.getBody(), user);
    return ResponseEntity.status(201).body(commentResponse(commentData));
  }

  @GetMapping
  public ResponseEntity getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    List<CommentData> comments = commentApiFacade.getComments(slug, user);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal User user) {
    commentApiFacade.deleteComment(slug, commentId, user);
    return ResponseEntity.noContent().build();
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }
}

@Getter
@NoArgsConstructor
@JsonRootName("comment")
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
