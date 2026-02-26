Feature: Edge Cases
  As a developer
  I want to ensure the API handles edge cases correctly
  So that the application is robust and reliable

  Background:
    Given a registered user exists with username "testuser" and email "testuser@example.com"
    And the user has a valid authentication token

  # --- Special Characters in Titles (Slug Encoding) ---

  Scenario: Title with special characters generates valid slug
    When the user creates an article with:
      | title       | Hello World & Goodbye, World. |
      | description | Language updates               |
      | body        | Details about updates          |
      | tagList     | java                           |
    Then the response status code should be 200
    And the article slug should be "hello-world-goodbye-world-"

  Scenario: Title with unicode characters generates valid slug
    When the user creates an article with:
      | title       | Coding Tips |
      | description | Tips and tricks             |
      | body        | Here are some tips          |
    Then the response status code should be 200
    And the article slug should be "coding-tips"

  Scenario: Title with multiple spaces generates clean slug
    When the user creates an article with:
      | title       | Too   Many    Spaces   Here |
      | description | Spacing test                 |
      | body        | Content                      |
    Then the response status code should be 200
    And the article slug should use hyphens as separators

  # --- Empty Tag Lists ---

  Scenario: Create article with explicitly empty tag list
    When the user creates an article with empty tag list:
      | title       | No Tags Article |
      | description | No tags here    |
      | body        | Content body    |
    Then the response status code should be 200
    And the article tagList should be empty

  Scenario: Article response always includes tagList array even when empty
    When the user creates an article with:
      | title       | Always Has TagList |
      | description | Even if empty       |
      | body        | Check the response  |
    Then the response status code should be 200
    And the article tagList should be an array

  # --- Unauthorized Operations ---

  Scenario: Unauthenticated user cannot create an article
    When an unauthenticated user creates an article with:
      | title       | Unauthorized Article |
      | description | Should fail          |
      | body        | No access            |
    Then the response status code should be 401

  Scenario: Unauthenticated user cannot delete an article
    Given an article exists with title "Cannot Delete This" by user "testuser"
    When an unauthenticated user deletes the article with slug "cannot-delete-this"
    Then the response status code should be 401

  Scenario: Unauthenticated user cannot favorite an article
    Given an article exists with title "Cannot Favorite" by user "testuser"
    When an unauthenticated user favorites the article with slug "cannot-favorite"
    Then the response status code should be 401

  Scenario: Unauthenticated user cannot add a comment
    Given an article exists with title "Cannot Comment" by user "testuser"
    When an unauthenticated user adds a comment to article "cannot-comment"
    Then the response status code should be 401

  # --- Response Payload Validation ---

  Scenario: Article response matches RealWorld API spec structure
    When the user creates an article with:
      | title       | Spec Compliance Test |
      | description | Testing spec         |
      | body        | Full body content    |
      | tagList     | test                 |
    Then the response status code should be 200
    And the response should contain an "article" wrapper object
    And the article response should have fields: slug, title, description, body, tagList, createdAt, updatedAt, favorited, favoritesCount, author
    And the article author should have fields: username, bio, image, following

  Scenario: Date fields use ISO 8601 format with UTC timezone
    When the user creates an article with:
      | title       | Date Format Check |
      | description | Check dates       |
      | body        | Date content      |
    Then the response status code should be 200
    And the article should have a valid ISO 8601 createdAt date
    And the article should have a valid ISO 8601 updatedAt date
