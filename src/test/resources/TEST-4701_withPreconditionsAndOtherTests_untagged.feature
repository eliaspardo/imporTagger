@stream @env=DEV,TEST @parallel=false
Feature: Dynamic VT Provider Creation

  Background: TEST-4705
    * url karate.get('baseURL')
    * configure headers = {Authorization: '#("Bearer "+karate.get("token"))'}

  Scenario: TEST-4701
    # Create DVTP Overwriting Tracking ID
    Given path "/api/test/"+appID+"/appDynamicProvider"
    * def randomName = "API_Automation_" + +Util.getTimestamp()


  Scenario: TEST-4700
    # Create DVTP Overwriting Tracking ID and Operation ID
    Given path "/api/test/"+appID+"/appDynamicProvider"
    * def randomName = "API_Automation_" + +Util.getTimestamp()