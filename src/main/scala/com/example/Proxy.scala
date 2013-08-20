package com.example

import akka.actor.{ActorRef, Actor}
import akka.io.IO
import akka.pattern.ask
import spray.can.Http
import spray.http._
import spray.client.pipelining._
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.ActorLogging
import spray.httpx.SprayJsonSupport
import spray.json.{JsonFormat, DefaultJsonProtocol}
import scala.util.{Success, Failure}
import akka.actor.Props
import spray.can.client.HttpClientConnection
import akka.actor.ActorSystem



case class GetTransaction()
case class Result(status: String)
case class GoogleApiResult[T](status: String)


object ResultJsonProtocol extends DefaultJsonProtocol {
   implicit val resultFormat = jsonFormat1(Result)
   implicit def googleApiResultFormat[T :JsonFormat] = jsonFormat1(GoogleApiResult.apply[T])
} 
 
class ProxyActor extends Actor with ActorLogging {
  
  import akka.pattern.pipe
  implicit val system = context.system
  import system.dispatcher // execution context for futures
  import SprayJsonSupport._
  import ResultJsonProtocol._
  
  implicit val timeout = Timeout(5 seconds)
  
  val pipeline = sendReceive ~> unmarshal[GoogleApiResult[Result]]
  val response :Future[GoogleApiResult[Result]] = pipeline(Get("http://localhost:3000/header.json"))
  
  def receive = {
    case GetTransaction() => response pipeTo sender
  }
}