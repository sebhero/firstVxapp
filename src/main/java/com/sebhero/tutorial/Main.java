package com.sebhero.tutorial;

import io.vertx.core.Vertx;

/**
 * Created by Sebastian Börebäck on 2016-02-01.
 */
public class Main {


	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(MyFirstVerticle.class.getName());
	}
}
