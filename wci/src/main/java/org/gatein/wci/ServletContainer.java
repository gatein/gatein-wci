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
package org.gatein.wci;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.api.GateInServlet;
import org.gatein.wci.authentication.AuthenticationEvent;
import org.gatein.wci.authentication.AuthenticationEventType;
import org.gatein.wci.authentication.AuthenticationException;
import org.gatein.wci.authentication.AuthenticationListener;
import org.gatein.wci.command.CommandDispatcher;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.spi.ServletContainerContext;
import org.gatein.wci.spi.WebAppContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A static registry for the servlet container context.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public final class ServletContainer
{

   private final static Logger log = LoggerFactory.getLogger(ServletContainer.class);

   /** . */
   private final Object lock = new Object();

   /** The event webapp listeners. */
   private final ArrayList<WebAppListener> webAppListeners = new ArrayList<WebAppListener>();

   /** The event authentication Listeners. */
   private final List<AuthenticationListener> authenticationListeners = new CopyOnWriteArrayList<AuthenticationListener>();

   /** The web applications. */
   private final Map<String, WebAppImpl> webAppMap = new HashMap<String, WebAppImpl>();

   /** The callback. */
   private RegistrationImpl registration;

   /** The monitored contexts which were manually added. */
   private Map<String, String> manualMonitoredContexts = new HashMap<String, String>();

   /**
    * Manually register a webapp with this ServletContainerContext.
    *
    * @param webappContext the WebAppContext associated with the application
    * @param dispatchPath the path to be used
    */
   public void registerWebApp(WebAppContext webappContext, String dispatchPath)
   {
      if (isDisabledNativeRegistration(webappContext.getServletContext()))
      {
         this.manualMonitoredContexts.put(webappContext.getServletContext().getServletContextName(), dispatchPath);
         registration.registerWebApp(webappContext);
      }
   }

   /**
    * Manually unregister a webapp associated with this ServletContainerContext.
    *
    * @param servletContext the servletContext of the application to be deregistered
    */
   public void unregisterWebApp(ServletContext servletContext)
   {
      if (isDisabledNativeRegistration(servletContext))
      {
         this.manualMonitoredContexts.remove(servletContext.getServletContextName());
         //if the registration is null, then this ServletContainerContext has been stopped already
         //and all the registrations have already been removed.
         if (registration != null)
         {
            registration.unregisterWebApp(servletContext.getContextPath());
         }
      }
   }

   public static boolean isDisabledNativeRegistration(ServletContext servletContext)
   {
      if (servletContext != null)
      {
         String disableWCINativeRegistration = servletContext.getInitParameter(GateInServlet.WCIDISABLENATIVEREGISTRATION);
         return disableWCINativeRegistration != null && disableWCINativeRegistration.equalsIgnoreCase("true");
      }
      else
      {
         return false;
      }
   }

   /**
    * Register a servlet container context. The registration is considered as successful if no existing context is
    * already registered.
    *
    * @param context the servlet container context to register
    * @throws IllegalArgumentException if the context is null
    */
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

   /**
    * Authentication support.
    *
    * @param request the request valid in the current servlet context
    * @param response the response valid in the current servlet context
    * @param credentials the credentials which try to authenticate
    * @throws org.gatein.wci.authentication.AuthenticationException when authentication fails
    * @throws IllegalStateException when the user is already authenticated
    * @throws ServletException any servlet exception that would prevent authentication to be performed
    * @throws IOException any io exception that would prevent authentication to be performed
    */
   public void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials) throws ServletException, IOException
   {
      String remoteUser = request.getRemoteUser();
      if (remoteUser  == null)
      {
         try
         {
            registration.context.login(request, response, credentials);
            fireEvent(new AuthenticationEvent(AuthenticationEventType.LOGIN,
                  request, response, credentials.getUsername(), credentials, registration.context));
         }
         catch (AuthenticationException ae)
         {
            fireEvent(new AuthenticationEvent(AuthenticationEventType.FAILED,
                  request, response, credentials.getUsername(), credentials, registration.context));
            throw ae;
         }
      }
      else
      {
         throw new IllegalStateException("User already authenticated");
      }
   }

   /**
    * Authentication support.
    *
    * @param request the request valid in the current servlet context
    * @param response the response valid in the current servlet context
    * @throws IllegalStateException when the user is not authenticated
    */
   public void logout(HttpServletRequest request, HttpServletResponse response)
   {
      String remoteUser = request.getRemoteUser();
      if (remoteUser != null)
      {
         try
         {
            registration.context.logout(request, response);
         }
         catch (ServletException ignore)
         {
         }
      }
      else
      {
         throw new IllegalStateException("User is not authenticated");
      }
   }

   /**
    * Add the authentication listener.
    *
    * @param listener AuthenticationListener to add
    */
   public void addAuthenticationListener(AuthenticationListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("listener is null");
      }

      authenticationListeners.add(listener);
   }

   /**
    * Remove the authentication listener.
    *
    * @param listener AuthenticationListener to remove
    */
   public void removeAuthenticationlistener(AuthenticationListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("listener is null");
      }

      authenticationListeners.remove(listener);
   }

   /**
    * Returns the name and version of the servlet container in which the
    * context is running.
    *
    * <P>
    * The form of the returned string is <code>containername/versionnumber</code>.
    *
    *
    * @return   the string containing at least name and version number
    */
   public String getContainerInfo()
   {
      return registration.context.getContainerInfo();
   }

   /**
    * Add a web listener.
    *
    * @param listener the listener
    * @return true if the listener has been added
    */
   public boolean addWebAppListener(WebAppListener listener)
   {
      synchronized (lock)
      {
         if (listener == null)
         {
            throw new IllegalArgumentException();
         }
         if (webAppListeners.contains(listener))
         {
            return false;
         }
         webAppListeners.add(listener);
         for (Object response : webAppMap.values())
         {
            WebApp webApp = (WebApp)response;
            WebAppLifeCycleEvent event = new WebAppLifeCycleEvent(webApp, WebAppLifeCycleEvent.ADDED);
            safeFireEvent(listener, event);
         }
         return true;
      }
   }

   /**
    * Removes a web listener.
    *
    * @param listener the listener
    * @return true if the listener has been removed
    */
   public boolean removeWebAppListener(WebAppListener listener)
   {
      synchronized (lock)
      {
         if (listener == null)
         {
            throw new IllegalArgumentException();
         }
         if (webAppListeners.remove(listener))
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
      catch (Throwable t)
      {
         if (t instanceof Error)
         {
            throw (Error)t;
         }
         else
         {
            t.printStackTrace();
         }
      }
   }

   private void fireEvent(WebAppEvent event)
   {
      for (WebAppListener listener : webAppListeners)
      {
         safeFireEvent(listener, event);
      }
   }

   public void fireEvent(AuthenticationEvent event)
   {
      for (AuthenticationListener currentListener : authenticationListeners)
      {
         try
         {
            currentListener.onEvent(event);
         }
         catch (Exception ignore)
         {
            ignore.printStackTrace();
         }
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
    * @throws ServletException any servlet exception
    * @throws IOException any io exception
    */
   public Object include(
      ServletContext targetServletContext,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestDispatchCallback callback,
      Object handback) throws ServletException, IOException
   {
      if (manualMonitoredContexts.containsKey(targetServletContext.getServletContextName()))
      {
         String dispatherPath = manualMonitoredContexts.get(targetServletContext.getServletContextName());
         CommandDispatcher dispatcher = new CommandDispatcher(dispatherPath);
         return dispatcher.include(targetServletContext, request, response, callback, handback);
      }
      else
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
   }

   /**
    * Visit the registered WebApps
    *
    * @param visitor ServletContainerVisitor instance
    */
   public void visit(ServletContainerVisitor visitor)
   {
      synchronized (lock)
      {
         for (WebApp webApp: webAppMap.values())
         {
            visitor.accept(webApp);
         }
      }
   }

   private static class RegistrationImpl implements ServletContainerContext.Registration
   {

      /** . */
      private boolean disposed;

      /** . */
      private ServletContainer container;

      /** . */
      private ServletContainerContext context;

      public RegistrationImpl(ServletContainer container, ServletContainerContext context)
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

         if (webAppId == null)
         {
            throw new IllegalArgumentException("No null web app id accepted");
         }

         WebAppImpl webApp = container.webAppMap.get(webAppId);
         if (webApp == null)
         {
            log.debug("Web application " + webAppId + " was not registered");
            return false;
         }
         // lock context before locking container to prevent deadlocks
         synchronized (webApp.context)
         {
            synchronized (container.lock)
            {
               webApp = container.webAppMap.remove(webAppId);
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

      public boolean invalidateSession(String sessId)
      {
         return context.invalidateSession(sessId);
      }

      @Override
      public void fireRequestInitialized(ServletRequest servletRequest)
      {
         context.fireRequestInitialized(servletRequest);
      }

      @Override
      public void fireRequestDestroyed(ServletRequest servletRequest)
      {
         context.fireRequestDestroyed(servletRequest);
      }
   }
}
