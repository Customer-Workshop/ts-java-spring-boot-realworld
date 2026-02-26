# Business Requirements Document (BRD) - Use Cases

**Application:** RealWorld Conduit (Blogging Platform)
**Date:** 2026-02-26

---

## Module 1: User Registration

### UC-1.1: Register a New Account

**User:** Unregistered visitor (prospective blogger)

**Goal:** Create a new account on the platform so they can publish articles, comment, and interact with other users.

**Pre-conditions:**
- The visitor does not already have an account.
- The visitor has a valid email address.

**Main Flow:**
1. The visitor submits their desired email, username, and password.
2. The system validates that none of the fields are blank.
3. The system validates that the email is in a proper email format.
4. The system checks that no existing account uses the same email.
5. The system checks that no existing account uses the same username.
6. The system hashes the password using BCrypt encryption.
7. The system assigns a unique ID (UUID) and a default profile image.
8. The system saves the new user record to the database.
9. The system generates a JWT authentication token.
10. The system returns the user profile along with the token.

**Exceptions:**
- **E1 - Blank fields:** If email, username, or password is blank, the system rejects the request with an error: "can't be empty."
- **E2 - Invalid email format:** If the email is not a valid format, the system rejects with: "should be an email."
- **E3 - Duplicate email:** If the email is already registered, the system rejects with: "duplicated email."
- **E4 - Duplicate username:** If the username is already taken, the system rejects with: "duplicated username."

---

## Module 2: User Authentication

### UC-2.1: Log In to an Existing Account

**User:** Registered blogger

**Goal:** Authenticate into the platform to access their account and perform actions that require login.

**Pre-conditions:**
- The blogger has a previously registered account with a valid email and password.

**Main Flow:**
1. The blogger submits their email and password.
2. The system validates that neither field is blank.
3. The system looks up the account by email.
4. The system compares the submitted password against the stored BCrypt hash.
5. If the credentials match, the system generates a JWT authentication token.
6. The system returns the user profile along with the token.

**Exceptions:**
- **E1 - Blank fields:** If email or password is blank, the system rejects the request with: "can't be empty."
- **E2 - Invalid email format:** If the email is not in a valid format, the system rejects with: "should be an email."
- **E3 - Wrong credentials:** If the email does not exist or the password does not match, the system rejects the request with an authentication error (HTTP 422).

---

## Module 3: User Profile Management

### UC-3.1: View Own Profile

**User:** Authenticated blogger

**Goal:** View their own current account information (email, username, bio, image).

**Pre-conditions:**
- The blogger is logged in with a valid JWT token.

**Main Flow:**
1. The blogger requests their current profile.
2. The system extracts the user identity from the JWT token.
3. The system retrieves the user's profile data from the database.
4. The system returns the profile along with the current token.

**Exceptions:**
- **E1 - Invalid or missing token:** The system returns a 401 Unauthorized error.

### UC-3.2: Update Own Profile

**User:** Authenticated blogger

**Goal:** Update their profile details such as email, username, password, bio, or profile image.

**Pre-conditions:**
- The blogger is logged in with a valid JWT token.

**Main Flow:**
1. The blogger submits the fields they want to update (any combination of email, username, password, bio, image).
2. The system validates the submitted data.
3. The system checks that the new email (if changed) is not already used by another user.
4. The system checks that the new username (if changed) is not already used by another user.
5. The system applies only the fields that were provided (partial update).
6. The system saves the updated profile to the database.
7. The system returns the updated profile.

**Exceptions:**
- **E1 - Invalid email format:** If a new email is provided but is not valid, the system rejects with: "should be an email."
- **E2 - Duplicate email:** If the new email belongs to another user, the system rejects with: "email already exist."
- **E3 - Duplicate username:** If the new username belongs to another user, the system rejects with: "username already exist."

### UC-3.3: View Another User's Profile

**User:** Any visitor (authenticated or anonymous)

**Goal:** View the public profile of another user on the platform.

**Pre-conditions:**
- The target user exists on the platform.

**Main Flow:**
1. The visitor requests a profile by username.
2. The system looks up the user by username.
3. The system constructs the profile (username, bio, image).
4. If the visitor is logged in, the system also determines whether they are following this user.
5. The system returns the profile data.

**Exceptions:**
- **E1 - User not found:** If no user exists with the given username, the system returns a 404 Not Found error.

---

## Module 4: Social - Follow / Unfollow

### UC-4.1: Follow Another Blogger

**User:** Authenticated blogger

**Goal:** Follow another blogger so their articles appear in the personalized feed.

**Pre-conditions:**
- The blogger is logged in.
- The target blogger exists on the platform.

**Main Flow:**
1. The blogger requests to follow a user by their username.
2. The system looks up the target user.
3. The system creates a follow relationship from the current user to the target user.
4. The system returns the target user's profile with "following" set to true.

**Exceptions:**
- **E1 - Target user not found:** The system returns a 404 Not Found error.
- **E2 - Not authenticated:** The system returns a 401 Unauthorized error.

### UC-4.2: Unfollow a Blogger

**User:** Authenticated blogger

**Goal:** Stop following a blogger so their articles no longer appear in the personalized feed.

**Pre-conditions:**
- The blogger is logged in.
- The blogger is currently following the target user.

**Main Flow:**
1. The blogger requests to unfollow a user by their username.
2. The system looks up the target user.
3. The system finds the existing follow relationship.
4. The system removes the follow relationship.
5. The system returns the target user's profile with "following" set to false.

**Exceptions:**
- **E1 - Target user not found:** The system returns a 404 Not Found error.
- **E2 - Follow relationship not found:** If the user was not following the target, the system returns a 404 Not Found error.

---

## Module 5: Article Publishing

### UC-5.1: Create a New Article

**User:** Authenticated blogger

**Goal:** Write and publish a new article on the platform.

**Pre-conditions:**
- The blogger is logged in.

**Main Flow:**
1. The blogger submits a title, description, body, and optionally a list of tags.
2. The system validates that title, description, and body are not blank.
3. The system converts the title into a URL-friendly slug (lowercase, special characters replaced with hyphens).
4. The system checks that no other article already exists with the same slug (i.e., no duplicate titles).
5. The system de-duplicates the tag list.
6. The system assigns a unique ID (UUID) and sets the created/updated timestamps.
7. The system saves the article and its tag associations to the database.
8. The system returns the newly created article data.

**Exceptions:**
- **E1 - Blank required fields:** If title, description, or body is blank, the system rejects with: "can't be empty."
- **E2 - Duplicate article title:** If an article with the same slug already exists, the system rejects with: "article name exists."
- **E3 - Not authenticated:** The system returns a 401 Unauthorized error.

### UC-5.2: Update an Existing Article

**User:** Authenticated blogger (article author)

**Goal:** Edit and update a previously published article.

**Pre-conditions:**
- The blogger is logged in.
- The blogger is the original author of the article.

**Main Flow:**
1. The blogger submits updated fields (any combination of title, description, body) for a specific article identified by its slug.
2. The system looks up the article by slug.
3. The system verifies that the logged-in user is the article's author.
4. The system applies only the fields that were provided (partial update).
5. If the title is changed, the system regenerates the slug.
6. The system updates the "updated at" timestamp.
7. The system saves the changes to the database.
8. The system returns the updated article data.

**Exceptions:**
- **E1 - Article not found:** If no article matches the slug, the system returns a 404 Not Found error.
- **E2 - Not the author:** If the logged-in user is not the article's author, the system returns a 403 Forbidden error.
- **E3 - Not authenticated:** The system returns a 401 Unauthorized error.

### UC-5.3: Delete an Article

**User:** Authenticated blogger (article author)

**Goal:** Remove a previously published article from the platform.

**Pre-conditions:**
- The blogger is logged in.
- The blogger is the original author of the article.

**Main Flow:**
1. The blogger requests deletion of an article by its slug.
2. The system looks up the article by slug.
3. The system verifies that the logged-in user is the article's author.
4. The system removes the article from the database.
5. The system confirms successful deletion.

**Exceptions:**
- **E1 - Article not found:** The system returns a 404 Not Found error.
- **E2 - Not the author:** The system returns a 403 Forbidden error.
- **E3 - Not authenticated:** The system returns a 401 Unauthorized error.

---

## Module 6: Article Discovery & Feed

### UC-6.1: Browse All Articles

**User:** Any visitor (authenticated or anonymous)

**Goal:** Browse and discover articles published on the platform.

**Pre-conditions:**
- None (publicly accessible).

**Main Flow:**
1. The visitor requests the article list, optionally filtering by tag, author, or favorited-by user.
2. The visitor may specify pagination parameters (offset and limit, defaulting to 0 and 20).
3. The system queries the database with the applied filters and pagination.
4. If the visitor is logged in, the system enriches each article with: whether the visitor has favorited it, and whether they follow the author.
5. The system returns the list of articles and the total count.

**Exceptions:**
- **E1 - No articles found:** The system returns an empty list with a count of zero.

### UC-6.2: View Personalized Feed

**User:** Authenticated blogger

**Goal:** See a feed of articles written only by bloggers they follow.

**Pre-conditions:**
- The blogger is logged in.
- The blogger follows at least one other user.

**Main Flow:**
1. The blogger requests their personalized feed with optional pagination parameters.
2. The system retrieves the list of users the blogger follows.
3. The system queries articles authored by those followed users.
4. The system enriches the articles with favorite and follow status.
5. The system returns the feed articles and the total count.

**Exceptions:**
- **E1 - Not following anyone:** The system returns an empty feed with a count of zero.
- **E2 - Not authenticated:** The system returns a 401 Unauthorized error.

### UC-6.3: View a Single Article

**User:** Any visitor (authenticated or anonymous)

**Goal:** Read the full content of a specific article.

**Pre-conditions:**
- The article exists on the platform.

**Main Flow:**
1. The visitor requests an article by its slug.
2. The system looks up the article.
3. If the visitor is logged in, the system enriches the response with favorite and author-follow status.
4. The system returns the full article data including title, body, description, tags, author profile, timestamps, and favorites count.

**Exceptions:**
- **E1 - Article not found:** The system returns a 404 Not Found error.

---

## Module 7: Article Favorites

### UC-7.1: Favorite an Article

**User:** Authenticated blogger

**Goal:** Mark an article as a favorite to show appreciation and save it for later.

**Pre-conditions:**
- The blogger is logged in.
- The article exists on the platform.

**Main Flow:**
1. The blogger requests to favorite an article by its slug.
2. The system looks up the article.
3. The system creates a favorite relationship between the blogger and the article.
4. The system returns the article data with the updated favorites count and "favorited" set to true.

**Exceptions:**
- **E1 - Article not found:** The system returns a 404 Not Found error.
- **E2 - Not authenticated:** The system returns a 401 Unauthorized error.

### UC-7.2: Unfavorite an Article

**User:** Authenticated blogger

**Goal:** Remove a previously favorited article from their favorites.

**Pre-conditions:**
- The blogger is logged in.
- The article exists on the platform.

**Main Flow:**
1. The blogger requests to unfavorite an article by its slug.
2. The system looks up the article.
3. The system finds and removes the favorite relationship (if it exists).
4. The system returns the article data with the updated favorites count and "favorited" set to false.

**Exceptions:**
- **E1 - Article not found:** The system returns a 404 Not Found error.
- **E2 - Not authenticated:** The system returns a 401 Unauthorized error.

---

## Module 8: Article Comments

### UC-8.1: Add a Comment to an Article

**User:** Authenticated blogger

**Goal:** Leave a comment on an article to engage in discussion.

**Pre-conditions:**
- The blogger is logged in.
- The article exists on the platform.

**Main Flow:**
1. The blogger submits a comment body for a specific article (identified by slug).
2. The system validates that the comment body is not blank.
3. The system looks up the article by slug.
4. The system creates the comment, linking it to both the article and the blogger.
5. The system assigns a unique ID (UUID) and sets the created timestamp.
6. The system saves the comment to the database.
7. The system returns the newly created comment with the author's profile.

**Exceptions:**
- **E1 - Blank comment body:** The system rejects with: "can't be empty."
- **E2 - Article not found:** The system returns a 404 Not Found error.
- **E3 - Not authenticated:** The system returns a 401 Unauthorized error.

### UC-8.2: View Comments on an Article

**User:** Any visitor (authenticated or anonymous)

**Goal:** Read all comments posted on a specific article.

**Pre-conditions:**
- The article exists on the platform.

**Main Flow:**
1. The visitor requests comments for an article by its slug.
2. The system looks up the article by slug.
3. The system retrieves all comments associated with the article.
4. The system returns the list of comments, each including the commenter's profile.

**Exceptions:**
- **E1 - Article not found:** The system returns a 404 Not Found error.

### UC-8.3: Delete a Comment

**User:** Authenticated blogger (comment author or article author)

**Goal:** Remove an inappropriate or unwanted comment from an article.

**Pre-conditions:**
- The blogger is logged in.
- The blogger is either the author of the comment or the author of the article the comment belongs to.

**Main Flow:**
1. The blogger requests deletion of a specific comment (by comment ID) on a specific article (by slug).
2. The system looks up the article by slug.
3. The system looks up the comment by ID within that article.
4. The system verifies the blogger is authorized (either the comment author or the article author).
5. The system removes the comment from the database.
6. The system confirms successful deletion.

**Exceptions:**
- **E1 - Article not found:** The system returns a 404 Not Found error.
- **E2 - Comment not found:** The system returns a 404 Not Found error.
- **E3 - Not authorized:** If the blogger is neither the comment author nor the article author, the system returns a 403 Forbidden error.
- **E4 - Not authenticated:** The system returns a 401 Unauthorized error.

---

## Module 9: Tags

### UC-9.1: Browse Available Tags

**User:** Any visitor (authenticated or anonymous)

**Goal:** View all available tags to discover articles by topic.

**Pre-conditions:**
- None (publicly accessible).

**Main Flow:**
1. The visitor requests the list of all tags.
2. The system retrieves all tag names from the database.
3. The system returns the complete list of tags.

**Exceptions:**
- **E1 - No tags exist:** The system returns an empty list.
