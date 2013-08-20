package com.example

import akka.actor.Actor
import spray.routing._
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.http.HttpResponse
import spray.httpx.SprayJsonSupport
import spray.json.{JsonFormat, DefaultJsonProtocol}


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {
	
  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context 

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}

// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {
  
  val proxyActor = actorRefFactory.actorOf(Props[ProxyActor])
  
  implicit def executionContext = actorRefFactory.dispatcher
 
  import ResultJsonProtocol._
  import SprayJsonSupport._
  
  implicit val timeout = Timeout(5 seconds)
  
  val myRoute =
    path("") {
      get {  
        
    	complete {
    		(proxyActor ? GetTransaction()).mapTo[GoogleApiResult[Result]].map(f => s"${f.status}")    	  
    	}
    	
       }
    }
}