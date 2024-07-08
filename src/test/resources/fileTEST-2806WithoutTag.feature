Feature: XRay Importer Test
  Scenario: TEST-2806
    Given path "https://google.com"
    When method GET
    Then status 200