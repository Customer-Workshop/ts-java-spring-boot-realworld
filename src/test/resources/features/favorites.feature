Feature: Article Favorites
  As an authenticated user
  I want to favorite and unfavorite articles
  So that I can save articles I enjoy

  Background:
    Given a registered user exists with username "testuser" and email "testuser@example.com"
    And the user has a valid authentication token

  # --- Favorite Article ---

  Scenario: Favorite an article
    Given an article exists with title "Likeable Article" by user "author1"
    When the user favorites the article with slug "likeable-article"
    Then the response status code should be 200
    And the response should contain an "article" wrapper object
    And the article favorited should be true
    And the article favoritesCount should be 1

  Scenario: Favorite count increments correctly
    Given an article exists with title "Popular Article" by user "author1"
    And the article "popular-article" already has 5 favorites
    When the user favorites the article with slug "popular-article"
    Then the response status code should be 200
    And the article favoritesCount should be 6

  # --- Unfavorite Article ---

  Scenario: Unfavorite a previously favorited article
    Given an article exists with title "Was Liked" by user "author1"
    And the user has already favorited article "was-liked"
    When the user unfavorites the article with slug "was-liked"
    Then the response status code should be 200
    And the article favorited should be false
    And the article favoritesCount should be 0

  Scenario: Favorited flag in article response for authenticated user
    Given an article exists with title "Check Favorited" by user "author1"
    And the user has already favorited article "check-favorited"
    When the user requests the article with slug "check-favorited"
    Then the response status code should be 200
    And the article favorited should be true

  Scenario: Favorited flag is false for non-favorited article
    Given an article exists with title "Not Favorited" by user "author1"
    When the user requests the article with slug "not-favorited"
    Then the response status code should be 200
    And the article favorited should be false
    And the article favoritesCount should be 0
