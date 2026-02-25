Feature: Comments on Articles
  As an authenticated user
  I want to add and manage comments on articles
  So that I can engage in discussions

  Background:
    Given a registered user exists with username "testuser" and email "testuser@example.com"
    And the user has a valid authentication token
    And an article exists with title "Commented Article" by user "testuser"

  # --- Add Comment ---

  Scenario: Add a comment to an article
    When the user adds a comment to article "commented-article" with body "Great article!"
    Then the response status code should be 201
    And the response should contain a "comment" wrapper object
    And the comment body should be "Great article!"
    And the comment should have a valid ISO 8601 createdAt date
    And the comment should have a valid ISO 8601 updatedAt date
    And the comment author username should be "testuser"

  Scenario: Add multiple comments to an article
    When the user adds a comment to article "commented-article" with body "First comment"
    And the user adds a comment to article "commented-article" with body "Second comment"
    And the user requests comments for article "commented-article"
    Then the response status code should be 200
    And the response should contain a "comments" wrapper array
    And the comments list should contain at least 2 comments

  # --- Get Comments ---

  Scenario: Get all comments for an article
    Given the article "commented-article" has a comment "Existing comment" by "testuser"
    When the user requests comments for article "commented-article"
    Then the response status code should be 200
    And the response should contain a "comments" wrapper array

  # --- Delete Comment ---

  Scenario: Delete own comment
    Given the article "commented-article" has a comment "To be deleted" by "testuser"
    When the user deletes comment on article "commented-article"
    Then the response status code should be 204

  Scenario: Unauthorized user cannot delete another user's comment
    Given a different user "othercommenter" is authenticated
    And the article "commented-article" has a comment "Protected comment" by "testuser"
    When the other user deletes comment on article "commented-article"
    Then the response status code should be 403
