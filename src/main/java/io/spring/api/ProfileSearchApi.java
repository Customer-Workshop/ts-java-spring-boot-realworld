package io.spring.api;

import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileSearchResult;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/profiles")
@AllArgsConstructor
public class ProfileSearchApi {
  private ProfileQueryService profileQueryService;

  @GetMapping(path = "/search")
  public ResponseEntity searchProfiles(
      @RequestParam(value = "q", required = true) String query,
      @AuthenticationPrincipal User user) {
    List<ProfileSearchResult> results = profileQueryService.searchProfiles(query, user);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("profiles", results);
            put("profilesCount", results.size());
          }
        });
  }
}
