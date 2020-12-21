package com.example.app

import com.example.app.model.Responses.Price
import org.scalatra.test.scalatest._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

class ControllerTestSpec extends ScalatraFunSuite  {

  addServlet(classOf[Controller], "/*")
  implicit val formats = DefaultFormats

  test("GET /health on Controller should return status 200") {
    get("/health") {
      status should equal (200)
    }
  }

  test("GET /price on Controller should return status 200 and price") {
    get("/price?date=2022-01-28") {
      status should equal (200)
      parse(response.getContent).extract[Price].price shouldEqual 10291.022456252716
    }
  }

}
