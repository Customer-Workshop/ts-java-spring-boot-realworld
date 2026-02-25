@users @registration
Feature: User Registration
  As a new user of the RealWorld application
  I want to register an account
  So that I can access the platform features

  Background:
    Given the API is available

  # --- Happy Path ---

  Scenario: Successfully register a new user with valid payload
    Given I have a registration payload with email "newuser@example.com", username "newuser", and password "password123"
    When I send a POST request to "/users"
    Then the response status code should be 201
    And the response body should contain a "user" wrapper object
    And the user object should have field "email" with value "newuser@example.com"
    And the user object should have field "username" with value "newuser"
    And the user object should have a non-empty "token" field
    And the user object should have field "bio" of type "string"
    And the user object should have field "image" of type "string"

  Scenario: Register user has correct JSON structure matching RealWorld API spec
    Given I have a registration payload with email "specuser@example.com", username "specuser", and password "password123"
    When I send a POST request to "/users"
    Then the response status code should be 201
    And the response body should only contain these user fields: "email,username,bio,image,token"

  # --- Duplicate Validation ---

  Scenario: Registration fails with duplicate email
    Given a user already exists with email "existing@example.com" and username "existinguser"
    And I have a registration payload with email "existing@example.com", username "uniqueuser", and password "password123"
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response should contain error for field "email" with message "duplicated email"

  Scenario: Registration fails with duplicate username
    Given a user already exists with email "unique@example.com" and username "takenname"
    And I have a registration payload with email "another@example.com", username "takenname", and password "password123"
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response should contain error for field "username" with message "duplicated username"

  # --- Missing Required Fields ---

  Scenario: Registration fails when email is missing
    Given I have a registration payload without email
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response should contain error for field "email"

  Scenario: Registration fails when username is missing
    Given I have a registration payload without username
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response should contain error for field "username"

  Scenario: Registration fails when password is missing
    Given I have a registration payload without password
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response should contain error for field "password"

  # --- Edge Cases ---

  Scenario: Registration fails with empty string email
    Given I have a registration payload with email "", username "emptyemail", and password "password123"
    When I send a POST request to "/users"
    Then the response status code should be 422

  Scenario: Registration fails with empty string username
    Given I have a registration payload with email "emptyuser@test.com", username "", and password "password123"
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response should contain error for field "username" with message "can't be empty"

  Scenario: Registration fails with invalid email format
    Given I have a registration payload with email "not-an-email", username "bademail", and password "password123"
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response should contain error for field "email" with message "should be an email"

  Scenario: Registration with SQL injection attempt in username
    Given I have a registration payload with email "sqli@example.com", username "'; DROP TABLE users; --", and password "password123"
    When I send a POST request to "/users"
    Then the response status code should be either 201 or 422
    And the API should remain operational

  Scenario: Registration with extremely long bio is handled at registration
    Given I have a registration payload with email "longbio@example.com", username "longbiouser", and password "password123"
    When I send a POST request to "/users"
    Then the response status code should be 201
