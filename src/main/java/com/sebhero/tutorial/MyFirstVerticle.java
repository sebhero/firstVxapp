package com.sebhero.tutorial;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Sebastian Börebäck on 2016-01-31.
 */
public class MyFirstVerticle extends AbstractVerticle {


	private Map<Integer, Whisky> products = new LinkedHashMap<>();
	public static final String COLLECTION = "whiskies";
	private MongoClient mongo;

	@Override
	public void start(Future<Void> fut) throws Exception {

		// Create a mongo client
		mongo = MongoClient.createShared(vertx, config());

		//add start data
		createSomeData(
				(nothing) -> startWebApp(
						(http) -> completeStartUp(http, fut)
				), fut);
	}


	private void startWebApp(Handler<AsyncResult<HttpServer>> next) {

		//create the route.
		Router router = Router.router(vertx);

		// TODO: 2016-03-13 :22:57 Old working with Openshift
//		//Setup IP and PORT. because of Openshift
//		String ip;
//		int port = Integer.getInteger("http.port", 8080);
//
// 		if (System.getProperty("http.address") == null) {
//			ip = "localhost";
//		} else {
//			ip = System.getProperty("http.address");
//		}

		//How to handle localhost/ then show resources/ws.html
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
//			response.sendFile("ws.html");
			response
					.putHeader("content-type", "text/html")
					.end("<h1>Hello from my first Vertx</h1>");
		});

		//static handler for files like html, css js m.m.
		//everything in assets is shared. with client
		router.route("/assets/*").handler(StaticHandler.create("assets"));

		//Setup for rest API on /api/whiskies
		//GET rest
		router.get("/api/whiskies").handler(this::getAll);
		//Create rest (CRUD)
		router.route("/api/whiskies*").handler(BodyHandler.create());
		//Create a new Wiskey
		router.post("/api/whiskies").handler(this::addOne);
		//Read reast
		router.get("/api/whiskies/:id").handler(this::getOne);
		//UPDATE rest
		router.put("/api/whiskies/:id").handler(this::updateOne);
		//Delete rest
		router.delete("/api/whiskies/:id").handler(this::deleteOne);

		/**
		 * //old handler
		 .requestHandler(r ->{
		 if (r.uri().equals("/")) {
		 r.response().sendFile("ws.html");
		 }

		 })
		 */

		//Create a http server
		//at ip and port. and the setteinge from router is connected here.
		vertx
				.createHttpServer()
				.requestHandler(router::accept)
				.listen(config().getInteger("http.port", 8080),
						next::handle
						// TODO: 2016-03-13 :22:59 Old working
//			.listen(port,ip, result ->{
//				if (result.succeeded()) {
//					fut.complete();
//				} else {
//					fut.fail(result.cause());
//				}
//			}
				);
		System.out.println("Server started");
	}

	private void completeStartUp(AsyncResult<HttpServer> http, Future<Void> fut) {
		if (http.succeeded()) {
			fut.complete();
		} else {
			fut.fail(http.cause());
		}
	}

	/***
	 * Close the mongo connection
	 *
	 * @throws Exception
	 */
	@Override
	public void stop() throws Exception {
		mongo.close();
	}

	/**
	 * Add a new whiskey
	 *
	 * @param routingContext client data.
	 */
	private void addOne(RoutingContext routingContext) {

		final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);

		System.out.println("add new wiskey "+whisky.getName());

		mongo.insert(COLLECTION, whisky.toJson(), r -> {
			routingContext.response()
					.setStatusCode(201)
					.putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encodePrettily(whisky.setId(r.result())));
		});

//		// TODO: 2016-03-13 :23:10 Old Working code
//		//create the wiskey
//		//here should be a test that is correct.
//		final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString()
//				, Whisky.class);
//		//add it to json / DB
//		products.put(whisky.getId(), whisky);
//		//respond to client with the new whiskey
//		routingContext.response()
//				.setStatusCode(201)
//				.putHeader("content-type", "application/json; charset=utf8")
//				.end(Json.encodePrettily(whisky));
	}


	/**
	 * Get single whiskey element by id
	 *
	 * @param routingContext data from client
	 */
	private void getOne(RoutingContext routingContext) {

		//get the id
		final String id = routingContext.request().getParam("id");

		if (id == null) {
			//error
			routingContext.response().setStatusCode(400).end();
		} else {
			mongo.findOne(COLLECTION, new JsonObject().put("_id", id), null, ar -> {
				if (ar.succeeded()) {
					if (ar.result() == null) {
						routingContext.response().setStatusCode(404).end();
						return;
					}
					Whisky whisky = new Whisky(ar.result());
					routingContext.response()
							.setStatusCode(200)
							.putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encodePrettily(whisky));
				} else {
					routingContext.response().setStatusCode(404).end();
				}
			});
		}

//		// TODO: 2016-03-13 :23:12 Old working code
//		//get the id
//		final String id = routingContext.request().getParam("id");
//
//		if (id == null) {
//			//error
//			routingContext.response().setStatusCode(400).end();
//		} else {
//
//			final Integer idAsInteger = Integer.valueOf(id);
//			Whisky whisky = products.get(idAsInteger);
//			if (whisky == null) {
//				//error
//				routingContext.response().setStatusCode(404).end();
//			} else {
//				//returned the wiskey with that id
//				routingContext.response()
//						.putHeader("content-type", "application/json; charset=utf-8")
//						.end(Json.encodePrettily(whisky));
//			}
//		}
	}

	/**
	 * Update a Wiskey
	 *
	 * @param routingContext data from client. holds the id of the wiskey.
	 */
	private void updateOne(RoutingContext routingContext) {
		//get the id
		final String id = routingContext.request().getParam("id");
		//new json data
		JsonObject json = routingContext.getBodyAsJson();

		if (id == null || json == null) {
			routingContext.response().setStatusCode(400).end();

		} else {
			mongo.update(COLLECTION,
					new JsonObject().put("_id", id),// Select a unique document
					//update the syntax: {$set, the json object containing the fields to update}
					new JsonObject()
							.put("$set", json),
					v -> {
						if (v.failed()) {
							routingContext.response().setStatusCode(404).end();
						} else {
							routingContext.response()
									.putHeader("content-type", "application/json; charset=utf-8")
									.end(Json.encodePrettily(new Whisky(id, json.getString("name"), json.getString("origin"))));
						}
					});
		}

		// TODO: 2016-03-13 :23:15 Old working code
//		if (id == null || json == null) {
//			//error
//			routingContext.response().setStatusCode(400).end();
//		} else {
//			final Integer idAsInteger = Integer.valueOf(id);
//			//get the whiskey
//			Whisky whisky = products.get(idAsInteger);
//
//			if (whisky == null) {
//				//error
//				routingContext.response().setStatusCode(404).end();
//			} else {
//				//uppdate whiskey from json data
//				whisky.setName(json.getString("name"));
//				whisky.setOrigin(json.getString("origin"));
//				//respond to the client with update
//				routingContext.response()
//						.putHeader("content-type", "application/json; charset=utf-8")
//						.end(Json.encodePrettily(whisky));
//			}
//		}
	}


	/***
	 * Remove wiskey by id
	 *
	 * @param routingContext client data, id
	 */
	private void deleteOne(RoutingContext routingContext) {

		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			mongo.removeOne(COLLECTION, new JsonObject().put("_id", id),
					ar -> {
						routingContext.response().setStatusCode(204).end();
					}
			);
		}

//		// TODO: 2016-03-13 :23:20 Old working code
//		String id = routingContext.request().getParam("id");
//		if (id == null) {
//			//error
//			routingContext.response().setStatusCode(400).end();
//		} else {
//			//do the remove
//			Integer idAsInteger = Integer.valueOf(id);
//			products.remove(idAsInteger);
//		}
//		//respond
//		routingContext.response().setStatusCode(204).end();
	}


	/**
	 * Get all the wiskeys
	 *
	 * @param routingContext client data. where to repsond to etc.
	 */
	private void getAll(RoutingContext routingContext) {
		System.out.println("Get All");

		mongo.find(COLLECTION, new JsonObject(), results -> {
			List<JsonObject> objects = results.result();
			List<Whisky> whiskies = objects.stream().map(Whisky::new).collect(Collectors.toList());
			routingContext.response()
					.putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encodePrettily(whiskies));
		});

		// TODO: 2016-03-13 :23:40 OLd working code
//		routingContext.response()
//				.putHeader("content-type", "application/json; charset=utf-8")
//				.end(Json.encodePrettily(products.values()));
	}

	/**
	 * Adds some start data to the db
	 */
	private void createSomeData(Handler<AsyncResult<Void>> next, Future<Void> fut) {

		Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
		Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
		System.out.println(bowmore.toJson());

		// Do we have a data in the colletion? *mongo
		mongo.count(COLLECTION, new JsonObject(), count -> {
			if (count.succeeded()) {
				if (count.result() == 0) {
					// no Whiskies, insert data
					mongo.insert(COLLECTION, bowmore.toJson(), ar -> {
						if (ar.failed()) {
							fut.fail(ar.cause());
						} else {
							mongo.insert(COLLECTION, talisker.toJson(), ar2 -> {
								if (ar2.failed()) {
									fut.failed();
								} else {
									next.handle(Future.<Void>succeededFuture());
								}
							});
						}
					});
				} else {
					next.handle(Future.<Void>succeededFuture());

				}
			} else {
				// report the error
				fut.fail(count.cause());
			}
		});

		// TODO: 2016-03-13 :23:03 Old working
//		Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
//		products.put(bowmore.getId(), bowmore);
//		Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
//		products.put(talisker.getId(), talisker);
	}
}
