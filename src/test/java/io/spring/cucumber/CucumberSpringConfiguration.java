package io.spring.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import io.spring.JacksonCustomizations;
import io.spring.api.CurrentUserApi;
import io.spring.api.ProfileApi;
import io.spring.api.UsersApi;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ProfileQueryService;
import io.spring.application.UserQueryService;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@CucumberContextConfiguration
@WebMvcTest({UsersApi.class, CurrentUserApi.class, ProfileApi.class})
@Import({
  WebSecurityConfig.class,
  UserQueryService.class,
  ProfileQueryService.class,
  BCryptPasswordEncoder.class,
  JacksonCustomizations.class,
  ScenarioContext.class
})
public class CucumberSpringConfiguration {

  @MockBean private UserRepository userRepository;

  @MockBean private JwtService jwtService;

  @MockBean private UserReadService userReadService;

  @MockBean private UserService userService;

  @MockBean private UserRelationshipQueryService userRelationshipQueryService;
}
