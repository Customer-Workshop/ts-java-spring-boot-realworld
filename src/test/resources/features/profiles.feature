Feature: Profile API
  As an API consumer
  I want to view user profiles and manage follow relationships
  So that I can discover and connect with other users

  Background:
    Given the following users are registered:
      | username  | email               | password  |
      | johndoe   | johndoe@example.com | pass1234  |
      | janedoe   | janedoe@example.com | pass5678  |

  # ---------- GET /api/profiles/:username ----------

  Scenario: Get profile of an existing user while authenticated
    Given I am authenticated as "johndoe"
    When I request the profile of "janedoe"
    Then the response status should be 200
    And the response should contain a "profile" object
    And the profile should have the following fields:
      | field     | type    |
      | username  | string  |
      | bio       | string  |
      | image     | string  |
      | following | boolean |
    And the profile username should be "janedoe"

  Scenario: Get profile of an existing user without authentication
    When I request the profile of "janedoe" without authentication
    Then the response status should be 200
    And the response should contain a "profile" object
    And the profile "following" field should be false

  Scenario: Get profile of a non-existent user
    Given I am authenticated as "johndoe"
    When I request the profile of "nonexistentuser"
    Then the response status should be 404

  # ---------- POST /api/profiles/:username/follow ----------

  Scenario: Follow another user
    Given I am authenticated as "johndoe"
    When I follow the user "janedoe"
    Then the response status should be 200
    And the response should contain a "profile" object
    And the profile "following" field should be true
    And the profile username should be "janedoe"

  Scenario: Follow a user who is already followed is idempotent
    Given I am authenticated as "johndoe"
    And I have already followed the user "janedoe"
    When I follow the user "janedoe"
    Then the response status should be 200
    And the profile "following" field should be true

  # ---------- DELETE /api/profiles/:username/follow ----------

  Scenario: Unfollow a followed user
    Given I am authenticated as "johndoe"
    And I have already followed the user "janedoe"
    When I unfollow the user "janedoe"
    Then the response status should be 200
    And the response should contain a "profile" object
    And the profile "following" field should be false
    And the profile username should be "janedoe"

  Scenario: Unfollow a user who is not followed
    Given I am authenticated as "johndoe"
    When I unfollow the user "janedoe"
    Then the response status should be 404

  # ---------- Authentication required for follow/unfollow ----------

  Scenario: Follow a user without authentication returns 401
    When I follow the user "janedoe" without authentication
    Then the response status should be 401

  Scenario: Unfollow a user without authentication returns 401
    When I unfollow the user "janedoe" without authentication
    Then the response status should be 401

  # ---------- Edge case ----------

  Scenario: Follow yourself
    Given I am authenticated as "johndoe"
    When I follow the user "johndoe"
    Then the response status should be 200
    And the response should contain a "profile" object
    And the profile "following" field should be true
    And the profile username should be "johndoe"
