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
package org.gatein.wci.impl;

import org.gatein.wci.spi.ServletContainerContext;
import org.gatein.wci.spi.WebAppContext;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.WebExecutor;
import org.gatein.common.NotYetImplemented;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A static registry for the servlet container context.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class DefaultServletContainer implements ServletContainer
{
   private final static Logger log = Logger.getLogger(ServletContainer.class);

   /** . */
   private final Object lock = new Object();

   /** The event listeners. */
   private final ArrayList<WebAppListener> listeners = new ArrayList<WebAppListener>();

   /** The web applications. */
   private final Map<String, WebAppImpl> webAppMap = new HashMap<String, WebAppImpl>();

   /** The callback. */
   private RegistrationImpl registration;

   public void register(ServletContainerContext context)
   {
      synchronized (lock)
      {
         if (context == null)
         {
            throw new IllegalArgumentException("No null context accepted");
         }

         //
         if (registration == null)
         {

            registration = new RegistrationImpl(this, context);

            // Installs the call back
            context.setCallback(registration);
         }
      }
   }

   public WebExecutor getExecutor(HttpServletRequest request, HttpServletResponse response)
   {
      throw new NotYetImplemented();
   }

   public boolean addWebAppListener(WebAppListener listener)
   {
      synchronized (lock)
      {
         if (listener == null)
         {
            throw new IllegalArgumentException();
         }
         if (listeners.contains(listener))
         {
            return false;
         }
         listeners.add(listener);
         for (Object response : webAppMap.values())
         {
            WebApp webApp = (WebApp)response;
            WebAppLifeCycleEvent event = new WebAppLifeCycleEvent(webApp, WebAppLifeCycleEvent.ADDED);
            safeFireEvent(listener, event);
         }
         return true;
      }
   }

   public boolean removeWebAppListener(WebAppListener listener)
   {
      synchronized (lock)
      {
         if (listener == null)
         {
            throw new IllegalArgumentException();
         }
         if (listeners.remove(listener))
         {
            for (WebApp webApp : webAppMap.values())
            {
               WebAppLifeCycleEvent event = new WebAppLifeCycleEvent(webApp, WebAppLifeCycleEvent.REMOVED);
               safeFireEvent(listener, event);
            }
            return true;
         }
         else
         {
            return false;
         }
      }
   }

   private void safeFireEvent(WebAppListener listener, WebAppEvent event)
   {
      try
      {
         listener.onEvent(event);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private void fireEvent(WebAppEvent event)
   {
      for (WebAppListener listener : listeners)
      {
         safeFireEvent(listener, event);
      }
   }

   /**
    * Generic detyped request dispatch to a servlet context using the include mechanism.
    *
    * @param targetServletContext the target servlet context to dispatch to
    * @param request              the request valid in the current servlet context
    * @param response             the response valid in the current servlet context
    * @param callback             the callback to perform after the dispatch operation
    * @param handback             the handback object that will be provided to the callback
    * @return the object returned by the callback
    * @throws javax.servlet.ServletException any servlet exception
    * @throws java.io.IOException any io exception
    */
   public Object include(
      ServletContext targetServletContext,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestDispatchCallback callback,
      Object handback) throws ServletException, IOException
   {
      RegistrationImpl registration = this.registration;

      //
      if (registration == null)
      {
         throw new IllegalStateException("No SPI installed");
      }

      //
      return registration.context.include(targetServletContext, request, response, callback, handback);
   }

   private static class RegistrationImpl implements ServletContainerContext.Registration
   {

      /** . */
      private boolean disposed;

      /** . */
      private DefaultServletContainer container;

      /** . */
      private ServletContainerContext context;

      public RegistrationImpl(DefaultServletContainer container, ServletContainerContext context)
      {
         this.disposed = false;
         this.container = container;
         this.context = context;
      }

      public boolean registerWebApp(WebAppContext webAppContext)
      {
         if (disposed)
         {
            throw new IllegalStateException("Disposed registration");
         }
         synchronized (container.lock)
         {
            if (webAppContext == null)
            {
               throw new IllegalArgumentException("No null web app context accepted");
            }

            //
            String key = webAppContext.getContextPath();

            //
            if (container.webAppMap.containsKey(key))
            {
               log.debug("Web application " + key + " is already registered");
               return false;
            }
            else
            {
               try
               {
                  log.debug("Web application " + key + " registration");
                  webAppContext.start();
                  WebAppImpl webApp = new WebAppImpl(webAppContext);
                  container.webAppMap.put(key, webApp);
                  container.fireEvent(new WebAppLifeCycleEvent(webApp, WebAppLifeCycleEvent.ADDED));
                  return true;
               }
               catch (Exception e)
               {
                  log.debug("Was not able to start web app context " + key);
                  e.printStackTrace();
                  return false;
               }
            }
         }
      }

      public boolean unregisterWebApp(String webAppId)
      {
         if (disposed)
         {
            throw new IllegalStateException("Disposed registration");
         }
         synchronized (container.lock)
         {
            if (webAppId == null)
            {
               throw new IllegalArgumentException("No null web app id accepted");
            }

            //
            WebAppImpl webApp = container.webAppMap.remove(webAppId);
            if (webApp != null)
            {
               log.debug("Web application " + webAppId + " cleanup");
               container.fireEvent(new WebAppLifeCycleEvent(webApp, WebAppLifeCycleEvent.REMOVED));
               webApp.context.stop();
               return true;
            }
            else
            {
               log.debug("Web application " + webAppId + " was not registered");
               return false;
            }
         }
      }

      public void cancel()
      {
         if (disposed)
         {
            throw new IllegalStateException("Disposed registration");
         }
         synchronized (container.lock)
         {
            // Unregister all web apps
            for (WebApp webApp : container.webAppMap.values())
            {
               WebAppLifeCycleEvent event = new WebAppLifeCycleEvent(webApp, WebAppLifeCycleEvent.REMOVED);
               container.fireEvent(event);
            }

            //
            container.webAppMap.clear();

            // Uninstall the call back
            try
            {
               context.unsetCallback(this);
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }

            // Update state
            context = null;
            disposed = true;
            container.registration = null;
         }
      }
   }

   /** Implementation of the <code>WebApp</code> interface. */
   private static class WebAppImpl implements WebApp
   {

      /** . */
      final WebAppContext context;

      public WebAppImpl(WebAppContext context)
      {
         this.context = context;
      }

      public ServletContext getServletContext()
      {
         return context.getServletContext();
      }

      public ClassLoader getClassLoader()
      {
         return context.getClassLoader();
      }

      public String getContextPath()
      {
         return context.getContextPath();
      }

      public boolean importFile(String parentDirRelativePath, String name, InputStream source, boolean overwrite) throws IOException
      {
         return context.importFile(parentDirRelativePath, name, source, overwrite);
      }
   }
}