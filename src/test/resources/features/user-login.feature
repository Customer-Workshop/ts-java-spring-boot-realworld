@users @login
Feature: User Login
  As a registered user of the RealWorld application
  I want to log in with my credentials
  So that I can access authenticated features

  Background:
    Given the API is available

  # --- Happy Path ---

  Scenario: Successfully login with valid credentials
    Given a registered user exists with email "login@example.com", username "loginuser", and password "correctpass"
    And I have a login payload with email "login@example.com" and password "correctpass"
    When I send a POST request to "/users/login"
    Then the response status code should be 200
    And the response body should contain a "user" wrapper object
    And the user object should have field "email" with value "login@example.com"
    And the user object should have field "username" with value "loginuser"
    And the user object should have a non-empty "token" field
    And the user object should have field "bio" of type "string"
    And the user object should have field "image" of type "string"

  Scenario: Login response has correct JSON structure matching RealWorld API spec
    Given a registered user exists with email "speclogin@example.com", username "specloginuser", and password "pass123"
    And I have a login payload with email "speclogin@example.com" and password "pass123"
    When I send a POST request to "/users/login"
    Then the response status code should be 200
    And the response body should only contain these user fields: "email,username,bio,image,token"

  # --- Invalid Credentials ---

  Scenario: Login fails with invalid password
    Given a registered user exists with email "wrongpass@example.com", username "wrongpassuser", and password "realpassword"
    And I have a login payload with email "wrongpass@example.com" and password "wrongpassword"
    When I send a POST request to "/users/login"
    Then the response status code should be 422
    And the response should contain authentication error message

  Scenario: Login fails with non-existent email
    Given I have a login payload with email "nonexistent@example.com" and password "anypassword"
    When I send a POST request to "/users/login"
    Then the response status code should be 422
    And the response should contain authentication error message

  # --- Edge Cases ---

  Scenario: Login fails with empty email
    Given I have a login payload with email "" and password "somepassword"
    When I send a POST request to "/users/login"
    Then the response status code should be 422

  Scenario: Login fails with empty password
    Given I have a login payload with email "user@example.com" and password ""
    When I send a POST request to "/users/login"
    Then the response status code should be 422

  Scenario: Login with SQL injection attempt in email
    Given I have a login payload with email "' OR '1'='1" and password "password"
    When I send a POST request to "/users/login"
    Then the response status code should be 422
    And the API should remain operational
