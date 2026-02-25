@users @currentuser
Feature: Get and Update Current User
  As an authenticated user of the RealWorld application
  I want to retrieve and update my user profile
  So that I can manage my account information

  Background:
    Given the API is available
    And I am authenticated as user "currentuser@example.com" with username "currentuser" and password "mypassword"

  # --- Get Current User ---

  Scenario: Successfully get current user
    When I send an authenticated GET request to "/user"
    Then the response status code should be 200
    And the response body should contain a "user" wrapper object
    And the user object should have field "email" with value "currentuser@example.com"
    And the user object should have field "username" with value "currentuser"
    And the user object should have a non-empty "token" field

  Scenario: Get current user response matches RealWorld API spec structure
    When I send an authenticated GET request to "/user"
    Then the response status code should be 200
    And the response body should only contain these user fields: "email,username,bio,image,token"

  # --- Update Current User ---

  Scenario: Successfully update user bio
    Given I have an update user payload with bio "This is my updated bio"
    When I send an authenticated PUT request to "/user"
    Then the response status code should be 200
    And the response body should contain a "user" wrapper object
    And the user object should have field "bio" with value "This is my updated bio"

  Scenario: Successfully update user image
    Given I have an update user payload with image "https://example.com/new-avatar.png"
    When I send an authenticated PUT request to "/user"
    Then the response status code should be 200
    And the user object should have field "image" with value "https://example.com/new-avatar.png"

  Scenario: Successfully update user email
    Given I have an update user payload with email "newemail@example.com"
    When I send an authenticated PUT request to "/user"
    Then the response status code should be 200
    And the user object should have field "email" with value "newemail@example.com"

  Scenario: Update user response matches RealWorld API spec structure
    Given I have an update user payload with bio "spec check bio"
    When I send an authenticated PUT request to "/user"
    Then the response status code should be 200
    And the response body should only contain these user fields: "email,username,bio,image,token"

  # --- Edge Cases ---

  Scenario: Update user with extremely long bio
    Given I have an update user payload with a bio of 5000 characters
    When I send an authenticated PUT request to "/user"
    Then the response status code should be either 200 or 422

  Scenario: Update user with empty string bio
    Given I have an update user payload with bio ""
    When I send an authenticated PUT request to "/user"
    Then the response status code should be 200

  Scenario: Get current user without authentication token
    When I send a GET request to "/user" without authentication
    Then the response status code should be 401
