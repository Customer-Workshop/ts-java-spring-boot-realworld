package io.spring.bdd.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.cucumber.java.en.Then;
import io.spring.bdd.SharedTestState;
import org.springframework.beans.factory.annotation.Autowired;

public class CommonSteps {

  @Autowired private SharedTestState state;

  @Then("the response status should be {int}")
  public void theResponseStatusShouldBe(int expectedStatus) {
    assertThat(state.getLastResponse().getStatusCode(), equalTo(expectedStatus));
  }

  @Then("the response should contain a {string} object")
  public void theResponseShouldContainObject(String objectKey) {
    assertThat(state.getLastResponse().jsonPath().getMap(objectKey), notNullValue());
  }
}
