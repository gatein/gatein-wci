/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.gatein.wci.jboss;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Wrapper;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.command.CommandServlet;
import org.gatein.wci.spi.WebAppContext;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class JB7WebAppContext implements WebAppContext
{
   private final static Logger log = LoggerFactory.getLogger(JB7WebAppContext.class);

   /**
    * .
    */
   private ServletContext servletContext;

   /**
    * .
    */
   private ClassLoader loader;

   /**
    * .
    */
   private String contextPath;

   /**
    * .
    */
   private final Context context;

   /**
    * .
    */
   private Wrapper commandServlet;

   JB7WebAppContext(Context context) throws Exception
   {
      this.context = context;

      //
      servletContext = context.getServletContext();
      loader = context.getLoader().getClassLoader();
      contextPath = context.getPath();
   }

   public void start() throws Exception
   {
      try
      {
         String className = CommandServlet.class.getName();
         try
         {
            loader.loadClass(className);
         }
         catch(Exception ex)
         {
            log.warn("WCI integration skipped for context: " + context);
            return;
         }
         commandServlet = context.createWrapper();
         commandServlet.setName("TomcatGateInServlet");
         commandServlet.setLoadOnStartup(0);
         commandServlet.setServletClass(className);
         context.addChild(commandServlet);
         context.addServletMapping("/tomcatgateinservlet", "TomcatGateInServlet");
      }
      catch (Exception e)
      {
         cleanup();
         throw e;
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
            context.removeServletMapping("tomcatgateinservlet");
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

   public boolean importFile(String parentDirRelativePath, String name, InputStream source, boolean overwrite)
      throws IOException
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
