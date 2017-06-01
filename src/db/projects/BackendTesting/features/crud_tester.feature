Feature: CRUD Database Testing

  Background:
    Given I visit the crud site home page
    When I remove all the initial test data from database

  @scenario1
  Scenario: Verifying data Insert - Scenario 01
    And I navigate to the "add users" page
    And I "add" a record to my database
    Then I verify added record display in the home page
    When I retrieve added record details from database
    Then I verify added record values with backend record values

  @scenario2
  Scenario: Verifying data Update - Scenario 02
    And I navigate to the "add users" page
    And I "add" a record to my database
    And I navigate to the "update users" page
    And I "update" a record to my database
    Then I verify updated record display in the home page
    When I retrieve updated record details from database
    Then I verify updated record values with backend record values

  @scenario3
  Scenario: Verifying data Delete - Scenario 03
    And I navigate to the "add users" page
    And I "add" a record to my database
    And I navigate to the "delete users" page
    And I "delete" a record to my database
    Then I verify whether the record deleted from backend
