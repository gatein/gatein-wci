/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.gatein.wci.test.crosscontext;

import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.test.WebAppRegistry;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CrossContextServlet extends HttpServlet
{

   /** . */
   static int status = 0;

   /** . */
   private ServletContainer container;

   /** . */
   private WebAppRegistry registry;

   @Override
   public void init() throws ServletException
   {
      container = ServletContainerFactory.getServletContainer();
      container.addWebAppListener(registry = new WebAppRegistry());
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      WebApp app = registry.getWebApp("/crosscontextapp");
      if (app == null)
      {
         throw new ServletException("Could not find the app to dispatch to");
      }

      //
      HttpSession session = req.getSession(false);
      if (session != null)
      {
         throw new ServletException("Was not expecting a session to exist");
      }

      // Authenticate
      container.login(req, resp, new Credentials("foo", "bar"));

      //
      session = req.getSession();
      String id = session.getId();

      //
      String dispatchedId = (String)container.include(app.getServletContext(), req, resp, new RequestDispatchCallback()
      {
         @Override
         public Object doCallback(ServletContext dispatchedCtx, HttpServletRequest dispatchedReq, HttpServletResponse dispatchedResp, Object handback) throws ServletException, IOException
         {
            if (dispatchedReq.getSession(false) != null)
            {
               throw new ServletException("Was not expecting a session to exist");
            }
            HttpSession dispatchedSession = dispatchedReq.getSession();
            dispatchedSession.setAttribute("payload", "foo");
            return dispatchedSession.getId();
         }
      }, null);

      //
      if (!id.equals(dispatchedId))
      {
         throw new ServletException("Was expecting session ids to be the same");
      }

      // Check we find the same value
      String payload = (String)container.include(app.getServletContext(), req, resp, new RequestDispatchCallback()
      {
         @Override
         public Object doCallback(ServletContext dispatchedCtx, HttpServletRequest dispatchedReq, HttpServletResponse dispatchedResp, Object handback) throws ServletException, IOException
         {
            HttpSession dispatchedSession = dispatchedReq.getSession();
            return dispatchedSession.getAttribute("payload");
         }
      }, null);
      if (!"foo".equals(payload))
      {
         throw new ServletException("Was expecting a foo payload instead of " + payload);
      }

      // Now logout
      container.logout(req, resp);

      //
      payload = (String)container.include(app.getServletContext(), req, resp, new RequestDispatchCallback()
      {
         @Override
         public Object doCallback(ServletContext dispatchedCtx, HttpServletRequest dispatchedReq, HttpServletResponse dispatchedResp, Object handback) throws ServletException, IOException
         {
            HttpSession dispatchedSession = dispatchedReq.getSession();
            return dispatchedSession.getAttribute("payload");
         }
      }, null);
      if (payload != null)
      {
         throw new ServletException("Was expecting a null payload instead of " + payload);
      }

      //
      resp.setStatus(200);
   }

   @Override
   public void destroy()
   {
      if (registry != null && container != null)
      {
         container.removeWebAppListener(registry);
      }
   }
}
