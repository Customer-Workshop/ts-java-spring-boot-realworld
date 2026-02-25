Feature: Tags API
  As a user of the platform
  I want to retrieve all available tags
  So that I can browse content by topic

  Scenario: List all tags
    Given the following tags exist in the system:
      | java       |
      | spring     |
      | python     |
      | javascript |
    When the user requests the tags list
    Then the response status code should be 200
    And the response should contain a "tags" wrapper array
    And the tags list should contain "java"
    And the tags list should contain "spring"
    And the tags list should contain "python"
    And the tags list should contain "javascript"

  Scenario: Tags list is not empty when articles with tags exist
    Given an article exists with tags "cucumber,bdd,testing"
    When the user requests the tags list
    Then the response status code should be 200
    And the tags list should not be empty

  Scenario: Tags endpoint is accessible without authentication
    When an unauthenticated user requests the tags list
    Then the response status code should be 200
    And the response should contain a "tags" wrapper array
