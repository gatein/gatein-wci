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
import org.gatein.wci.api.GateInServletRegistrations;
import org.gatein.wci.authentication.GenericAuthentication;
import org.gatein.wci.impl.DefaultServletContainer;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.spi.ServletContainerContext;
import org.gatein.wci.spi.WebAppContext;
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
   private static GenericServletContainerContext instance;

   private static HashMap<ServletContext, String> requestDispatchMap = new HashMap<ServletContext, String>();

   /** . */
   private GenericAuthentication authentication = new GenericAuthentication();
   
   public static GenericServletContainerContext getInstance()
   {
      return instance;
   }

   /** . */
   private Registration registration;

   public GenericServletContainerContext()
   {
   }

   /** . */

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
      GateInServletRegistrations.setServletContainerContext(this);
   }

   public void unsetCallback(Registration registration)
   {
      this.registration = null;
   }

   public void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials, long validityMillis) throws IOException
   {
      authentication.login(credentials, request, response, validityMillis);
   }

   public void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials, long validityMillis, String initialURI) throws IOException
   {
      authentication.login(credentials, request, response, validityMillis, initialURI);
   }

   public void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException
   {
      authentication.logout(request, response);
   }

   public String getContainerInfo()
   {
      return "Generic";
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

   @Override
   public void registerWebApp(WebAppContext webappContext, String dispatcherPath)
   {
      requestDispatchMap.put(webappContext.getServletContext(), dispatcherPath);
      instance.registration.registerWebApp(webappContext);
   }

   @Override
   public void unregisterWebApp(ServletContext servletContext)
   {
      requestDispatchMap.remove(servletContext);
      instance.registration.unregisterWebApp(servletContext.getContextPath());
   }
}