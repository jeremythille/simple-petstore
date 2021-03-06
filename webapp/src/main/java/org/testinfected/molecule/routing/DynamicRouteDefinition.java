package org.testinfected.molecule.routing;

import org.testinfected.molecule.Application;
import org.testinfected.molecule.HttpMethod;
import org.testinfected.molecule.matchers.Matchers;
import org.testinfected.molecule.util.Matcher;

import static org.testinfected.molecule.matchers.Matchers.equalTo;

public class DynamicRouteDefinition implements RouteDefinition, ViaClause {

    private Matcher<? super String> path;
    private Matcher<? super String> method = Matchers.<String>anything();
    private Application app;

    public DynamicRouteDefinition map(String path) {
        return map(new DynamicPath(path));
    }

    public DynamicRouteDefinition map(Matcher<? super String> path) {
        this.path = path;
        return this;
    }

    public DynamicRouteDefinition via(HttpMethod method) {
        return via(equalTo(method.name()));
    }

    public DynamicRouteDefinition via(Matcher<? super String> method) {
        this.method = method;
        return this;
    }

    public DynamicRouteDefinition to(Application application) {
        this.app = application;
        return this;
    }

    public DynamicRoute toRoute() {
        ensureValid();
        return new DynamicRoute(path, method, app);
    }

    public void ensureValid() {
        if (path == null) throw new IllegalStateException("No path was specified");
    }
}
