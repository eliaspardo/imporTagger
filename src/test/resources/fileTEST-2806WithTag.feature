Feature: XRay Importer Test
  @run @TEST_TEST-2806
  Scenario: TEST-2806
    Given path "https://google.com"
    When method GET
    Then status 200