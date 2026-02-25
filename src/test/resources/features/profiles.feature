@profiles
Feature: User Profiles
  As a user of the RealWorld application
  I want to view profiles and follow/unfollow other users
  So that I can interact with the community

  Background:
    Given the API is available

  # --- Get Profile ---

  Scenario: Successfully get a user profile
    Given a user exists with username "profileuser" and email "profile@example.com"
    And I am authenticated as user "viewer@example.com" with username "viewer" and password "viewerpass"
    When I send an authenticated GET request to "/profiles/profileuser"
    Then the response status code should be 200
    And the response body should contain a "profile" wrapper object
    And the profile object should have field "username" with value "profileuser"
    And the profile object should have field "bio" of type "string"
    And the profile object should have field "image" of type "string"
    And the profile object should have field "following" of type "boolean"

  Scenario: Get profile response matches RealWorld API spec structure
    Given a user exists with username "specprofile" and email "specprofile@example.com"
    And I am authenticated as user "specviewer@example.com" with username "specviewer" and password "viewerpass"
    When I send an authenticated GET request to "/profiles/specprofile"
    Then the response status code should be 200
    And the response body should only contain these profile fields: "username,bio,image,following"

  Scenario: Get profile of non-existent user returns 404
    And I am authenticated as user "searcher@example.com" with username "searcher" and password "searcherpass"
    When I send an authenticated GET request to "/profiles/nonexistentuser"
    Then the response status code should be 404

  # --- Follow User ---

  Scenario: Successfully follow a user
    Given a user exists with username "followtarget" and email "followtarget@example.com"
    And I am authenticated as user "follower@example.com" with username "follower" and password "followerpass"
    When I send an authenticated POST request to "/profiles/followtarget/follow"
    Then the response status code should be 200
    And the response body should contain a "profile" wrapper object
    And the profile object should have field "username" with value "followtarget"
    And the profile object should have field "following" with boolean value true

  Scenario: Follow response matches RealWorld API spec structure
    Given a user exists with username "specfollow" and email "specfollow@example.com"
    And I am authenticated as user "specfollower@example.com" with username "specfollower" and password "followerpass"
    When I send an authenticated POST request to "/profiles/specfollow/follow"
    Then the response status code should be 200
    And the response body should only contain these profile fields: "username,bio,image,following"

  # --- Unfollow User ---

  Scenario: Successfully unfollow a user
    Given a user exists with username "unfollowtarget" and email "unfollowtarget@example.com"
    And I am authenticated as user "unfollower@example.com" with username "unfollower" and password "unfollowerpass"
    And user "unfollower" is following user "unfollowtarget"
    When I send an authenticated DELETE request to "/profiles/unfollowtarget/follow"
    Then the response status code should be 200
    And the response body should contain a "profile" wrapper object
    And the profile object should have field "username" with value "unfollowtarget"
    And the profile object should have field "following" with boolean value false

  Scenario: Unfollow response matches RealWorld API spec structure
    Given a user exists with username "specunfollow" and email "specunfollow@example.com"
    And I am authenticated as user "specunfollower@example.com" with username "specunfollower" and password "unfollowerpass"
    And user "specunfollower" is following user "specunfollow"
    When I send an authenticated DELETE request to "/profiles/specunfollow/follow"
    Then the response status code should be 200
    And the response body should only contain these profile fields: "username,bio,image,following"

  # --- Edge Cases ---

  Scenario: Follow a non-existent user returns 404
    And I am authenticated as user "eagerfollower@example.com" with username "eagerfollower" and password "eagerpass"
    When I send an authenticated POST request to "/profiles/nonexistentuser/follow"
    Then the response status code should be 404

  Scenario: Get profile without authentication still works
    Given a user exists with username "publicprofile" and email "publicprofile@example.com"
    When I send a GET request to "/profiles/publicprofile" without authentication
    Then the response status code should be 200
    And the response body should contain a "profile" wrapper object
    And the profile object should have field "username" with value "publicprofile"
    And the profile object should have field "following" with boolean value false
