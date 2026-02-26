Feature: Current User API
  As an authenticated user of the RealWorld application
  I want to retrieve and update my profile information
  So that I can manage my account details

  The Current User API provides two endpoints:
    - GET /user   : Retrieve the currently authenticated user
    - PUT /user   : Update the currently authenticated user's profile

  All endpoints require a valid JWT token in the Authorization header
  using the format: "Token <jwt>"

  Background:
    Given a registered user exists with username "bdduser" and email "bdduser@test.com" and password "password123"
    And the user is logged in and has a valid auth token

  # ──────────────────────────────────────────────
  # Scenario 1: Get current user with valid token
  # ──────────────────────────────────────────────
  Scenario: Get current user with valid auth token
    When the client sends a GET request to "/user" with the auth token
    Then the response status code should be 200
    And the response should contain a "user" object
    And the user object "email" should match the registered email
    And the user object "username" should match the registered username
    And the user object should contain the field "token"
    And the user object should contain the field "bio"
    And the user object should contain the field "image"

  # ──────────────────────────────────────────────
  # Scenario 2: Get current user without auth
  # ──────────────────────────────────────────────
  Scenario: Get current user without auth token returns 401
    When the client sends a GET request to "/user" without an auth token
    Then the response status code should be 401

  # ──────────────────────────────────────────────
  # Scenario 3: Update bio
  # ──────────────────────────────────────────────
  Scenario: Update user bio with auth token
    When the client sends a PUT request to "/user" with the auth token and body:
      """
      {"user": {"bio": "My updated bio"}}
      """
    Then the response status code should be 200
    And the response should contain a "user" object
    And the user object should contain the field "bio" with value "My updated bio"

  # ──────────────────────────────────────────────
  # Scenario 4: Update image
  # ──────────────────────────────────────────────
  Scenario: Update user image URL with auth token
    When the client sends a PUT request to "/user" with the auth token and body:
      """
      {"user": {"image": "https://example.com/new-avatar.png"}}
      """
    Then the response status code should be 200
    And the response should contain a "user" object
    And the user object should contain the field "image" with value "https://example.com/new-avatar.png"

  # ──────────────────────────────────────────────
  # Scenario 5: Update email
  # ──────────────────────────────────────────────
  Scenario: Update user email with auth token
    When the client sends a PUT request to "/user" with the auth token and body:
      """
      {"user": {"email": "newemail@test.com"}}
      """
    Then the response status code should be 200
    And the response should contain a "user" object
    And the user object should contain the field "email" with value "newemail@test.com"

  # ──────────────────────────────────────────────
  # Scenario 6: Update multiple fields at once
  # ──────────────────────────────────────────────
  Scenario: Update multiple user fields simultaneously
    When the client sends a PUT request to "/user" with the auth token and body:
      """
      {"user": {"bio": "Multi-field bio", "image": "https://example.com/multi.png", "email": "multi@test.com"}}
      """
    Then the response status code should be 200
    And the response should contain a "user" object
    And the user object should contain the field "bio" with value "Multi-field bio"
    And the user object should contain the field "image" with value "https://example.com/multi.png"
    And the user object should contain the field "email" with value "multi@test.com"

  # ──────────────────────────────────────────────
  # Scenario 7: Edge case - extremely long bio
  # ──────────────────────────────────────────────
  Scenario: Update user bio with an extremely long string
    When the client sends a PUT request to "/user" with a bio of 10000 characters
    Then the response status code should be 200
    And the response should contain a "user" object
    And the user object "bio" field should have length 10000

  # ──────────────────────────────────────────────
  # Scenario 8: Edge case - empty string for bio
  # ──────────────────────────────────────────────
  Scenario: Update user bio with an empty string
    When the client sends a PUT request to "/user" with the auth token and body:
      """
      {"user": {"bio": ""}}
      """
    Then the response status code should be 200
    And the response should contain a "user" object
    And the user object should contain the field "bio"

  # ──────────────────────────────────────────────
  # Scenario 9: Edge case - empty string for image
  # ──────────────────────────────────────────────
  Scenario: Update user image with an empty string
    When the client sends a PUT request to "/user" with the auth token and body:
      """
      {"user": {"image": ""}}
      """
    Then the response status code should be 200
    And the response should contain a "user" object
    And the user object should contain the field "image"

  # ──────────────────────────────────────────────
  # Scenario 10: Edge case - invalid email format
  # ──────────────────────────────────────────────
  Scenario: Update user email with an invalid email format
    When the client sends a PUT request to "/user" with the auth token and body:
      """
      {"user": {"email": "not-an-email"}}
      """
    Then the response status code should be 422

  # ──────────────────────────────────────────────
  # Scenario 11: Update with invalid/expired token
  # ──────────────────────────────────────────────
  Scenario: Update user profile with an invalid token returns 401
    When the client sends a PUT request to "/user" with an invalid auth token and body:
      """
      {"user": {"bio": "Should not update"}}
      """
    Then the response status code should be 401
