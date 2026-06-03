package org.springframework.samples.petclinic.gatling;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.core.exec.Executable;

import java.time.Duration;

public class BasicSimulation extends Simulation {

	// Define HTTP configuration
	// Reference: https://docs.gatling.io/reference/script/protocols/http/protocol/
	private static final HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
		.acceptHeader("application/json")
		.userAgentHeader(
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");

	private final static String SESSION_OWNERS_PAGE = "session_owners_page";

	private static ChainBuilder getOwnersPage() {
		return exec(session -> {
			int page = session.getInt(SESSION_OWNERS_PAGE);
			http("get-owner-page-" + page).get("/owners?page=" + page);
			return session;
		});
	}

	private static HttpRequestActionBuilder getOwner(int owner) {
		return http("get-owner-" + owner).get("/owners/" + owner);
	}

	private static HttpRequestActionBuilder newVisit() {
		return http("new-visit").get("/owners/21/pets/61/visits/new");
	}

	private static HttpRequestActionBuilder createVisit() {
		return http("create-visit").post("/owners/21/pets/61/visits/new")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.formParam("date", "2026-06-30")
			.formParam("description", "test")
			.formParam("petId", "61");
	}

	private static ChainBuilder sleep() {
		return pause(Duration.ofMillis(1_000), Duration.ofMillis(2_000));
	}

	private static Executable firstLoad() {
		return group("first-load").on(http("first-load").get("/"), http("owners").get("/owners"));
	}

	private static Executable browseOwners() {
		return group("browse-owners").on(exec(session -> session.set(SESSION_OWNERS_PAGE, 2)),
				repeat(4).on(sleep(), getOwnersPage(),
						exec(session -> session.set(SESSION_OWNERS_PAGE, session.getInt(SESSION_OWNERS_PAGE) + 1))));
	}

	// Define scenario
	// Reference: https://docs.gatling.io/reference/script/core/scenario/
	private static ScenarioBuilder bookVisit() {
		return scenario("BookVisit")
			.exec(group("Homepage").on(firstLoad(), browseOwners(), getOwner(21), newVisit(), createVisit()));
	}

	// Define assertions
	// Reference: https://docs.gatling.io/reference/script/core/assertions/
	private static final Assertion assertion = global().failedRequests().count().lt(1L);

	private static final OpenInjectionStep injectionProfile = incrementUsersPerSec(20.0).times(5)
		.eachLevelLasting(10)
		.separatedByRampsLasting(10)
		.startingFrom(20);

	// Define injection profile and execute the test
	// Reference: https://docs.gatling.io/reference/script/core/injection/
	{
		setUp(bookVisit().injectOpen(injectionProfile)).assertions(assertion).protocols(httpProtocol);
	}

}
