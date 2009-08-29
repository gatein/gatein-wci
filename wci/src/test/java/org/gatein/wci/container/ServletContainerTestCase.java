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
package org.gatein.wci.container;

import org.gatein.wci.WebAppListener;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.impl.DefaultServletContainer;
import org.gatein.wci.spi.ServletContainerContext;
import org.gatein.common.util.Tools;
import org.gatein.wci.WebAppRegistry;
import org.jboss.unit.api.pojo.annotations.Test;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

import static org.jboss.unit.api.Assert.*;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
@Test
public class ServletContainerTestCase
{

   @Test
   public void testContextRegistrationLifeCycle()
   {
      ServletContainer container = new DefaultServletContainer();
      ServletContainerContextImpl scc = new ServletContainerContextImpl();

      //
      container.register(scc);

      // Assert we got registration
      assertNotNull(scc.registration);

      // Keep a ref on registration
      ServletContainerContext.Registration registration = scc.registration;

      // Cancel registration
      scc.registration.cancel();

      // Assert we don't have registration anymore
      assertNull(scc.registration);

      // Test registration object is invalid
      try
      {
         registration.registerWebApp(new WebAppContextImpl("/foo"));
         fail("Was expecting an ISE");
      }
      catch (IllegalStateException ignore)
      {
      }
      try
      {
         registration.unregisterWebApp("/blah");
         fail("Was expecting an ISE");
      }
      catch (IllegalStateException ignore)
      {
      }
      try
      {
         registration.cancel();
         fail("Was expecting an ISE");
      }
      catch (IllegalStateException ignore)
      {
      }
   }

   @Test
   public void testConcurrentContextRegistrations()
   {
      ServletContainer container = new DefaultServletContainer();
      ServletContainerContextImpl scc1 = new ServletContainerContextImpl();
      ServletContainerContextImpl scc2 = new ServletContainerContextImpl();

      // We register
      container.register(scc1);

      // Registration was done
      assertNotNull(scc1.registration);

      // Try register
      container.register(scc2);

      // Registration failed
      assertNull(scc2.registration);

      // Cancel
      scc1.registration.cancel();

      // Try register again
      container.register(scc2);

      // Registration should have worked now
      assertNotNull(scc2.registration);
   }

   @Test
   public void testContextRegistrationCancellationUnregistersWebApps()
   {
      ServletContainer container = new DefaultServletContainer();
      ServletContainerContextImpl scc = new ServletContainerContextImpl();
      WebAppRegistry registry = new WebAppRegistry();

      //
      container.register(scc);

      //
      container.addWebAppListener(registry);

      //
      scc.registration.registerWebApp(new WebAppContextImpl("/foo"));
      scc.registration.registerWebApp(new WebAppContextImpl("/bar"));
      assertEquals(Tools.toSet("/foo", "/bar"), registry.getKeys());

      //
      scc.registration.cancel();
      assertEquals(Tools.toSet(), registry.getKeys());
   }

   @Test
   public void testListenerDoubleRegistration()
   {
      ServletContainer container = new DefaultServletContainer();
      ServletContainerContextImpl scc = new ServletContainerContextImpl();
      WebAppRegistry registry = new WebAppRegistry();

      //
      container.addWebAppListener(registry);
      container.addWebAppListener(registry);

      //
      container.register(scc);
      scc.registration.registerWebApp(new WebAppContextImpl("/foo"));
      assertEquals(Tools.toSet("/foo"), registry.getKeys());

      //
      container.addWebAppListener(registry);
      assertEquals(Tools.toSet("/foo"), registry.getKeys());

      //
      container.removeWebAppListener(registry);
      assertEquals(Tools.toSet(), registry.getKeys());

      //
      container.removeWebAppListener(registry);
      assertEquals(Tools.toSet(), registry.getKeys());
   }

   @Test
   public void testListenerIsNotified()
   {
      ServletContainer container = new DefaultServletContainer();
      ServletContainerContextImpl scc = new ServletContainerContextImpl();
      WebAppRegistry registry = new WebAppRegistry();

      //
      container.register(scc);

      // Add 2 web apps
      scc.registration.registerWebApp(new WebAppContextImpl("/foo"));
      scc.registration.registerWebApp(new WebAppContextImpl("/bar"));

      // Add listener
      container.addWebAppListener(registry);

      // Assert we received events during the registration
      assertEquals(Tools.toSet("/foo", "/bar"), registry.getKeys());

      // Add a new web app
      scc.registration.registerWebApp(new WebAppContextImpl("/juu"));

      // Assert we now have 3 web apps
      assertEquals(Tools.toSet("/foo", "/bar", "/juu"), registry.getKeys());

      // Remove one web app
      scc.registration.unregisterWebApp("/foo");

      // Assert we have 2 web apps
      assertEquals(Tools.toSet("/bar", "/juu"), registry.getKeys());

      // Remove registration
      container.removeWebAppListener(registry);

      // Assert we receveived events during removal
      assertEquals(Tools.toSet(), registry.getKeys());

      // W Add a new web app
      scc.registration.registerWebApp(new WebAppContextImpl("/foo"));

      // hen unregistered, a new web app registration does not send event
      assertEquals(Tools.toSet(), registry.getKeys());
   }

   @Test
   public void testServletContainerThrowsIAE()
   {
      ServletContainer container = new DefaultServletContainer();
      try
      {
         container.register(null);
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException ignore)
      {
      }
      try
      {
         container.addWebAppListener(null);
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException ignore)
      {
      }
      try
      {
         container.removeWebAppListener(null);
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException ignore)
      {
      }
   }

   @Test
   public void testServletContainerThrowsISE() throws Exception
   {
      ServletContainer container = new DefaultServletContainer();
      try
      {
         container.include(null, null, null, null, null);
         fail("Was expecting an ISE");
      }
      catch (IllegalStateException ignore)
      {
      }
   }

   @Test
   public void testListenerFailure()
   {
      ServletContainer container = new DefaultServletContainer();
      ServletContainerContextImpl scc = new ServletContainerContextImpl();
      WebAppRegistry registry = new WebAppRegistry();

      //
      final SynchronizedBoolean called = new SynchronizedBoolean(false);
      container.register(scc);
      container.addWebAppListener(registry);
      container.addWebAppListener(new WebAppListener()
      {
         public void onEvent(WebAppEvent event)
         {
            called.set(true);
            throw new RuntimeException("Expected : don't freak out");
         }
      });

      //
      scc.registration.registerWebApp(new WebAppContextImpl("/foo"));
      assertTrue(called.get());
      assertEquals(Tools.toSet("/foo"), registry.getKeys());
   }
}
