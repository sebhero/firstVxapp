package com.sebhero.tutorial;

import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Sebastian Börebäck on 2016-02-07.
 */
public class Whisky {

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private String id;

	private String name;

	private String origin;

	public Whisky(String name, String origin) {
		this.id = "";
		this.name = name;
		this.origin = origin;
	}

	public Whisky(String id, String name, String origin) {
		this.id = id;
		this.name = name;
		this.origin = origin;
	}

	public Whisky(JsonObject json) {
		this.name = json.getString("name");
		this.origin = json.getString("origin");
		this.id = json.getString("_id");
	}

	public Whisky() {

		this.id = "";
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject()
				.put("name", name)
				.put("origin", origin);
		//IF the id already exists
		if (id != null && !id.isEmpty()) {
			json.put("_id", id);
		}
		return json;
	}

	public String getName() {
		return name;
	}

	public Whisky setName(String name) {
		this.name = name;
		return this;
	}

	public String getOrigin() {
		return origin;
	}

	public Whisky setOrigin(String origin) {
		this.origin = origin;
		return this;
	}

	public String getId() {
		return id;
	}

	public Whisky setId(String id) {
		this.id = id;
		return this;
	}

}
