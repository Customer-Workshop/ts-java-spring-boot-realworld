Feature: Article Listing and Feed
  As a user of the platform
  I want to browse and filter articles
  So that I can find content relevant to my interests

  Background:
    Given a registered user exists with username "testuser" and email "testuser@example.com"
    And the user has a valid authentication token

  # --- List Articles ---

  Scenario: List articles returns articles wrapper with correct structure
    Given the following articles exist:
      | title            | author    | tags           |
      | First Article    | testuser  | java,spring    |
      | Second Article   | testuser  | python,django  |
    When the user requests the articles list
    Then the response status code should be 200
    And the response should contain an "articles" wrapper array
    And the response should contain an "articlesCount" field

  Scenario: Filter articles by tag
    Given the following articles exist:
      | title            | author    | tags           |
      | Java Post        | author1   | java,spring    |
      | Python Post      | author2   | python,django  |
      | Another Java     | author3   | java,backend   |
    When the user requests articles filtered by tag "java"
    Then the response status code should be 200
    And all returned articles should contain the tag "java"

  Scenario: Filter articles by author
    Given the following articles exist:
      | title            | author    | tags          |
      | Post by Alice    | alice     | tech          |
      | Post by Bob      | bob       | science       |
      | Another by Alice | alice     | programming   |
    When the user requests articles filtered by author "alice"
    Then the response status code should be 200
    And all returned articles should have author "alice"

  Scenario: Filter articles by favorited user
    Given the following articles exist:
      | title             | author    | tags     |
      | Favorited Post    | author1   | tech     |
      | Not Favorited     | author2   | science  |
    And the user "testuser" has favorited "Favorited Post"
    When the user requests articles favorited by "testuser"
    Then the response status code should be 200
    And the returned articles should include "Favorited Post"

  # --- Pagination ---

  Scenario: Paginate articles with limit
    Given 25 articles exist in the system
    When the user requests articles with limit 10 and offset 0
    Then the response status code should be 200
    And the returned articles count should be at most 10

  Scenario: Paginate articles with offset
    Given 25 articles exist in the system
    When the user requests articles with limit 10 and offset 10
    Then the response status code should be 200
    And the returned articles count should be at most 10

  Scenario: Default pagination returns up to 20 articles
    Given 25 articles exist in the system
    When the user requests the articles list
    Then the response status code should be 200
    And the returned articles count should be at most 20

  # --- Authenticated Feed ---

  Scenario: Authenticated feed returns articles from followed users
    Given a user "followed_author" exists
    And the user "testuser" follows "followed_author"
    And "followed_author" has published articles
    When the user requests their feed
    Then the response status code should be 200
    And the response should contain an "articles" wrapper array
    And the response should contain an "articlesCount" field
    And the feed articles should be from followed users

  Scenario: Unauthenticated user cannot access feed
    When an unauthenticated user requests the feed
    Then the response status code should be 401
