/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.wci.impl.tomcat;

import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Wrapper;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class TC6ContainerServlet extends HttpServlet implements ContainerServlet
{

   /** . */
   private Wrapper wrapper;

   /** . */
   private TC6ServletContainerContext containerContext;

   /** . */
   private boolean started;

   public Wrapper getWrapper()
   {
      return wrapper;
   }

   public void setWrapper(Wrapper wrapper)
   {
      this.wrapper = wrapper;

      //
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

      //
      attemptStart();
   }

   public void destroy()
   {
      started = false;

      //
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
            containerContext = new TC6ServletContainerContext(engine);
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

         //
         containerContext = null;
      }
   }
}
