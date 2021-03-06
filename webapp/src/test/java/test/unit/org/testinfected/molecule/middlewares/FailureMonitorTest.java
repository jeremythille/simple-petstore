package test.unit.org.testinfected.molecule.middlewares;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.testinfected.molecule.Application;
import org.testinfected.molecule.Request;
import org.testinfected.molecule.Response;
import org.testinfected.molecule.middlewares.FailureMonitor;
import org.testinfected.molecule.util.FailureReporter;
import test.support.org.testinfected.molecule.unit.MockRequest;
import test.support.org.testinfected.molecule.unit.MockResponse;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static test.support.org.testinfected.molecule.unit.MockRequest.aRequest;
import static test.support.org.testinfected.molecule.unit.MockResponse.aResponse;

public class FailureMonitorTest {
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();

    FailureReporter failureReporter = context.mock(FailureReporter.class);
    FailureMonitor monitor = new FailureMonitor(failureReporter);

    Exception error = new Exception("An internal error occurred!");

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Test public void
    notifiesFailureReporterAndRethrowsExceptionInCaseOfError() throws Exception {
        monitor.connectTo(crashWith(error));

        context.checking(new Expectations() {{
            oneOf(failureReporter).errorOccurred(with(same(error)));
        }});

        try {
            monitor.handle(request, response);
            fail("Exception did not bubble up");
        } catch (Exception e) {
            assertThat("error", e, sameInstance(error));
        }
    }

    private Application crashWith(final Exception error) {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
                throw error;
            }
        };
    }
}
