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
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.ServletContainerVisitor;
import org.gatein.wci.WebApp;
import org.gatein.wci.authentication.AuthenticationException;
import org.gatein.wci.command.CommandDispatcher;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.session.SessionTask;
import org.gatein.wci.session.SessionTaskVisitor;
import org.gatein.wci.spi.ServletContainerContext;
import java.io.IOException;
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

   private final Engine engine;

   private Registration registration;

   private boolean crossContextLogout = true;

   public GF3ServletContainerContext(Engine engine)
   {
      this.engine = engine;
   }

   public Object include(ServletContext targetServletContext, HttpServletRequest request, HttpServletResponse response, RequestDispatchCallback callback, Object handback) throws ServletException, IOException
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

   @Override
   public void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials) throws AuthenticationException, ServletException, IOException
   {
      request.getSession();
      try
      {
         request.login(credentials.getUsername(), credentials.getPassword());
      }
      catch (ServletException se)
      {
         throw new AuthenticationException(se);
      }
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
      ServletContainerFactory.getServletContainer().visit(new SessionTaskVisitor(sessId, new SessionTask()
      {
         @Override
         public boolean executeTask(HttpSession session)
         {
            ClassLoader portalContainerCL = Thread.currentThread().getContextClassLoader();
            ClassLoader webAppCL = session.getServletContext().getClassLoader();

            Thread.currentThread().setContextClassLoader(webAppCL);
            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } finally {
                Thread.currentThread().setContextClassLoader(portalContainerCL);
            }

            return true;
         }

      }));
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
         if (!ServletContainer.isDisabledNativeRegistration(context.getServletContext()))
         {
            GF3WebAppContext webAppContext = new GF3WebAppContext(context);
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
         if (!ServletContainer.isDisabledNativeRegistration(context.getServletContext()))
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
      ServletContainerFactory.registerContext(this);
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
}
