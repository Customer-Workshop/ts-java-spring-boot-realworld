package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import reactor.core.publisher.Mono;

@WebMvcTest(UsersApi.class)
@Import({
  WebSecurityConfig.class,
  UserQueryService.class,
  BCryptPasswordEncoder.class,
  JacksonCustomizations.class
})
public class UsersApiTest {
  @Autowired private MockMvc mvc;

  @MockBean private UserRepository userRepository;

  @MockBean private JwtService jwtService;

  @MockBean private UserReadService userReadService;

  @MockBean private UserService userService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private ObjectMapper objectMapper;

  private String defaultAvatar;

  @BeforeEach
  public void setUp() throws Exception {
    defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";
  }

  @Test
  public void should_create_user_success() throws Exception {
    String email = "john@jacob.com";
    String username = "johnjacob";

    when(jwtService.toToken(any())).thenReturn("123");
    User user = new User(email, username, "123", "", defaultAvatar);
    UserData userData = new UserData(user.getId(), email, username, "", defaultAvatar);
    when(userReadService.findById(any())).thenReturn(userData);

    when(userService.createUser(any())).thenReturn(Mono.just(user));

    when(userRepository.findByUsername(eq(username))).thenReturn(Mono.empty());
    when(userRepository.findByEmail(eq(email))).thenReturn(Mono.empty());

    Map<String, Object> param = prepareRegisterParameter(email, username);

    mvc.perform(
            MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.user.email").value(email))
        .andExpect(MockMvcResultMatchers.jsonPath("$.user.username").value(username))
        .andExpect(MockMvcResultMatchers.jsonPath("$.user.token").value("123"));

    verify(userService).createUser(any());
  }

    @Test
    public void should_show_error_message_for_blank_username() throws Exception {

      String email = "john@jacob.com";
      String username = "";

      when(userRepository.findByUsername(any())).thenReturn(Mono.empty());
      when(userRepository.findByEmail(any())).thenReturn(Mono.empty());

      Map<String, Object> param = prepareRegisterParameter(email, username);

    mvc.perform(
            MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
  }

    @Test
    public void should_show_error_message_for_invalid_email() throws Exception {
      String email = "johnxjacob.com";
      String username = "johnjacob";

      when(userRepository.findByUsername(any())).thenReturn(Mono.empty());
      when(userRepository.findByEmail(any())).thenReturn(Mono.empty());

      Map<String, Object> param = prepareRegisterParameter(email, username);

    mvc.perform(
            MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
  }

  @Test
  public void should_show_error_for_duplicated_username() throws Exception {
    String email = "john@jacob.com";
    String username = "johnjacob";

    when(userRepository.findByUsername(eq(username)))
        .thenReturn(Mono.just(new User(email, username, "123", "bio", "")));
    when(userRepository.findByEmail(any())).thenReturn(Mono.empty());

    Map<String, Object> param = prepareRegisterParameter(email, username);

    mvc.perform(
            MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
  }

  @Test
  public void should_show_error_for_duplicated_email() throws Exception {
    String email = "john@jacob.com";
    String username = "johnjacob2";

    when(userRepository.findByEmail(eq(email)))
        .thenReturn(Mono.just(new User(email, username, "123", "bio", "")));

    when(userRepository.findByUsername(eq(username))).thenReturn(Mono.empty());

    Map<String, Object> param = prepareRegisterParameter(email, username);

    mvc.perform(
            MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
  }

  private HashMap<String, Object> prepareRegisterParameter(
      final String email, final String username) {
    return new HashMap<String, Object>() {
      {
        put(
            "user",
            new HashMap<String, Object>() {
              {
                put("email", email);
                put("password", "johnnyjacob");
                put("username", username);
              }
            });
      }
    };
  }

  @Test
  public void should_login_success() throws Exception {
    String email = "john@jacob.com";
    String username = "johnjacob2";
    String password = "123";

    User user = new User(email, username, passwordEncoder.encode(password), "", defaultAvatar);
    UserData userData = new UserData("123", email, username, "", defaultAvatar);

    when(userRepository.findByEmail(eq(email))).thenReturn(Mono.just(user));
    when(userReadService.findByUsername(eq(username))).thenReturn(userData);
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
    when(jwtService.toToken(any())).thenReturn("123");

    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("email", email);
                    put("password", password);
                  }
                });
          }
        };

    mvc.perform(
            MockMvcRequestBuilders.post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.user.email").value(email))
        .andExpect(MockMvcResultMatchers.jsonPath("$.user.username").value(username))
        .andExpect(MockMvcResultMatchers.jsonPath("$.user.token").value("123"));
  }

  @Test
  public void should_fail_login_with_wrong_password() throws Exception {
    String email = "john@jacob.com";
    String username = "johnjacob2";
    String password = "123";

    User user = new User(email, username, password, "", defaultAvatar);
    UserData userData = new UserData(user.getId(), email, username, "", defaultAvatar);

    when(userRepository.findByEmail(eq(email))).thenReturn(Mono.just(user));
    when(userReadService.findByUsername(eq(username))).thenReturn(userData);

    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("email", email);
                    put("password", "123123");
                  }
                });
          }
        };

    mvc.perform(
            MockMvcRequestBuilders.post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
  }
}
