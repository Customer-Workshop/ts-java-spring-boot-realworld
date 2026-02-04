package io.spring.application.facade;

import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.UserQueryService;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserApiFacade {

  private final UserRepository userRepository;
  private final UserQueryService userQueryService;
  private final ProfileQueryService profileQueryService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserService userService;

  public UserWithToken registerUser(RegisterParam registerParam) {
    User user = userService.createUser(registerParam);
    UserData userData =
        userQueryService.findById(user.getId()).orElseThrow(() -> new ResourceNotFoundException());
    return new UserWithToken(userData, jwtService.toToken(user));
  }

  public UserWithToken login(String email, String password) {
    Optional<User> optional = userRepository.findByEmail(email);
    if (optional.isPresent() && passwordEncoder.matches(password, optional.get().getPassword())) {
      UserData userData =
          userQueryService
              .findById(optional.get().getId())
              .orElseThrow(() -> new ResourceNotFoundException());
      return new UserWithToken(userData, jwtService.toToken(optional.get()));
    } else {
      throw new InvalidAuthenticationException();
    }
  }

  public UserWithToken getCurrentUser(User currentUser, String token) {
    UserData userData =
        userQueryService
            .findById(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException());
    return new UserWithToken(userData, token);
  }

  public UserWithToken updateUser(User currentUser, UpdateUserParam updateUserParam, String token) {
    userService.updateUser(new UpdateUserCommand(currentUser, updateUserParam));
    UserData userData =
        userQueryService
            .findById(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException());
    return new UserWithToken(userData, token);
  }

  public ProfileData getProfile(String username, User currentUser) {
    return profileQueryService
        .findByUsername(username, currentUser)
        .orElseThrow(ResourceNotFoundException::new);
  }

  public ProfileData followUser(String username, User currentUser) {
    User target =
        userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    FollowRelation followRelation = new FollowRelation(currentUser.getId(), target.getId());
    userRepository.saveRelation(followRelation);
    return profileQueryService
        .findByUsername(username, currentUser)
        .orElseThrow(ResourceNotFoundException::new);
  }

  public ProfileData unfollowUser(String username, User currentUser) {
    User target =
        userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    return userRepository
        .findRelation(currentUser.getId(), target.getId())
        .map(
            relation -> {
              userRepository.removeRelation(relation);
              return profileQueryService
                  .findByUsername(username, currentUser)
                  .orElseThrow(ResourceNotFoundException::new);
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}
