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

import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.ServletContainerVisitor;
import org.gatein.wci.WebApp;
import org.gatein.wci.api.GateInServlet;
import org.gatein.wci.api.GateInServletRegistrations;
import org.gatein.wci.authentication.GenericAuthentication;
import org.gatein.wci.authentication.TicketService;
import org.gatein.wci.command.CommandDispatcher;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.spi.ServletContainerContext;
import org.gatein.wci.spi.WebAppContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class JB7ServletContainerContext implements ServletContainerContext, ContainerListener, LifecycleListener
{
   private final static Logger log = LoggerFactory.getLogger(JB7ServletContainerContext.class);

   /**
    * .
    */
   //TODO: maybe we should rename this to /jbossgateinservlet? but older versions of jboss
   //      use the tomcat ServletContainerContext.
   private final CommandDispatcher dispatcher = new CommandDispatcher("/tomcatgateinservlet");

   /**
    * The monitored hosts.
    */
   private final Set<String> monitoredHosts = new HashSet<String>();

   /**
    * The monitored contexts.
    */
   private final Set<String> monitoredContexts = new HashSet<String>();

   /**
    * The monitored contexts which were manually added.
    */
   private static Map<String, String> manualMonitoredContexts = new HashMap<String, String>();

   /**
    * .
    */
   private final Engine engine;

   /**
    * .
    */
   private Registration registration;

   /**
    * Perform cross-context session invalidation on logout, or not
    */
   private boolean crossContextLogout = true;

   /**
    * Used to perform 2-pass init in case any container events occur during first pass init
    */
   private int initCount = 1;


   public JB7ServletContainerContext(Engine engine)
   {
      this.engine = engine;
   }

   public Object include(ServletContext targetServletContext, HttpServletRequest request, HttpServletResponse response,
                         RequestDispatchCallback callback, Object handback) throws ServletException, IOException
   {
      if (manualMonitoredContexts.containsKey(targetServletContext.getServletContextName()))
      {
         String dispatherPath = manualMonitoredContexts.get(targetServletContext.getServletContextName());
         CommandDispatcher dispatcher = new CommandDispatcher(dispatherPath);
         return dispatcher.include(targetServletContext, request, response, callback, handback);
      }
      else
      {
         return dispatcher.include(targetServletContext, request, response, callback, handback);
      }
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

   public void setCrossContextLogout(boolean val)
   {
      crossContextLogout = val;
   }

   public void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials, long validityMillis) throws ServletException, IOException
   {
      login(request, response, credentials, validityMillis, null);
   }

   public void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials, long validityMillis, String initialURI) throws ServletException, IOException
   {
      if (initialURI == null)
      {
         initialURI = request.getRequestURI();
      }
      try
      {
         request.login(credentials.getUsername(), credentials.getPassword());
         response.sendRedirect(response.encodeRedirectURL(initialURI));
      }
      catch (ServletException se)
      {
         log.trace("login() exception: ", se);
         try
         {
            String ticket = GenericAuthentication.TICKET_SERVICE.createTicket(new Credentials(credentials.getUsername(), credentials.getUsername()),
               TicketService.DEFAULT_VALIDITY);
            String url = "j_security_check?j_username=" + credentials.getUsername() + "&j_password=" + ticket + "&initialURI=" + initialURI;
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
            response.flushBuffer();
         }
         catch (Exception ignore)
         {
         }
      }
   }


   public void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException
   {
      HttpSession sess = request.getSession(false);
      request.logout();

      if (sess == null)
      {
         return;
      }

      if (!crossContextLogout)
      {
         return;
      }

      final String sessId = sess.getId();
      DefaultServletContainerFactory.getInstance().getServletContainer().visit(new ServletContainerVisitor()
      {
         public void accept(WebApp webApp)
         {
            webApp.invalidateSession(sessId);
         }
      });
   }

   public String getContainerInfo()
   {
      return "JBossAS/7.x";
   }

   public synchronized void containerEvent(ContainerEvent event)
   {
      if (initCount > 0)
      {
         initCount++;
         return;
      }

      processContainerEvent(event);
   }

   private void processContainerEvent(ContainerEvent event)
   {
      if (event.getData() instanceof Host)
      {
         Host host = (Host) event.getData();

         if (Container.ADD_CHILD_EVENT.equals(event.getType()))
         {
            registerHost(host);
         }
         else if (Container.REMOVE_CHILD_EVENT.equals(event.getType()))
         {
            unregisterHost(host);
         }
      }
      else if (event.getData() instanceof StandardContext)
      {
         StandardContext context = (StandardContext) event.getData();

         if (Container.ADD_CHILD_EVENT.equals(event.getType()))
         {
            registerContext(context);
         }
         else if (Container.REMOVE_CHILD_EVENT.equals(event.getType()))
         {
            unregisterContext(context);
         }
      }
   }

   public void lifecycleEvent(LifecycleEvent event)
   {
      if (event.getSource() instanceof Context)
      {
         Context context = (Context) event.getSource();

         if (Lifecycle.AFTER_START_EVENT.equals(event.getType()))
         {
            start(context);
         }
         else if (Lifecycle.BEFORE_STOP_EVENT.equals(event.getType()))
         {
            stop(context);
         }
      }
   }

   void start()
   {
      DefaultServletContainerFactory.registerContext(this);
      engine.addContainerListener(this);

      boolean again = false;
      do
      {
         Container[] childrenContainers = engine.findChildren();
         for (Container childContainer : childrenContainers)
         {
            if (childContainer instanceof Host)
            {
               Host host = (Host) childContainer;
               registerHost(host);
            }
         }

         synchronized (this)
         {
            again = initCount > 1;
            initCount = again ? 1: 0;
         }
      } while (again);
   }

   void stop()
   {
      engine.removeContainerListener(this);

      Container[] childrenContainers = engine.findChildren();
      for (Container childContainer : childrenContainers)
      {
         if (childContainer instanceof Host)
         {
            Host host = (Host) childContainer;
            unregisterHost(host);
         }
      }

      registration.cancel();
      registration = null;
   }

   /**
    * Register an host for registration which means that we fire events for all the contexts it contains and we
    * subscribe for its life cycle events. If the host is already monitored nothing is done.
    *
    * @param host the host to register for monitoring
    */
   private void registerHost(Host host)
   {
      if (!monitoredHosts.contains(host.getName()))
      {
         host.addContainerListener(this);
         monitoredHosts.add(host.getName());
      }

      Container[] childrenContainers = host.findChildren();
      for (Container childContainer : childrenContainers)
      {
         if (childContainer instanceof StandardContext)
         {
            StandardContext context = (StandardContext) childContainer;
            registerContext(context);
         }
      }
   }

   private void unregisterHost(Host host)
   {
      if (monitoredHosts.contains(host.getName()))
      {
         monitoredHosts.remove(host.getName());
         host.removeContainerListener(this);

         Container[] childrenContainers = host.findChildren();
         for (Container childContainer : childrenContainers)
         {
            if (childContainer instanceof StandardContext)
            {
               StandardContext context = (StandardContext) childContainer;
               unregisterContext(context);
            }
         }
      }
   }

   private void registerContext(StandardContext context)
   {
      if (!monitoredContexts.contains(context.getName()))
      {
         context.addLifecycleListener(this);

         if (context.getState() == 1)
         {
            start(context);
         }
         monitoredContexts.add(context.getName());
      }
   }

   private void unregisterContext(StandardContext context)
   {
      if (monitoredContexts.contains(context.getName()))
      {
         monitoredContexts.remove(context.getName());

         if (context.getState() == 1)
         {
            stop(context);
         }
         context.removeLifecycleListener(this);
      }
   }

   private void start(Context context)
   {
      try
      {
         log.debug("Context added " + context.getPath());

         // skip if the webapp has explicitly stated it doesn't want native registration
         // usefull when portlets are dependent on servlet ordering
         if (isDisabledNativeRegistration(context.getServletContext()))
         {
            return;
         }

         // we need to preemptively synchronize on context to prevent deadlocks from
         // opposite order locking on jboss-as7
         synchronized (context)
         {
            JB7WebAppContext webAppContext = new JB7WebAppContext(context);

            if (registration != null)
            {
               registration.registerWebApp(webAppContext);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private void stop(Context context)
   {
      try
      {
         // skip if the webapp has explicitly stated it doesn't want native registration
         // usefull when portlets are dependent on servlet ordering
         if (isDisabledNativeRegistration(context.getServletContext()))
         {
            return;
         }

         // we need to preemptively synchronize on context to prevent deadlocks from
         // opposite order locking on jboss-as7
         synchronized (context)
         {
            if (registration != null)
            {
               registration.unregisterWebApp(context.getPath());
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private boolean isDisabledNativeRegistration(ServletContext servletContext)
   {
      if (servletContext != null)
      {
         String disableWCINativeRegistration = servletContext.getInitParameter(GateInServlet.WCIDISABLENATIVEREGISTRATION);
         if (disableWCINativeRegistration != null && disableWCINativeRegistration.equalsIgnoreCase("true"))
         {
            return true;
         }
         else
         {
            return false;
         }
      }
      else
      {
         return false;
      }
   }

   @Override
   public void registerWebApp(WebAppContext webappContext, String dispatchPath)
   {
      if (isDisabledNativeRegistration(webappContext.getServletContext()))
      {
         this.manualMonitoredContexts.put(webappContext.getServletContext().getServletContextName(), dispatchPath);
         registration.registerWebApp(webappContext);
      }
   }

   @Override
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
}
