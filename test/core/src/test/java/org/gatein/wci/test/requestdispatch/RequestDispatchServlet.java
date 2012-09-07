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
package org.gatein.wci.test.requestdispatch;

import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.ServletContextDispatcher;
import org.gatein.wci.WebApp;
import org.gatein.wci.test.WebAppRegistry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RequestDispatchServlet extends HttpServlet
{

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      ServletContainer container = ServletContainerFactory.getServletContainer();
      WebAppRegistry registry = new WebAppRegistry();
      if (container.addWebAppListener(registry))
      {
         try
         {
            WebApp app = registry.getWebApp("/rdapp");
            if (app != null)
            {
               NormalCallback cb1 = new NormalCallback(app.getServletContext(), app.getClassLoader());
               Exception ex = new Exception();
               ExceptionCallback cb2 = new ExceptionCallback(app.getServletContext(), ex, ex);
               Error err = new Error();
               ExceptionCallback cb3 = new ExceptionCallback(app.getServletContext(), err, err);
               RuntimeException rex = new RuntimeException();
               ExceptionCallback cb4 = new ExceptionCallback(app.getServletContext(), rex, rex);
               IOException ioe = new IOException();
               ExceptionCallback cb5 = new ExceptionCallback(app.getServletContext(), ioe, ioe);

               //
               ServletContextDispatcher dispatcher = new ServletContextDispatcher(req, resp, container);
               Throwable response = cb1.test(null, dispatcher);
               response = cb2.test(response, dispatcher);
               response = cb3.test(response, dispatcher);
               response = cb4.test(response, dispatcher);
               response = cb5.test(response, dispatcher);

               //
               if (response != null)
               {
                  throw new ServletException(response);
               }
               else
               {
                  resp.setStatus(200);
               }
            }
            else
            {
               resp.sendError(500, "Could not find application among " + registry.getKeys());
            }
         }
         finally
         {
            container.removeWebAppListener(registry);
         }
      }
      else
      {
         resp.sendError(500, "Could not add registry as web app listener");
      }
   }
}
