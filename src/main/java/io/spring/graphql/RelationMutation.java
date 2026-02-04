package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import io.spring.application.data.ProfileData;
import io.spring.application.facade.UserApiFacade;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class RelationMutation {

  private UserApiFacade userApiFacade;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.FollowUser)
  public ProfilePayload follow(@InputArgument("username") String username) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    ProfileData profileData = userApiFacade.followUser(username, user);
    Profile profile = buildProfile(profileData);
    return ProfilePayload.newBuilder().profile(profile).build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UnfollowUser)
  public ProfilePayload unfollow(@InputArgument("username") String username) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    ProfileData profileData = userApiFacade.unfollowUser(username, user);
    Profile profile = buildProfile(profileData);
    return ProfilePayload.newBuilder().profile(profile).build();
  }

  private Profile buildProfile(ProfileData profileData) {
    return Profile.newBuilder()
        .username(profileData.getUsername())
        .bio(profileData.getBio())
        .image(profileData.getImage())
        .following(profileData.isFollowing())
        .build();
  }
}
