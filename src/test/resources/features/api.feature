Feature: RealWorld API
  As a user of the RealWorld API
  I want to be able to interact with the API
  So that I can use the application

  Scenario: Register a new user
    Given I have user registration data with username "cucumberuser1" email "cucumber1@test.com" and password "password123"
    When I send a POST request to "/users" with the registration data
    Then the response status code should be 201
    And the response should contain a user with email "cucumber1@test.com"
    And the response should contain a JWT token

  Scenario: Register with duplicate email should fail
    Given a user exists with email "duplicate@test.com"
    And I have user registration data with username "anotheruser" email "duplicate@test.com" and password "password123"
    When I send a POST request to "/users" with the registration data
    Then the response status code should be 422

  Scenario: Login with valid credentials
    Given a user exists with email "loginuser@test.com" and password "password123"
    When I send a POST request to "/users/login" with email "loginuser@test.com" and password "password123"
    Then the response status code should be 200
    And the response should contain a user with email "loginuser@test.com"
    And the response should contain a JWT token

  Scenario: Login with invalid password should fail
    Given a user exists with email "badlogin@test.com" and password "password123"
    When I send a POST request to "/users/login" with email "badlogin@test.com" and password "wrongpassword"
    Then the response status code should be 422

  Scenario: Get current user when authenticated
    Given I am logged in as a user with email "authuser@test.com"
    When I send a GET request to "/user" with authentication
    Then the response status code should be 200
    And the response should contain a user with email "authuser@test.com"

  Scenario: Get current user without authentication should fail
    When I send a GET request to "/user" without authentication
    Then the response status code should be 401

  Scenario: Get tags
    Given some articles with tags exist in the system
    When I send a GET request to "/tags" without authentication
    Then the response status code should be 200
    And the response should contain a list of tags

  Scenario: Get articles without authentication
    Given some articles exist in the system
    When I send a GET request to "/articles" without authentication
    Then the response status code should be 200
    And the response should contain a list of articles

  Scenario: Create article without authentication should fail
    Given I have article data with title "Unauthorized Article" description "Test" and body "Test"
    When I send a POST request to "/articles" with the article data without authentication
    Then the response status code should be 401

  Scenario: Get user feed without authentication should fail
    When I send a GET request to "/articles/feed" without authentication
    Then the response status code should be 401

  Scenario: Follow user without authentication should fail
    Given a user exists with username "targetuser"
    When I send a POST request to "/profiles/targetuser/follow" without authentication
    Then the response status code should be 401

  Scenario: Favorite article without authentication should fail
    Given an article exists with slug "favorite-test-article"
    When I send a POST request to "/articles/favorite-test-article/favorite" without authentication
    Then the response status code should be 401
