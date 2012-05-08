/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.wci.glassfish;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Engine;
import org.apache.catalina.Wrapper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 4/13/12
 */
public class GF3ContainerServlet extends HttpServlet implements ContainerServlet
{
   private static final String CROSS_CONTEXT_LOGOUT_KEY = "org.gatein.wci.cross_context_logout";

   private Wrapper wrapper;

   private GF3ServletContainerContext containerContext;

   private boolean started;

   public Wrapper getWrapper()
   {
      return wrapper;
   }

   public void setWrapper(Wrapper wrapper)
   {
      this.wrapper = wrapper;

      if (wrapper != null)
      {
         attemptStart();
      }
      else
      {
         attemptStop();
      }
   }

   public void init() throws ServletException
   {
      started = true;
      attemptStart();
   }

   public void destroy()
   {
      started = false;
      attemptStop();
   }

   private void attemptStart()
   {
      if (started && wrapper != null)
      {
         start();
      }
   }

   private void attemptStop()
   {
      if (!started || wrapper == null)
      {
         stop();
      }
   }

   private void start()
   {
      Container container = wrapper;
      while (container.getParent() != null)
      {
         container = container.getParent();
         if (container instanceof Engine)
         {
            Engine engine = (Engine)container;
            containerContext = new GF3ServletContainerContext(engine);
            containerContext.setCrossContextLogout(getCrossContextLogoutConfig());
            containerContext.start();
            break;
         }
      }
   }

   private void stop()
   {
      if (containerContext != null)
      {
         containerContext.stop();
         containerContext = null;
      }
   }

   private boolean getCrossContextLogoutConfig() {
      String val = getServletContext().getInitParameter(CROSS_CONTEXT_LOGOUT_KEY);
      return val == null || Boolean.valueOf(val);
   }
}
