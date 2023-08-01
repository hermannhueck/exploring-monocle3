// Optics beyond Lenses with Monocle
//
// Real-world example: circe-optics
//
// see:
// https://scalac.io/blog/scala-optics-lenses-with-monocle/
// https://github.com/note/monocle-example/blob/master/src/test/scala/com/example/CirceExample.scala
//

// import io.circe.optics.JsonPath
import io.circe.optics.JsonPath._

@annotation.nowarn("cat=deprecation")
def referenceJson(streetName: String): io.circe.Json =
  io.circe
    .parser
    .parse(s"""
              |{
              |  "order": {
              |    "address": {
              |      "street": "$streetName",
              |      "city": "someCity"
              |    },
              |    "items": [
              |      {
              |        "name": "OK Computer",
              |        "amount": 1
              |      },
              |      {
              |        "name": "Kid A",
              |        "amount": 3
              |      }
              |    ]
              |  }
              |}
    """.stripMargin)
    .right
    .get

// let's capitalize street name without optics
def modifyWithoutOptics(json: io.circe.Json): io.circe.Json =
  json
    .hcursor
    .downField("order")
    .downField("address")
    .downField("street")
    .withFocus(_.mapString(_.toUpperCase))
    .top
    .get

// now with optics:
def modifyWithOptics(json: io.circe.Json): io.circe.Json =
  root
    .order
    .address
    .street
    .string
    .modify(_.toUpperCase)(json)

// val input         = referenceJson("abc")
// val withOptics    = modifyWithOptics(input)
// val withoutOptics = modifyWithoutOptics(input)

// withOptics == withoutOptics
// withOptics == referenceJson("ABC")
