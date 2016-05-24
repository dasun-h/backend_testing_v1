$(document).ready(function() {var formatter = new CucumberHTML.DOMFormatter($('.cucumber-report'));formatter.uri("src/db/projects/BackendTesting/features/crud_tester.feature");
formatter.feature({
  "line": 1,
  "name": "CRUD Database Testing",
  "description": "",
  "id": "crud-database-testing",
  "keyword": "Feature"
});
formatter.background({
  "line": 3,
  "name": "",
  "description": "",
  "type": "background",
  "keyword": "Background"
});
formatter.step({
  "line": 4,
  "name": "I visit the crud site home page",
  "keyword": "Given "
});
formatter.step({
  "line": 5,
  "name": "I remove all the initial test data from database",
  "keyword": "When "
});
formatter.match({
  "location": "DB_Tester.I_visit_the_crud_site_home_page()"
});
formatter.result({
  "duration": 1611301109,
  "status": "passed"
});
formatter.match({
  "location": "DB_Tester.I_remove_all_the_initial_test_data_from_database()"
});
formatter.result({
  "duration": 95724868,
  "status": "passed"
});
formatter.scenario({
  "line": 8,
  "name": "Verifying data Insert - Scenario 01",
  "description": "",
  "id": "crud-database-testing;verifying-data-insert---scenario-01",
  "type": "scenario",
  "keyword": "Scenario",
  "tags": [
    {
      "line": 7,
      "name": "@scenario1"
    }
  ]
});
formatter.step({
  "line": 9,
  "name": "I navigate to the \"add users\" page",
  "keyword": "And "
});
formatter.step({
  "line": 10,
  "name": "I \"add\" a record to my database",
  "keyword": "And "
});
formatter.step({
  "line": 11,
  "name": "I verify added record display in the home page",
  "keyword": "Then "
});
formatter.step({
  "line": 12,
  "name": "I retrieve added record details from database",
  "keyword": "When "
});
formatter.step({
  "line": 13,
  "name": "I verify added record values with backend record values",
  "keyword": "Then "
});
formatter.match({
  "arguments": [
    {
      "val": "add users",
      "offset": 19
    }
  ],
  "location": "DB_Tester.I_navigate_to_the_page(String)"
});
formatter.result({
  "duration": 2477974304,
  "status": "passed"
});
formatter.match({
  "arguments": [
    {
      "val": "add",
      "offset": 3
    }
  ],
  "location": "DB_Tester.I_a_record_to_my_database(String)"
});
formatter.result({
  "duration": 1708322625,
  "status": "passed"
});
formatter.match({
  "location": "DB_Tester.I_verify_added_record_display_in_the_home_page()"
});
formatter.result({
  "duration": 1301293184,
  "status": "passed"
});
formatter.match({
  "location": "DB_Tester.I_retrieve_added_record_details_from_database()"
});
formatter.result({
  "duration": 528012430,
  "status": "passed"
});
formatter.match({
  "location": "DB_Tester.I_verify_added_record_values_with_backend_record_values()"
});
formatter.result({
  "duration": 147540,
  "status": "passed"
});
});