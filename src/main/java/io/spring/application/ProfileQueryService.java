package io.spring.application;

import io.spring.application.data.ProfileData;
import io.spring.application.data.ProfileSearchResult;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProfileQueryService {
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  public Optional<ProfileData> findByUsername(String username, User currentUser) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return Optional.empty();
    } else {
      ProfileData profileData =
          new ProfileData(
              userData.getId(),
              userData.getUsername(),
              userData.getBio(),
              userData.getImage(),
              currentUser != null
                  && userRelationshipQueryService.isUserFollowing(
                      currentUser.getId(), userData.getId()));
      return Optional.of(profileData);
    }
  }

  public List<ProfileSearchResult> searchProfiles(String query, User currentUser) {
    List<UserData> users = userReadService.searchByUsername(query);
    return users.stream()
        .map(
            userData -> {
              ProfileData profileData =
                  new ProfileData(
                      userData.getId(),
                      userData.getUsername(),
                      userData.getBio(),
                      userData.getImage(),
                      currentUser != null
                          && userRelationshipQueryService.isUserFollowing(
                              currentUser.getId(), userData.getId()));
              int articleCount = userReadService.countArticlesByUserId(userData.getId());
              return new ProfileSearchResult(profileData, articleCount);
            })
        .collect(Collectors.toList());
  }
}
