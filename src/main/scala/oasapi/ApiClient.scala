package oasapi

import com.tfsm.oas.apiclient.OaxApiClient
import scala.xml.XML
import java.util.Properties
import scala.io.Source

object ApiClient extends App {

  val props = new Properties()
  props.load(Source.fromFile(s"${System.getenv("HOME")}/ads.properties").reader())
  val host = props.getProperty("oas.host")
  val account = props.getProperty("oas.account")
  val userId = props.getProperty("oas.userId")
  val userPassword = props.getProperty("oas.userPassword")

  val oaxApiClient = new OaxApiClient

  def fetchCampaignCpms(campaignIds: Array[String]): Map[String, String] = fetchCampaignCpms(campaignIds.toSeq)

  def fetchCampaignCpms(campaignIds: Seq[String]): Map[String, String] = {

    val request =
      <AdXML>
        {for (id <- campaignIds) yield
        <Request type="Campaign">
          <Campaign action="read">
            <Overview>
              <Id>
                {id}
              </Id>
            </Overview>
          </Campaign>
        </Request>}
      </AdXML>

    val response = XML.loadString {
      oaxApiClient.callOasApi(host, account, userId, userPassword, request.buildString(stripComments = true))
    }

    (for {
      e <- response \ "Response" \ "Exception"
      errorCode <- e.attribute("errorCode")
    } yield ApiException(errorCode.head.text.toInt, e.text)).headOption.foreach(e => throw e)

    (for {
      campaign <- response \ "Response" \ "Campaign"
      id <- campaign \ "Overview" \ "Id"
      sitePricing <- campaign \ "Billing" \ "SitePayout" \ "SitePricing"
      if (sitePricing \ "SiteDomain").text == "www.theguardian.com"
      cpm <- sitePricing \ "Cpm"
    } yield (id.text, cpm.text)).toMap
  }


  val cpms = {
    if (args.isEmpty) {
      val lines = Source.fromFile("campaigns.txt").getLines().take(1000).toSeq
      fetchCampaignCpms(lines)
    } else {
      fetchCampaignCpms(args)
    }
  }

  cpms foreach {
    case (campaignId, cpm) => println(s"$campaignId = $cpm GBP (Cost per Thousand Impressions)")
  }
}


case class ApiException(errorCode: Int, message: String) extends Exception(s"$errorCode: $message")
