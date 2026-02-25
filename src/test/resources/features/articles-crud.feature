Feature: Articles CRUD Operations
  As an authenticated user
  I want to create, read, update, and delete articles
  So that I can manage my content on the platform

  Background:
    Given a registered user exists with username "testuser" and email "testuser@example.com"
    And the user has a valid authentication token

  # --- Create Article ---

  Scenario: Create an article with tags
    When the user creates an article with:
      | title       | How to Train Your Dragon |
      | description | Ever wonder how?         |
      | body        | You have to believe      |
      | tagList     | reactjs,angularjs,dragons |
    Then the response status code should be 200
    And the response should contain an "article" wrapper object
    And the article title should be "How to Train Your Dragon"
    And the article description should be "Ever wonder how?"
    And the article body should be "You have to believe"
    And the article slug should be "how-to-train-your-dragon"
    And the article tagList should contain "reactjs"
    And the article tagList should contain "angularjs"
    And the article tagList should contain "dragons"
    And the article should have a valid ISO 8601 createdAt date
    And the article should have a valid ISO 8601 updatedAt date
    And the article author username should be "testuser"
    And the article favorited should be false
    And the article favoritesCount should be 0

  Scenario: Create an article without tags
    When the user creates an article with:
      | title       | Minimal Article    |
      | description | A brief overview   |
      | body        | Just the essentials |
    Then the response status code should be 200
    And the response should contain an "article" wrapper object
    And the article title should be "Minimal Article"
    And the article tagList should be empty

  Scenario: Slug is generated from title
    When the user creates an article with:
      | title       | My Awesome Blog Post |
      | description | A great post         |
      | body        | Content here         |
    Then the response status code should be 200
    And the article slug should be "my-awesome-blog-post"

  # --- Read Article ---

  Scenario: Get a single article by slug
    Given an article exists with title "Existing Article" by user "testuser"
    When the user requests the article with slug "existing-article"
    Then the response status code should be 200
    And the response should contain an "article" wrapper object
    And the article title should be "Existing Article"

  # --- Update Article ---

  Scenario: Update article title
    Given an article exists with title "Original Title" by user "testuser"
    When the user updates the article "original-title" with:
      | title | Updated Title |
    Then the response status code should be 200
    And the article title should be "Updated Title"
    And the article slug should be "updated-title"

  Scenario: Update article body
    Given an article exists with title "Body Update Test" by user "testuser"
    When the user updates the article "body-update-test" with:
      | body | This is the new body content |
    Then the response status code should be 200
    And the article body should be "This is the new body content"

  Scenario: Update article description
    Given an article exists with title "Desc Update Test" by user "testuser"
    When the user updates the article "desc-update-test" with:
      | description | Updated description here |
    Then the response status code should be 200
    And the article description should be "Updated description here"

  Scenario: Update article with unauthorized user
    Given an article exists with title "Protected Article" by user "otheruser"
    And a different user "anotheruser" is authenticated
    When the other user updates the article "protected-article" with:
      | title | Hacked Title |
    Then the response status code should be 403

  # --- Delete Article ---

  Scenario: Delete own article
    Given an article exists with title "To Be Deleted" by user "testuser"
    When the user deletes the article with slug "to-be-deleted"
    Then the response status code should be 204

  Scenario: Unauthorized user cannot delete article
    Given an article exists with title "Someone Else Article" by user "otheruser"
    And a different user "anotheruser" is authenticated
    When the other user deletes the article with slug "someone-else-article"
    Then the response status code should be 403
