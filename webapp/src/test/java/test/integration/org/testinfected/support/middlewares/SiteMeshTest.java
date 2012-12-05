package test.integration.org.testinfected.support.middlewares;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.testinfected.support.Server;
import org.testinfected.support.decoration.Decorator;
import org.testinfected.support.decoration.Selector;
import org.testinfected.support.MiddlewareStack;
import org.testinfected.support.middlewares.SiteMesh;
import test.support.org.testinfected.support.web.HttpRequest;
import test.support.org.testinfected.support.web.HttpResponse;
import test.support.org.testinfected.support.web.StaticResponse;

import java.io.IOException;
import java.io.Writer;

import static org.hamcrest.CoreMatchers.equalTo;
import static test.support.org.testinfected.support.web.StaticResponse.respondWith;

@RunWith(JMock.class)
public class SiteMeshTest {
    Mockery context = new JUnit4Mockery();
    Selector selector = context.mock(Selector.class);
    States response = context.states("response").startsAs("selected");

    String originalPage = "<plain page>";
    String decoratedPage = "<decorated page>";
    SiteMesh siteMesh = new SiteMesh(selector, new FakeDecorator());

    Server server = new Server(9999);
    StaticResponse app = respondWith(Status.OK, originalPage);
    HttpRequest request = HttpRequest.aRequest().to(server);

    @Before public void
    startServer() throws IOException {
        context.checking(new Expectations() {{
            allowing(selector).select(with(any(Response.class))); will(returnValue(true)); when(response.is("selected"));
            allowing(selector).select(with(any(Response.class))); will(returnValue(false)); when(response.isNot("selected"));
        }});

        server.run(new MiddlewareStack() {{
            use(siteMesh);
            run(app);
        }});
    }

    @After public void
    stopServer() throws Exception {
        server.shutdown();
    }

    @Test public void
    runsContentThroughDecoratorWhenResponseIsSelected() throws IOException {
        HttpResponse response = request.send();
        response.assertHasContent(equalTo(decoratedPage));
    }

    @Test public void
    doesNotDecorateContentWhenResponseIsNotSelected() throws IOException {
        response.become("unselected");

        HttpResponse response = request.send();
        response.assertHasContent(equalTo(originalPage));
    }

    @Test public void
    preservesOriginalPageEncodingWhetherResponseIsSelectedOrNot() throws IOException {
        app.setContentEncoding("UTF-16");
        decoratedPage = "<The following characters require encoding: éçë>";

        request.send().assertContentIsEncodedAs("UTF-16");
        response.become("unselected");
        request.send().assertContentIsEncodedAs("UTF-16");
    }

    private class FakeDecorator implements Decorator {
        public void decorate(Writer out, String content) throws IOException {
            out.write(decoratedPage);
        }
    }
}