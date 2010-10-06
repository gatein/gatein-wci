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

package org.gatein.wci.authentication;

import org.gatein.wci.impl.DefaultServletContainer;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.security.Credentials;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class GenericAuthenticationResult extends AuthenticationResult {
   private String username;
   private String ticket;

   public GenericAuthenticationResult(String username, String ticket) {
      this.username = username;
      this.ticket = ticket;
   }

   public String getTicket() {
     return ticket;
   }

   public void perform(HttpServletRequest req, HttpServletResponse resp) throws IOException
   {
      req.getSession().removeAttribute(Credentials.CREDENTIALS);
      String url = "j_security_check?j_username=" + username + "&j_password=" + ticket;
      url = resp.encodeRedirectURL(url);
      resp.sendRedirect(url);
      resp.flushBuffer();

      Object o = DefaultServletContainerFactory.getInstance().getServletContainer();
      if (o instanceof DefaultServletContainer)
      {
        ((DefaultServletContainer)o).fireEvent(DefaultServletContainer.EventType.LOGIN, new AuthenticationEvent(req, resp, username, ticket));  
      }
   }
}
