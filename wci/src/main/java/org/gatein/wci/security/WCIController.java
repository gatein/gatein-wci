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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public abstract class WCIController
{
   public void sendAuth(HttpServletRequest req, HttpServletResponse resp, String jUsername, String jPassword) throws IOException
   {
      resp.sendRedirect(getAuthURI(req, resp, jUsername, jPassword));
   }

   public String getInitialURI(HttpServletRequest req)
   {
      String initialURI = req.getParameter("initialURI");
      if (initialURI ==  null)
      {
         initialURI = (String)req.getAttribute("javax.servlet.forward.request_uri");
      }
      if (initialURI == null)
      {
         initialURI =  getHomeURI(req);
      }
      return initialURI;
   }

   public String getAuthURI(HttpServletRequest req, HttpServletResponse resp, String jUsername, String jPassword)
   {
      String initialURI = getInitialURI(req);
      if (!initialURI.endsWith("/"))
      {
         initialURI += "/";
      }
      String url = initialURI + "j_security_check?j_username=" + jUsername + "&j_password=" + jPassword;
      return resp.encodeRedirectURL(url);
   }

   abstract public void showLoginForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
   abstract public void showErrorLoginForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
   abstract public Credentials getCredentials (HttpServletRequest req, HttpServletResponse resp);
   abstract public String getHomeURI(HttpServletRequest req);
}
