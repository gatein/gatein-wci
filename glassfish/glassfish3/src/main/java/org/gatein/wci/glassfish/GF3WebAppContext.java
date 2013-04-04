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

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Wrapper;
import org.gatein.wci.spi.CatalinaWebAppContext;

import java.io.IOException;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 4/13/12
 */
public class GF3WebAppContext extends CatalinaWebAppContext
{

   private final Context context;

   private Wrapper commandServlet;

   public GF3WebAppContext(Context context)
   {
      super(context.getServletContext(), context.getLoader().getClassLoader(), context.getPath());

      this.context = context;
   }

   protected void performStartup() throws Exception
   {
      try
      {
         String className = getCommandServletClassName();
         if (null == className) {
            return;
         }

         commandServlet = context.createWrapper();
         commandServlet.setName(GATEIN_SERVLET_NAME);
         commandServlet.setLoadOnStartup(GATEIN_SERVLET_LOAD_ON_STARTUP);
         commandServlet.setServletClassName(className);
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
}
