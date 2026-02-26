Feature: User Login API
  As a registered user of the RealWorld application
  I want to log in using my email and password
  So that I receive a JWT token and can access authenticated endpoints

  Background:
    Given a registered user exists with email "login-test@example.com" and password "password123" and username "loginuser"

  # -----------------------------------------------------------------
  # Scenario 1: Valid login
  # -----------------------------------------------------------------
  Scenario: Successful login with valid credentials
    When I send a login request with email "login-test@example.com" and password "password123"
    Then the response status code should be 200
    And the response body should contain a "user" object
    And the "user" object should contain the field "email" with value "login-test@example.com"
    And the "user" object should contain the field "username" with value "loginuser"
    And the "user" object should contain a non-empty "token"
    And the "user" object should contain the field "bio"
    And the "user" object should contain the field "image"

  # -----------------------------------------------------------------
  # Scenario 2: Invalid password
  # -----------------------------------------------------------------
  Scenario: Login fails with incorrect password
    When I send a login request with email "login-test@example.com" and password "wrongpassword"
    Then the response status code should be 422
    And the response body should contain the message "invalid email or password"

  # -----------------------------------------------------------------
  # Scenario 3: Non-existent email
  # -----------------------------------------------------------------
  Scenario: Login fails with non-existent email
    When I send a login request with email "nobody@example.com" and password "password123"
    Then the response status code should be 422
    And the response body should contain the message "invalid email or password"

  # -----------------------------------------------------------------
  # Scenario 4 & 5: Missing fields (scenario outline)
  # -----------------------------------------------------------------
  Scenario Outline: Login fails when a required field is missing
    When I send a login request with the following body:
      | email   | password   |
      | <email> | <password> |
    Then the response status code should be 422

    Examples:
      | email                      | password    |
      |                            | password123 |
      | login-test@example.com     |             |

  # -----------------------------------------------------------------
  # Scenario 6: Edge case - empty strings
  # -----------------------------------------------------------------
  Scenario Outline: Login fails when fields contain empty strings
    When I send a login request with email "<email>" and password "<password>"
    Then the response status code should be 422

    Examples:
      | email                    | password    |
      |                          | password123 |
      | login-test@example.com   |             |
      |                          |             |

  # -----------------------------------------------------------------
  # Scenario 7: Edge case - SQL injection in email
  # -----------------------------------------------------------------
  Scenario Outline: Login rejects SQL injection attempts in the email field
    When I send a login request with email "<injection_email>" and password "password123"
    Then the response status code should be 422
    And the application should not expose any database error details

    Examples:
      | injection_email                            |
      | ' OR '1'='1                                |
      | admin@example.com'; DROP TABLE users; --   |
      | 1; DROP TABLE users --                     |
      | 1' UNION SELECT * FROM users --            |
