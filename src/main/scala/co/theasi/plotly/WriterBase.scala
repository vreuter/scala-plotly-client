package co.theasi.plotly

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scalaj.http.{ Http, HttpRequest, HttpResponse }

import org.json4s.JString
import org.json4s.native.JsonMethods.parse

trait WriterBase {

  def sendToPlotlyAsync(
      origin: String,
      args: String,
      kwargs: String
  ): Future[Unit] = {
    val response = Future {
      request(origin, args, kwargs).asString
    }
    response.map { processPlotlyResponse _ }
  }

  def sendToPlotly(
      origin: String,
      args: String,
      kwargs: String
  ) {
    val response = request(origin, args, kwargs).asString
    processPlotlyResponse(response)
  }

  private val plotlyUrl = "https://plot.ly/clientresp"

  private val credentials = Credentials.read

  private def request(
      origin: String,
      args: String,
      kwargs: String
    ): HttpRequest = {
    val request = Http(plotlyUrl).postForm(Seq(
      "un" -> credentials.username,
      "key" -> credentials.key,
      "origin" -> origin,
      "platform" -> "scala",
      "args" -> args,
      "kwargs" -> kwargs
    ))
    request
  }

  private def processPlotlyResponse(response: HttpResponse[String]) {
    val JString(err) = parse(response.body) \ "error"
    if (err != "") {
      throw new PlotlyException(err)
    }
  }

}
