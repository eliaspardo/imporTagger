@xtadium
Feature: AppLayout

  Background: TEST-3436
    * url baseURL

  Scenario Outline: TEST-3470 Scenario Outline
    Given path "client/test/"+<layout>
    When method GET
    Then status <responseStatus>
    Examples:
      |layout|responseStatus|
      |defaultAppLayout|200|
      |1234567890|404|
      |""|404|
