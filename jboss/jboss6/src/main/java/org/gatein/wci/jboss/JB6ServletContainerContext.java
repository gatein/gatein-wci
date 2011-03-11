/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
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
import org.gatein.wci.authentication.GenericAuthentication;
import org.gatein.wci.authentication.TicketService;
import org.gatein.wci.command.CommandDispatcher;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.spi.ServletContainerContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class JB6ServletContainerContext implements ServletContainerContext, ContainerListener, LifecycleListener
{
   private final static Logger log = LoggerFactory.getLogger(JB6ServletContainerContext.class);

   /** . */
   //TODO: maybe we should rename this to /jbossgateinservlet? but older versions of jboss
   //      use the tomcat ServletContainerContext.
   private final CommandDispatcher dispatcher = new CommandDispatcher("/tomcatgateinservlet");

   /** The monitored hosts. */
   private final Set<String> monitoredHosts = new HashSet<String>();

   /** The monitored contexts. */
   private final Set<String> monitoredContexts = new HashSet<String>();

   /** . */
   private final Engine engine;

   /** . */
   private Registration registration;

   /** Perform cross-context session invalidation on logout, or not */
   private boolean crossContextLogout = true;

   public JB6ServletContainerContext(Engine engine)
   {
      this.engine = engine;
   }

   public Object include(ServletContext targetServletContext, HttpServletRequest request, HttpServletResponse response,
         RequestDispatchCallback callback, Object handback) throws ServletException, IOException
         {
      return dispatcher.include(targetServletContext, request, response, callback, handback);
         }

   public void setCallback(Registration registration)
   {
      this.registration = registration;
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
         se.printStackTrace();
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
         return;

      if (!crossContextLogout)
         return;

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
      return "JBossas/6.x";
   }

   public synchronized void containerEvent(ContainerEvent event)
   {
      if (event.getData() instanceof Host)
      {
         Host host = (Host) event.getData();

         //
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

         //
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

         //
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

      //
      Container[] childrenContainers = engine.findChildren();
      for (Container childContainer : childrenContainers)
      {
         if (childContainer instanceof Host)
         {
            Host host = (Host) childContainer;
            registerHost(host);
         }
      }

      //
      engine.addContainerListener(this);
   }

   void stop()
   {
      engine.removeContainerListener(this);

      //
      Container[] childrenContainers = engine.findChildren();
      for (Container childContainer : childrenContainers)
      {
         if (childContainer instanceof Host)
         {
            Host host = (Host) childContainer;
            unregisterHost(host);
         }
      }

      //
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
         Container[] childrenContainers = host.findChildren();
         for (Container childContainer : childrenContainers)
         {
            if (childContainer instanceof StandardContext)
            {
               StandardContext context = (StandardContext) childContainer;
               registerContext(context);
            }
         }

         //
         host.addContainerListener(this);

         //
         monitoredHosts.add(host.getName());
      }
   }

   private void unregisterHost(Host host)
   {
      if (monitoredHosts.contains(host.getName()))
      {
         monitoredHosts.remove(host.getName());

         //
         host.removeContainerListener(this);

         //
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

         //
         if (context.getState() == 1)
         {
            start(context);
         }

         //
         monitoredContexts.add(context.getName());
      }
   }

   private void unregisterContext(StandardContext context)
   {
      if (monitoredContexts.contains(context.getName()))
      {
         monitoredContexts.remove(context.getName());

         //
         if (context.getState() == 1)
         {
            stop(context);
         }

         //
         context.removeLifecycleListener(this);
      }
   }

   private void start(Context context)
   {
      try
      {
         log.debug("Context added " + context.getPath());
         JB6WebAppContext webAppContext = new JB6WebAppContext(context);

         //
         if (registration != null)
         {
            registration.registerWebApp(webAppContext);
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
         if (registration != null)
         {
            registration.unregisterWebApp(context.getPath());
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
