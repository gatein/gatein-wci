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

import org.apache.catalina.Engine;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardServer;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class CatalinaIntegration
{
   private static final Logger log = LoggerFactory.getLogger(CatalinaIntegration.class);

   /**
    * System property name that can be used to turn off cross-context logout
    */
   private static final String CROSS_CONTEXT_LOGOUT_KEY = "org.gatein.wci.cross_context_logout";

   private StandardServer server;

   protected final AtomicBoolean start = new AtomicBoolean(false);

   private LinkedHashMap<Engine, JB7ServletContainerContext> containerContexts = new LinkedHashMap<Engine, JB7ServletContainerContext>();

   public CatalinaIntegration(StandardServer server)
   {
      this.server = server;
   }

   public void start()
   {
      if (start.compareAndSet(false, true))
      {
         start(server);
      }
   }

   private void start(Server server)
   {
      List<Service> services = Arrays.asList(server.findServices());

      for (Service service : services)
      {
         Engine engine = (Engine) service.getContainer();
         JB7ServletContainerContext containerContext = new JB7ServletContainerContext(engine);
         containerContext.setCrossContextLogout(getCrossContextLogoutConfig());
         containerContext.start();
         containerContexts.put(engine, containerContext);
      }
   }

   public void stop()
   {
      if (start.compareAndSet(true, false))
      {
         stop(server);
      }
   }

   private void stop(Server server)
   {
      List<Service> services = Arrays.asList(server.findServices());

      for (Service service : services)
      {
         Engine engine = (Engine) service.getContainer();
         JB7ServletContainerContext containerContext = containerContexts.get(engine);
         containerContext.stop();
         containerContexts.remove(engine);
      }
   }

   private boolean getCrossContextLogoutConfig()
   {
      String val = System.getProperty(CROSS_CONTEXT_LOGOUT_KEY);
      if (val == null || Boolean.valueOf(val))
      {
         return true;
      }

      if (!"false".equalsIgnoreCase(val))
      {
         log.warn("System property " + CROSS_CONTEXT_LOGOUT_KEY + " value is invalid: [" + val + "] - falling back to: [false]");
      }

      log.info("Cross-context session invalidation on logout disabled");
      return false;
   }
}
