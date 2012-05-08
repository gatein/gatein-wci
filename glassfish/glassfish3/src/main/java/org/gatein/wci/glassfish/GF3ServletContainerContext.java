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
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.ServletContainerVisitor;
import org.gatein.wci.WebApp;
import org.gatein.wci.api.GateInServlet;
import org.gatein.wci.authentication.GenericAuthentication;
import org.gatein.wci.command.CommandDispatcher;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.spi.ServletContainerContext;
import org.gatein.wci.spi.WebAppContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 4/13/12
 */
public class GF3ServletContainerContext implements ServletContainerContext, ContainerListener, LifecycleListener
{

   private final CommandDispatcher dispatcher = new CommandDispatcher("/tomcatgateinservlet");

   private final Set<String> monitoredHosts = new HashSet<String>();

   private final Set<String> monitoredContexts = new HashSet<String>();

   private static HashMap<String, String> manualMonitoredContexts = new HashMap<String, String>();

   private final Engine engine;

   private Registration registration;

   private boolean crossContextLogout = true;

   private GenericAuthentication authentication = new GenericAuthentication();

   public GF3ServletContainerContext(Engine engine)
   {
      this.engine = engine;
   }

   public Object include(ServletContext targetServletContext, HttpServletRequest request, HttpServletResponse response, RequestDispatchCallback callback, Object handback) throws ServletException, IOException
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
      authentication.login(credentials, request, response, validityMillis);
   }

   public void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials, long l, String initialURI) throws ServletException, IOException
   {
      authentication.login(credentials, request, response, l, initialURI);
   }

   public void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException
   {
      HttpSession sess = request.getSession(false);

      if (sess == null)
         return;

      sess.invalidate();

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
      return "GlassFish/3.x";
   }

   public synchronized void containerEvent(ContainerEvent event)
   {
      if (event.getData() instanceof Host)
      {
         Host host = (Host)event.getData();
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
         StandardContext context = (StandardContext)event.getData();
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

   private void registerHost(Host host)
   {
      if (!monitoredHosts.contains(host.getName()))
      {
         Container[] childrenContainers = host.findChildren();
         for (Container childContainer : childrenContainers)
         {
            if (childContainer instanceof StandardContext)
            {
               StandardContext context = (StandardContext)childContainer;
               registerContext(context);
            }
         }
         host.addContainerListener(this);
         monitoredHosts.add(host.getName());
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
               StandardContext context = (StandardContext)childContainer;
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
         GF3WebAppContext webAppContext = new GF3WebAppContext(context);
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

   public void lifecycleEvent(LifecycleEvent event) throws LifecycleException
   {
      if (event.getSource() instanceof Context)
      {
         Context context = (Context)event.getSource();
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
      Container[] childrenContainers = engine.findChildren();
      for (Container childContainer : childrenContainers)
      {
         if (childContainer instanceof Host)
         {
            Host host = (Host)childContainer;
            registerHost(host);
         }
      }
      engine.addContainerListener(this);
   }

   void stop()
   {
      engine.removeContainerListener(this);
      Container[] childrenContainers = engine.findChildren();
      for (Container childContainer : childrenContainers)
      {
         if (childContainer instanceof Host)
         {
            Host host = (Host)childContainer;
            unregisterHost(host);
         }
      }
      registration.cancel();
      registration = null;
   }

   public void registerWebApp(WebAppContext webappContext, String dispatchPath)
   {
      if (isDisabledNativeRegistration(webappContext.getServletContext()))
      {
         this.manualMonitoredContexts.put(webappContext.getServletContext().getServletContextName(), dispatchPath);
         registration.registerWebApp(webappContext);
      }
   }

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
}
