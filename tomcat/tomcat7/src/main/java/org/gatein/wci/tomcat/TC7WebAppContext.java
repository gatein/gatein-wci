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
package org.gatein.wci.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Wrapper;
import org.gatein.wci.command.CommandServlet;
import org.gatein.wci.spi.CatalinaWebAppContext;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import java.io.IOException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision: 1.0 $
 */
public class TC7WebAppContext extends CatalinaWebAppContext
{
   /** . */
   private final Context context;

   /** . */
   private Wrapper commandServlet;

   TC7WebAppContext(Context context) throws Exception
   {
      super(context.getServletContext(), context.getLoader().getClassLoader(), context.getPath());

      this.context = context;
   }

   protected void performStartup() throws Exception
   {
      try
      {
         String className = getCommandServletClassName();
         if (null == className)
         {
            return;
         }

         commandServlet = context.createWrapper();
         commandServlet.setName(GATEIN_SERVLET_NAME);
         commandServlet.setLoadOnStartup(GATEIN_SERVLET_LOAD_ON_STARTUP);
         commandServlet.setServletClass(className);
         context.addChild(commandServlet);
         context.addServletMapping(GATEIN_SERVLET_PATH, GATEIN_SERVLET_NAME);
      }
      catch (Exception e)
      {
         cleanup();
         throw e;
      }
   }

   protected void cleanup()
   {
      if (commandServlet != null)
      {
         try
         {
            context.removeServletMapping(GATEIN_SERVLET_PATH);
            context.removeChild(commandServlet);
         }
         catch (Exception e)
         {
         }
      }
   }

   public boolean invalidateSession(String sessId)
   {
      Manager mgr = context.getManager();
      if (mgr != null)
      {
         try
         {
            Session sess = mgr.findSession(sessId);
            if (sess != null)
            {
               sess.expire();
               return true;
            }
         }
         catch (IOException ignored)
         {
         }
      }
      return false;
   }

   @Override
   public void fireRequestDestroyed(ServletRequest servletRequest)
   {
      Object instances[] = context.getApplicationEventListeners();
      if (null != instances && instances.length > 0)
      {
         ServletRequestEvent event = new ServletRequestEvent(context.getServletContext(), servletRequest);
         for (int i = instances.length - 1; i >= 0; i--)
         {
            if (null == instances[i])
            {
               continue;
            }
            if (!(instances[i] instanceof ServletRequestListener))
            {
               continue;
            }
            ServletRequestListener listener = (ServletRequestListener) instances[i];
            try
            {
               listener.requestDestroyed(event);
            }
            catch (Throwable t)
            {
               log.warn("Error calling requestDestroyed() for " + listener.toString(), t);
            }
         }
      }
   }

   @Override
   public void fireRequestInitialized(ServletRequest servletRequest)
   {
      Object instances[] = context.getApplicationEventListeners();
      if (null != instances && instances.length > 0)
      {
         ServletRequestEvent event = new ServletRequestEvent(context.getServletContext(), servletRequest);
         for (int i = 0; i < instances.length; i++)
         {
            if (null == instances[i])
            {
               continue;
            }
            if (!(instances[i] instanceof ServletRequestListener))
            {
               continue;
            }
            ServletRequestListener listener = (ServletRequestListener) instances[i];
            try
            {
               listener.requestInitialized(event);
            }
            catch (Throwable t)
            {
               log.warn("Error calling requestInitialized() for " + listener.toString(), t);
            }
         }
      }
   }
}
