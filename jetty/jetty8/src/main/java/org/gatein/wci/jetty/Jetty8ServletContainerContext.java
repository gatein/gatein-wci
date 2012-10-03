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
package org.gatein.wci.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.Container;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.ServletContainerVisitor;
import org.gatein.wci.WebApp;
import org.gatein.wci.authentication.AuthenticationException;
import org.gatein.wci.command.CommandDispatcher;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.spi.ServletContainerContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 9/24/12
 */
public class Jetty8ServletContainerContext implements ServletContainerContext, Container.Listener, LifeCycle.Listener
{
   private final static Logger LOG = LoggerFactory.getLogger(Jetty8ServletContainerContext.class);

   private final Container container;

   private final CommandDispatcher dispatcher = new CommandDispatcher("/jetty8gateinservlet");

   private boolean crossContextLogout = true;

   private Registration registration;

   private final Map<String, String> manualMonitoredContexts = new HashMap<String, String>();

   private final Set<String> monitoredContexts = new HashSet<String>();

   private final String containerInfo = "Jetty " + Server.getVersion();

   public Jetty8ServletContainerContext(Server server)
   {
      this.container = server.getContainer();
   }

   public void start()
   {
      ServletContainerFactory.registerContext(this);
      container.addEventListener(this);
   }

   public void stop()
   {
      container.removeEventListener(this);
      if (registration != null)
      {
         registration.cancel();
         registration = null;
      }
   }


   @Override
   public Object include(ServletContext targetServletContext, HttpServletRequest request, HttpServletResponse response, RequestDispatchCallback callback, Object handback) throws ServletException, IOException
   {
      if (manualMonitoredContexts.containsKey(targetServletContext.getServletContextName()))
      {
         String customPath = manualMonitoredContexts.get(targetServletContext.getServletContextName());
         return new CommandDispatcher(customPath).include(targetServletContext, request, response, callback, handback);
      }
      else
      {
         return dispatcher.include(targetServletContext, request, response, callback, handback);
      }
   }

   @Override
   public void setCallback(Registration registration)
   {
      this.registration = registration;
   }

   @Override
   public void unsetCallback(Registration registration)
   {
      this.registration = null;
   }

   @Override
   public void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials) throws AuthenticationException, ServletException, IOException
   {
      request.getSession(true);
      try
      {
         request.login(credentials.getUsername(), credentials.getPassword());
      }
      catch (ServletException se)
      {
         throw new AuthenticationException(se);
      }
   }

   @Override
   public void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException
   {
      HttpSession sess = request.getSession(false);
      request.logout();

      if (sess == null)
      { return; }

      if (!crossContextLogout)
      { return; }

      final String sessId = sess.getId();
      ServletContainerFactory.getServletContainer().visit(new ServletContainerVisitor()
      {
         public void accept(WebApp webApp)
         {
            webApp.invalidateSession(sessId);
         }
      });
   }

   @Override
   public String getContainerInfo()
   {
      return containerInfo;
   }

   @Override
   public void addBean(Object o)
   {
      if (o instanceof WebAppContext)
      {
         registerWebAppContext((WebAppContext)o);
      }
   }

   private void registerWebAppContext(WebAppContext ctx)
   {
      if (!monitoredContexts.contains(ctx.getServletContext().getServletContextName()))
      {
         LOG.debug("Register lifecycle listener on webapp " + ctx.getContextPath());
         ctx.addLifeCycleListener(this);
         if (ctx.isStarted())
         {
            startWebAppContext(ctx);
         }
         monitoredContexts.add(ctx.getServletContext().getServletContextName());
      }
   }

   @Override
   public void removeBean(Object o)
   {
      if (o instanceof WebAppContext)
      {
         unregisterWebAppContext((WebAppContext)o);
      }
   }

   private void unregisterWebAppContext(WebAppContext ctx)
   {
      if (monitoredContexts.contains(ctx.getServletContext().getServletContextName()))
      {
         monitoredContexts.remove(ctx.getServletContext().getServletContextName());
         if (!ctx.isStopped())
         {
            stopWebAppContext(ctx);
         }
         LOG.debug("Unregister lifecycle listener on webapp " + ctx.getContextPath());
         ctx.removeLifeCycleListener(this);
      }

   }

   @Override
   public void add(Container.Relationship relationship)
   {
      addBean(relationship.getChild());
   }

   @Override
   public void remove(Container.Relationship relationship)
   {
      removeBean(relationship.getChild());
   }

   @Override
   public void lifeCycleStarting(LifeCycle lifeCycle)
   {
   }

   @Override
   public void lifeCycleStarted(LifeCycle lifeCycle)
   {
      startWebAppContext((WebAppContext)lifeCycle);
   }

   private void startWebAppContext(WebAppContext ctx)
   {
      try
      {
         if (!ServletContainer.isDisabledNativeRegistration(ctx.getServletContext()))
         {
            Jetty8WebAppContext jetty8Ctx = new Jetty8WebAppContext(ctx);
            if (registration != null)
            {
               registration.registerWebApp(jetty8Ctx);
            }
         }
      }
      catch (Exception ex)
      {
         LOG.warn("Failed to register webapp " + ctx.getContextPath(), ex);
      }
   }

   @Override
   public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable)
   {
   }

   @Override
   public void lifeCycleStopping(LifeCycle lifeCycle)
   {
   }

   @Override
   public void lifeCycleStopped(LifeCycle lifeCycle)
   {
      stopWebAppContext((WebAppContext)lifeCycle);
   }

   private void stopWebAppContext(WebAppContext ctx)
   {
      try
      {
         if (!ServletContainer.isDisabledNativeRegistration(ctx.getServletContext()))
         {
            if (registration != null)
            {
               registration.unregisterWebApp(ctx.getContextPath());
            }
         }
      }
      catch (Exception ex)
      {
         LOG.warn("Failed to unregister webapp " + ctx.getContextPath(), ex);
      }
   }
}
