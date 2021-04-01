package com.rtbengine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import akka.actor.{ ActorRefFactory, ActorSystem }
import akka.http.scaladsl.Http

import web.WebServiceT

object Main extends App {

  def start() = {
    println("Starting RTB Engine Service")

    implicit val system = ActorSystem("RTBEngineAkkaHttpServer")

    val futureBinding   = Http().newServerAt(
      interface = "localhost",
      port      = 8080
    ).bindFlow( new WebServiceT {
      override def actorRefFactory: ActorRefFactory = system
    }.route
    )

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex)     =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate
    }
  }

  start()
}
