/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wci.impl.generic;

import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.authentication.AuthenticationResult;
import org.gatein.wci.authentication.GenericAuthentication;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.spi.ServletContainerContext;
import org.gatein.wci.command.CommandDispatcher;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class GenericServletContainerContext implements ServletContainerContext, ServletContextListener
{

   /** . */
   private static final Map<String, GenericWebAppContext> pendingContexts = Collections.synchronizedMap(new HashMap<String, GenericWebAppContext>());

   /** . */
   private static GenericServletContainerContext instance;

   private static HashMap<ServletContext, String> requestDispatchMap = new HashMap<ServletContext, String>();
   
   public static GenericServletContainerContext getInstance()
   {
      return instance;
   }

   /** . */
   private Registration registration;

   public GenericServletContainerContext()
   {
   }

   public static void register(GenericWebAppContext webAppContext, String dispatcherPath)
   {
      requestDispatchMap.put(webAppContext.getServletContext(), dispatcherPath);
      if (instance != null && instance.registration != null)
      {
         instance.registration.registerWebApp(webAppContext);
      }
      else
      {
         pendingContexts.put(webAppContext.getContextPath(), webAppContext);
      }
   }

   public static void unregister(ServletContext servletContext)
   {
      requestDispatchMap.remove(servletContext);
      
      String contextPath = servletContext.getContextPath();
      
      if (instance != null && instance.registration != null)
      {
         instance.registration.unregisterWebApp(contextPath);
      }

      //
      if (pendingContexts.containsKey(contextPath))
      {
         pendingContexts.remove(contextPath);
      }
   }

   /** . */
   //private final CommandDispatcher dispatcher = new CommandDispatcher("/gateinservlet");

   public Object include(
      ServletContext targetServletContext,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestDispatchCallback callback,
      Object handback) throws ServletException, IOException
   {
      String dispatcherPath = requestDispatchMap.get(targetServletContext);
      CommandDispatcher dispatcher = new CommandDispatcher(dispatcherPath);
      
      return dispatcher.include(targetServletContext, request, response, callback, handback);
   }

   public void setCallback(Registration registration)
   {
      this.registration = registration;

      //
      for (GenericWebAppContext pendingContext : pendingContexts.values())
      {
         registration.registerWebApp(pendingContext);
      }
   }

   public void unsetCallback(Registration registration)
   {
      this.registration = null;
   }

   public AuthenticationResult login(HttpServletRequest request, HttpServletResponse response, String userName, String password, long validityMillis)
   {
      return GenericAuthentication.getInstance().login(userName, password, request, response, validityMillis);
   }

   public void logout(HttpServletRequest request, HttpServletResponse response)
   {
      GenericAuthentication.getInstance().logout(request, response);
   }
   //

   public void contextInitialized(ServletContextEvent servletContextEvent)
   {
      if (instance != null)
      {
         throw new IllegalStateException("Shared instance singleton already created");
      }

      //
      instance = this;

      // Register
      DefaultServletContainerFactory.registerContext(this);
   }

   public void contextDestroyed(ServletContextEvent servletContextEvent)
   {
      // Should we really do something ?
   }
}