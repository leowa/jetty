package com.rest.test;

import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Path("/")
public class App extends Application {
    private final Keywords keywords;
    private static Random rand = new Random();
    private static PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    public App() throws IOException {
        this.keywords = new Keywords("localhost", 6379);
    }

    // Add Service APIs
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<Class<?>>();
        // register REST modules
        resources.add(App.class);
        // Manually adding MOXyJSONFeature
        resources.add(org.glassfish.jersey.moxy.json.MoxyJsonFeature.class);
        // Configure Moxy behavior
        resources.add(MoxyJsonConfigResolver.class);
        return resources;
    }

    @Path("/keywords")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listKeyword() throws InterruptedException {
        StopWatch sw = new StopWatch();
        sw.start();
        try {
            Thread.sleep(rand.nextInt(500));
            return Response.ok(this.keywords.list()).build();
        } finally {
            registry.counter("hot_key_request_total", "method", "GET", "path", "/keywords", "status", "200").increment();
            registry.summary("hot_key_request_duration", "method", "GET", "path", "/keywords", "status", "200").record(sw.getNanoTime() / Math.pow(10, 9));
        }
    }

    @Path("/keywords/{keyword}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKeyword(@PathParam("keyword") final String keyword) throws InterruptedException {
        HotKeyword ret = this.keywords.get(keyword);
        StopWatch sw = new StopWatch();
        sw.start();
        Thread.sleep(rand.nextInt(500));
        int status = 200;
        try {
            if (ret == null) {
                status = 404;
                return Response.status(Status.NOT_FOUND).build();
            } else {
                return Response.ok(ret).build();
            }
        } finally {
            registry.counter("hot_key_request_total", "method", "GET", "path", "/keywords/{keyword}", "status", Integer.toString(status)).increment();
            registry.summary("hot_key_request_duration", "method", "GET", "path", "/keywords/{keyword}", "status", Integer.toString(status)).record(sw.getNanoTime() / Math.pow(10, 9));
        }
    }

    @Path("/keywords/{keyword}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addKeyword(@PathParam("keyword") final String keyword) throws InterruptedException {
        StopWatch sw = new StopWatch();
        sw.start();
        Thread.sleep(rand.nextInt(500));
        int status = rand.nextInt(10) > 8 ? 400 : 200;
        try {
            return Response.ok(this.keywords.put(keyword)).build();
        } finally {
            registry.counter("hot_key_request_total", "method", "POST", "path", "/keywords/{keyword}", "status", Integer.toString(status)).increment();
            registry.summary("hot_key_request_duration", "method", "POST", "path", "/keywords/{keyword}", "status", Integer.toString(status)).record(sw.getNanoTime() / Math.pow(10, 9));
        }
    }

    @Path("/metrics")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getMetrics() {
        return registry.scrape();
    }

    public static void main(String[] args) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);

        ServletHolder servlet =
                context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        servlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        // servlet.setInitParameter("jersey.config.server.provider.classnames",
        // App.class.getCanonicalName());
        servlet.setInitParameter("javax.ws.rs.Application", App.class.getCanonicalName());
        // servlet.setInitParameter("com.sun.jersey.config.property.packages",
        // "com.irootech.mds.examples.metrics.jetty");
        // servlet.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        new JvmThreadMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }

}
