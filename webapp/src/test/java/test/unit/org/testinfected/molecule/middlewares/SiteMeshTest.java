package test.unit.org.testinfected.molecule.middlewares;

import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testinfected.molecule.Application;
import org.testinfected.molecule.Request;
import org.testinfected.molecule.Response;
import org.testinfected.molecule.decoration.Decorator;
import org.testinfected.molecule.decoration.Selector;
import org.testinfected.molecule.middlewares.SiteMesh;
import org.testinfected.molecule.util.Charsets;
import test.support.org.testinfected.molecule.unit.MockRequest;
import test.support.org.testinfected.molecule.unit.MockResponse;

import java.io.IOException;
import java.io.Writer;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static test.support.org.testinfected.molecule.unit.MockRequest.aRequest;
import static test.support.org.testinfected.molecule.unit.MockResponse.aResponse;

public class SiteMeshTest {
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();

    Selector selector = context.mock(Selector.class);
    SiteMesh siteMesh = new SiteMesh(selector, new FakeDecorator());

    String originalPage = "<plain page>";
    String decoratedPage = "<decorated page>";
    States page = context.states("page").startsAs("selected");

    MockRequest request = aRequest();
    MockResponse response = aResponse();

    @Before public void
    selectPage() throws Exception {
        context.checking(new Expectations() {{
            allowing(selector).select(with(any(Response.class))); will(returnValue(true)); when(page.is("selected"));
            allowing(selector).select(with(any(Response.class))); will(returnValue(false)); when(page.isNot("selected"));
        }});
    }

    @Before public void
    stubApplication() {
        siteMesh.connectTo(write(originalPage));
    }

    @Test public void
    runsContentThroughDecoratorWhenPageIsSelected() throws Exception {
        siteMesh.handle(request, response);
        response.assertBody(decoratedPage);
    }

    @Test public void
    removesContentLengthHeaderWhenDecorating() throws Exception {
        response.header("Content-Length", String.valueOf(140));
        siteMesh.handle(request, response);
        response.assertHeader("Content-Length", nullValue());
    }

    @Test public void
    leavesContentUntouchedWhenNotDecorating() throws Exception {
        page.become("unselected");
        siteMesh.handle(request, response);
        response.assertBody(originalPage);
        int contentSize = originalPage.getBytes(Charsets.UTF_8).length;
        response.assertBufferSize(equalTo(contentSize));
    }

    @Test public void
    preservesOriginalResponseEncodingWhenDecorating() throws Exception {
        response.withContentType("text/html; charset=utf-8");
        decoratedPage = "<The following characters require encoding: éçë>";

        siteMesh.handle(request, response);

        response.assertContentType(containsString("utf-8"));
        response.assertContentEncodedAs("utf-8");
    }

    private Application write(final String text) {
        return new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body(text);
            }
        };
    }

    private class FakeDecorator implements Decorator {
        public void decorate(Writer out, String content) throws IOException {
            out.write(decoratedPage);
        }
    }
}
