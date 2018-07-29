package ru.dayneko

import java.io._
import java.nio.file.{Files, Path, Paths}
import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse, Part}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{BasicCookieStore, CloseableHttpClient, HttpClientBuilder}
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import scala.collection.mutable.ListBuffer
import Auth._
import javax.servlet.ServletInputStream
import org.apache.http.impl.cookie.BasicClientCookie
import Utility._

@WebServlet(Array("/uploadFile/*"))
class UploadServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    if(req.getSession.getAttribute(authToken) != null) {
      val out        = resp.getOutputStream
      val fileName   = req.getParameter("fileName")
      val path: Path = Paths.get(s"$rootFolder\\$fileName")

      resp.setCharacterEncoding("UTF8")
      resp.setContentType("application/octet-stream")
      resp.setHeader("Content-Disposition", s"attachment; filename=$fileName")
      try {
        Files.copy(path, out)
        out.flush()
      } finally {
        out.close()
      }
    } else {
      resp.setStatus(403)
    }
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val sessionId: String           = req.getSession.getAttribute(authToken).toString
    val client: CloseableHttpClient = createHttpClient(req, sessionId)
    val outerIds: ListBuffer[String] = new ListBuffer[String]()

    val stream: ServletInputStream = req.getInputStream
    val reader: BufferedReader     = new BufferedReader(new InputStreamReader(stream))
    var line: String               = ""

    try {
      while ({line = reader.readLine(); line != null }) {
        try {
          outerIds += "\\d{5,12}".r.findFirstMatchIn(line).get.matched


        } catch {
          case e: NoSuchElementException => e
        }
      }
      outerFile(outerIds.mkString(","), sessionId, client, resp)
    } catch {
      case e: IOException => e.printStackTrace();
    } finally {
      reader.close()
      stream.close()
    }
  }

  /**
    * @param callsId id of current SESSIONID
    * @param sessionId curennt SESSIONID
    * @param client http client
    * @return zip file
    */
  def outerFile(callsId: String, sessionId: String, client: CloseableHttpClient, resp: HttpServletResponse): Unit = {
    val tokenCallsId: String              = parseCallsToken(callsId, sessionId, client)
    val resCallsId: CloseableHttpResponse = client.execute(new HttpGet(s"http://127.0.0.1:8081/callr/sendf.mp3;jid=$sessionId?token=$tokenCallsId&id_c[]=$callsId&type=1&action=download"))
    val tempFile: File                    = File.createTempFile(filePrefix, fileSuffix)
    val outputStream: OutputStream        = new FileOutputStream(tempFile)
    resCallsId.getEntity.writeTo(outputStream)

    resp.setHeader("fileName", tempFile.getName)
    resp.setStatus(200)
  }


  /**
    * Parse token from second request
    * @param callsId id of current SESSIONID
    * @param sessionId curennt SESSIONID
    * @param client http client
    * @return token of callsId
    */
  def parseCallsToken(callsId: String, sessionId: String, client: CloseableHttpClient): String = {
    val resCallsId: CloseableHttpResponse = client.execute(new HttpGet(s"http://127.0.0.1:8081/callrec/getToken;jid=$sessionId?id_c[]=$callsId&type=1&action=download"))
    val callsIdBody: String               = io.Source.fromInputStream(resCallsId.getEntity.getContent).getLines().mkString
    resCallsId.close()

    "<reply>([\\w|\\d]+)</reply>".r.findFirstMatchIn(callsIdBody).get.group(1)
  }

  /**
    * @param req current request object
    * @param sessionId current SESSIONID
    * @return http client
    */
  def createHttpClient(req: HttpServletRequest, sessionId: String): CloseableHttpClient = {
    val cookieStore: BasicCookieStore = new BasicCookieStore()
    cookieStore.addCookie(new BasicClientCookie(authToken, sessionId))
    HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()
  }
}
