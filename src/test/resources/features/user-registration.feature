Feature: User Registration API
  As a new user of the RealWorld application
  I want to register an account via the POST /users endpoint
  So that I can access authenticated features of the platform

  Background:
    Given the application is running

  # ---------------------------------------------------------------
  # Scenario 1: Valid registration
  # ---------------------------------------------------------------
  Scenario: Successfully register a new user with valid credentials
    Given I prepare a registration request with username "newuser", email "newuser@example.com", and password "securePass123"
    When I send a POST request to "/users"
    Then the response status code should be 201
    And the response body should contain a "user" object
    And the "user" object should have the following fields:
      | field    | type   |
      | email    | string |
      | token    | string |
      | username | string |
      | bio      | string |
      | image    | string |
    And the "user.email" should be "newuser@example.com"
    And the "user.username" should be "newuser"
    And the "user.token" should not be empty

  # ---------------------------------------------------------------
  # Scenario 2: Duplicate email
  # ---------------------------------------------------------------
  Scenario: Registration fails when email is already taken
    Given a user already exists with username "existing", email "taken@example.com", and password "password123"
    And I prepare a registration request with username "anotheruser", email "taken@example.com", and password "password456"
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response body should contain error for field "email"

  # ---------------------------------------------------------------
  # Scenario 3: Duplicate username
  # ---------------------------------------------------------------
  Scenario: Registration fails when username is already taken
    Given a user already exists with username "takenname", email "first@example.com", and password "password123"
    And I prepare a registration request with username "takenname", email "second@example.com", and password "password456"
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response body should contain error for field "username"

  # ---------------------------------------------------------------
  # Scenario 4: Missing required fields (using Scenario Outline)
  # ---------------------------------------------------------------
  Scenario Outline: Registration fails when a required field is missing
    Given I prepare a registration request with <field> omitted
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response body should contain error for field "<field>"

    Examples:
      | field    |
      | email    |
      | username |
      | password |

  # ---------------------------------------------------------------
  # Scenario 5: Edge case - empty strings
  # ---------------------------------------------------------------
  Scenario Outline: Registration fails when a field is an empty string
    Given I prepare a registration request with <field> set to ""
    When I send a POST request to "/users"
    Then the response status code should be 422
    And the response body should contain error for field "<field>"

    Examples:
      | field    |
      | email    |
      | username |
      | password |

  # ---------------------------------------------------------------
  # Scenario 6: Edge case - SQL injection in username
  # ---------------------------------------------------------------
  Scenario: Registration handles SQL injection attempt in username safely
    Given I prepare a registration request with username "'; DROP TABLE users; --", email "sqlinject@example.com", and password "securePass123"
    When I send a POST request to "/users"
    Then the response status code should be one of 201 or 422
    And the application should not have a database error

  # ---------------------------------------------------------------
  # Scenario 7: Edge case - extremely long username
  # ---------------------------------------------------------------
  Scenario: Registration handles an extremely long username
    Given I prepare a registration request with a username of 500 characters, email "longuser@example.com", and password "securePass123"
    When I send a POST request to "/users"
    Then the response status code should be one of 201 or 422
    And the response should be valid JSON
