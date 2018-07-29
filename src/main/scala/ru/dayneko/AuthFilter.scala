package ru.dayneko

import javax.servlet._
import javax.servlet.FilterConfig
import javax.servlet.annotation.WebFilter
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}

@WebFilter(Array("/logout/*"))
class LogoutFilter extends Filter {
  override def init(filterConfig: FilterConfig): Unit = {}
  override def destroy(): Unit                        = {}

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    Auth.logout(request.asInstanceOf[HttpServletRequest])
    chain.doFilter(request, response)
  }
}

@WebFilter(Array("/upload.html"))
class AuthServlet extends Filter {
  override def init(filterConfig: FilterConfig): Unit = {}
  override def destroy(): Unit                        = {}

  override def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain): Unit = {
    val request  = req.asInstanceOf[HttpServletRequest]
    val response = res.asInstanceOf[HttpServletResponse]
    val login    = request.getParameter("login")
    val password = request.getParameter("password")

    if (Auth.isAuthenticated(request)) {
      chain.doFilter(req, res)
    } else if (login != null &&
               password != null &&
               Auth.login(login, password, request)
              ) chain.doFilter(req, res) else response.sendRedirect("login.html")
  }
}

object Auth {
  val authToken: String = "SESSIONID"

  /**
    * Request for Auth
    * @param login current user login
    * @param password current user password
    * @param req current user request
    * @return
    */
  def login(login: String, password: String, req: HttpServletRequest): Boolean = {
    val client: CloseableHttpClient        = HttpClientBuilder.create().build()
    val outerAuthReq: HttpGet               = new HttpGet(s"http://127.0.0.1:8081/callr/loginserv?loginname=$login&password=$password")
    val outerAuthRes: CloseableHttpResponse = client.execute(outerAuthReq)
    val outerAuthBody: String               = io.Source.fromInputStream(outerAuthRes.getEntity.getContent).getLines().mkString

    makeAuth(outerAuthBody, req)
  }

  /**
    * Check Auth
    * @param request current user request
    * @return
    */
  def isAuthenticated(request: HttpServletRequest): Boolean = request.getSession.getAttribute(authToken) != null

  /**
    * Handle outer server Auth
    * @param resBody outer server response body
    * @param req current user request
    * @return result of outer server auth
    */
  def makeAuth(resBody: String, req: HttpServletRequest): Boolean ={
    if (resBody.indexOf("</ok>") > 0) {
      req.getSession.setAttribute(authToken, getSessionId(resBody))
      true
    } else false
  }

  /**
    * Извлекаем sessionId
    * @param s is response from outer server
    * @return
    */
  def getSessionId(s: String): String = "sessionid=\"([\\w|\\d]+)\"".r.findFirstMatchIn(s).get.group(1)

  /**
    * Logout
    * @param req current request
    * removing sessionId parameter from session
    */
  def logout(req: HttpServletRequest): Unit = req.getSession.removeAttribute(authToken)
}
