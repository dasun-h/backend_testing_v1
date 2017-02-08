Feature: Sample

  @scenario1
  Scenario: Yahoo Search
    Given I visit the yahoo home page
    And I verify yahoo home page
    And I search "WWE" keyword using yahoo

  @scenario2
  Scenario: Verify Canopy Labs Drag and Drop
    Given I visit the canopy labs login page
    And I login to the canopy labs using "kohls" and "kohls123"
    Then I should navigate to the canopy labs home page
    When I navigate to the canopy labs funnel generator page
    And I drag and drop canopy labs sequence

  @scenario3
  Scenario: Verify Drag and Drop in heroku app
    Given I visit the drag and drop herokuapp app
    And I drag and drop heroku app elements

  @scenario4
  Scenario: Verify Drag and Drop in html5demo app
    Given I visit the drag and drop html5demo app
    And I drag and drop html5demo app elements