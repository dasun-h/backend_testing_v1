Feature: Mercury

  @scenario1
  Scenario: New User Registration
    Given I visit the mercury home page
    When I navigate to the user registration page
    And I registered as a new user
    Then I verify my registration success state
    And I logout from my account