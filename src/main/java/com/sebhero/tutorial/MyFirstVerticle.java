package com.sebhero.tutorial;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sebastian Börebäck on 2016-01-31.
 */
public class MyFirstVerticle extends AbstractVerticle {

	private Map<Integer, Whisky> products = new LinkedHashMap<>();

	@Override
	public void start(Future<Void> fut) throws Exception {

		createSomeData();

		Router router = Router.router(vertx);

//		String ip = "192.168.0.10";
		//Setup IP and PORT. because of Openshift
		String ip;
		int port = Integer.getInteger("http.port", 8080);

 		if (System.getProperty("http.address") == null) {
			ip = "localhost";
		} else {
			ip = System.getProperty("http.address");
		}

		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.sendFile("ws.html");
		});

		//static handler for files like html, css js m.m.
		//everything in assets is shared.
		router.route("/assets/*").handler(StaticHandler.create("assets"));

		//rest API on /api/whiskies
		//GET rest
		router.get("/api/whiskies").handler(this::getAll);
		//Create rest
		router.route("/api/whiskies*").handler(BodyHandler.create());
		//connect Create to method addOne
		router.post("/api/whiskies").handler(this::addOne);
		//Remove rest
		router.delete("/api/whiskies/:id").handler(this::deleteOne);
		//Read reast
		router.get("/api/whiskies/:id").handler(this::getOne);
		//UPDATE rest
		router.put("/api/whiskies/:id").handler(this::updateOne);


		/**
		 * //old handler
		 .requestHandler(r ->{
		 if (r.uri().equals("/")) {
		 r.response().sendFile("ws.html");
		 }

		 })
		 */

		vertx
			.createHttpServer()
			.requestHandler(router::accept)
			.listen(port,ip, result ->{
				if (result.succeeded()) {
					fut.complete();
				} else {
					fut.fail(result.cause());
				}
			});
		System.out.println("Server started");
	}

	private void updateOne(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		JsonObject json = routingContext.getBodyAsJson();
		if (id == null || json == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			final Integer idAsInteger = Integer.valueOf(id);
			Whisky whisky = products.get(idAsInteger);
			if (whisky == null) {
				routingContext.response().setStatusCode(404).end();
			} else {
				whisky.setName(json.getString("name"));
				whisky.setOrigin(json.getString("origin"));
				routingContext.response()
						.putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(whisky));
			}
		}
	}

	private void getOne(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			final Integer idAsInteger = Integer.valueOf(id);
			Whisky whisky = products.get(idAsInteger);
			if (whisky == null) {
				routingContext.response().setStatusCode(404).end();
			} else {
				routingContext.response()
						.putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(whisky));
			}
		}
	}

	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			products.remove(idAsInteger);
		}
		routingContext.response().setStatusCode(204).end();
	}

	private void addOne(RoutingContext routingContext) {
		//create the wiskey
		//here should be a test that is correct.
		final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString()
				, Whisky.class);
		//add it to json / DB
		products.put(whisky.getId(), whisky);
		//respond to client with the new whiskey
		routingContext.response()
				.setStatusCode(201)
				.putHeader("content-type", "application/json; charset=utf8")
				.end(Json.encodePrettily(whisky));
	}

	private void getAll(RoutingContext routingContext) {
		routingContext.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(products.values()));
	}

	private void createSomeData() {
		Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
		products.put(bowmore.getId(), bowmore);
		Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
		products.put(talisker.getId(), talisker);
	}
}
