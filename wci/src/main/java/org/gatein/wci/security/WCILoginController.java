/*
* Copyright (C) 2003-2009 eXo Platform SAS.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.gatein.wci.security;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class WCILoginController extends HttpServlet
{
   /** . */
   private static final Logger log = LoggerFactory.getLogger(WCILoginController.class);

   /** . */
   protected Credentials credentials;

   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      String username = req.getParameter("username");
      String password = req.getParameter("password");

      //
      if (username == null)
      {
         log.error("Tried to access the portal login controller without username provided");
         resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No username provided");
         return;
      }
      if (password == null)
      {
         log.error("Tried to access the portal login controller without password provided");
         resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No password provided");
         return;
      }

      //
      log.debug("Found username and password and set credentials in http session");
      credentials = new Credentials(username, password);
      req.getSession().setAttribute(Credentials.CREDENTIALS, credentials);
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      doGet(req, resp);
      Field f;
   }
}
