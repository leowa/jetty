package com.rest.test;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;


import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Path("/")
public class App extends Application {
    private final Keywords keywords;

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
    public Response listKeyword() {
        return Response.ok(this.keywords.list()).build();
    }

    @Path("/keywords/{keyword}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKeyword(@PathParam("keyword") final String keyword) {
        HotKeyword ret = this.keywords.get(keyword);
        if( ret == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            return Response.ok(ret).build();
        }
    }

    @Path("/keywords/{keyword}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addKeyword(@PathParam("keyword") final String keyword) {
        return Response.ok(this.keywords.put(keyword)).build();
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


        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }

}
