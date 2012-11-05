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
import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Wrapper;
import org.gatein.wci.command.CommandServlet;
import org.gatein.wci.spi.WebAppContext;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 4/13/12
 */
public class GF3WebAppContext implements WebAppContext
{
   private static final String GATEIN_SERVLET_NAME = "TomcatGateInServlet";

   private ServletContext servletContext;

   private ClassLoader loader;

   private String contextPath;

   private final Context context;

   private Wrapper commandServlet;

   public GF3WebAppContext(Context context)
   {
      this.context = context;

      servletContext = context.getServletContext();
      loader = context.getLoader().getClassLoader();
      contextPath = context.getPath();
   }

   public void start() throws Exception
   {
      // only add the command servlet if it hasn't already been added to the context
      final Container child = context.findChild(GATEIN_SERVLET_NAME);
      if (child == null)
      {
         try
         {
            commandServlet = context.createWrapper();
            commandServlet.setName(GATEIN_SERVLET_NAME);
            commandServlet.setLoadOnStartup(0);
            commandServlet.setServletClassName(CommandServlet.class.getName());
            context.addChild(commandServlet);
            context.addServletMapping("/tomcatgateinservlet", GATEIN_SERVLET_NAME);
         }
         catch (Exception e)
         {
            cleanup();
            throw e;
         }
      }
   }

   public void stop()
   {
      cleanup();
   }

   private void cleanup()
   {
      if (commandServlet != null)
      {
         try
         {
            context.removeServletMapping("/tomcatgateinservlet");
            context.removeChild(commandServlet);
         }
         catch (Exception e)
         {
         }
      }
   }

   public ServletContext getServletContext()
   {
      return servletContext;
   }

   public ClassLoader getClassLoader()
   {
      return loader;
   }

   public String getContextPath()
   {
      return contextPath;
   }

   public boolean importFile(String s, String s1, InputStream inputStream, boolean b) throws IOException
   {
      return false;
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
