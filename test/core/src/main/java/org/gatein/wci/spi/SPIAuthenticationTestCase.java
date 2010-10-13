/*
* Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.gatein.wci.spi;

import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletTestCase;
import org.gatein.wci.TestServlet;
import org.gatein.wci.WebRequest;
import org.gatein.wci.WebResponse;
import org.gatein.wci.authentication.AuthenticationEvent;
import org.gatein.wci.authentication.AuthenticationListener;
import org.gatein.wci.authentication.AuthenticationResult;
import org.gatein.wci.authentication.GenericAuthentication;
import org.gatein.wci.authentication.GenericAuthenticationResult;
import org.gatein.wci.authentication.ProgrammaticAuthenticationResult;
import org.gatein.wci.authentication.TicketService;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.jboss.unit.Failure;
import org.jboss.unit.driver.DriverCommand;
import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.response.EndTestResponse;
import org.jboss.unit.driver.response.FailureResponse;
import org.jboss.unit.remote.driver.handler.http.response.InvokeGetResponse;

import static org.jboss.unit.api.Assert.*;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SPIAuthenticationTestCase extends ServletTestCase
{
   private final String username = "foo";
   private final String password = "bar";

   /** . */
   private ServletContainer container;

   /** . */
   private final Value v = new Value();

   /** . */
   private AuthenticationResult result;

   @Override
   public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
   {
      if (getRequestCount() == 0)
      {
         assertNull(req.getUserPrincipal());
         container = DefaultServletContainerFactory.getInstance().getServletContainer();
         container.addAuthenticationListener(new TestListener(v));
         assertEquals("", v.value);
         result = container.login(req, resp, username, password, TicketService.DEFAULT_VALIDITY);
         assertNotNull(result);
         if (result instanceof GenericAuthenticationResult)
         {
            GenericAuthenticationResult gAuthentication = (GenericAuthenticationResult) result;
            // Test Ticket Service
            Credentials srcCredentials = new Credentials(username, password);
            String ticket = GenericAuthentication.TICKET_SERVICE.createTicket(srcCredentials);
            Credentials resultCredentials = GenericAuthentication.TICKET_SERVICE.validateTicket(ticket, false);
            assertEquals(srcCredentials.getUsername(), resultCredentials.getUsername());
            assertEquals(srcCredentials.getPassword(), resultCredentials.getPassword());
            assertNotNull(GenericAuthentication.TICKET_SERVICE.validateTicket(ticket, true));
            assertNull(GenericAuthentication.TICKET_SERVICE.validateTicket(ticket, true));

            // Test Generic login
            GenericAuthenticationResult gResult = (GenericAuthenticationResult) result;
            String t = gResult.getTicket();
            Credentials credentials = GenericAuthentication.TICKET_SERVICE.validateTicket(t, true);
            assertNotNull(credentials);
            assertEquals("", v.value);
            gAuthentication.perform(req, resp);

            // Test login Event
            assertEquals("login", v.value);
            assertTrue(resp.isCommitted());
            
         }
         else if (result instanceof ProgrammaticAuthenticationResult)
         {
            assertEquals("login", v.value);
            assertNotNull(req.getUserPrincipal());
            assertTrue(req.isUserInRole("test"));
         }

         //
         String url = resp.renderURL("/", null, null);
         return new InvokeGetResponse(url);
      }
      else if (getRequestCount() == 1)
      {
         if (result instanceof GenericAuthenticationResult)
         {
            // Test logout
            assertNotNull(req.getSession(false));
            assertEquals("login", v.value);
            container.logout(req, resp);
            assertNull(req.getSession(false));

            // Test logout Event
            assertEquals("logout", v.value);
         }
         else if (result instanceof ProgrammaticAuthenticationResult)
         {
            assertEquals("login", v.value);

            container.logout(req, resp);

            assertEquals("logout", v.value);
            assertNull(req.getUserPrincipal());
         }
         return new EndTestResponse();
      }

      return new FailureResponse(Failure.createAssertionFailure(""));
   }

   @Override
   public DriverResponse invoke(TestServlet testServlet, DriverCommand driverCommand)
   {
      if (getRequestCount() == -1)
      {
         return new InvokeGetResponse("/test-spi-server");
      }
      else
      {
         return new FailureResponse(Failure.createAssertionFailure(""));
      }
   }

   class Value
   {
      public String value = "";
   }

   public static class TestListener implements AuthenticationListener
   {
      private Value value;

      public TestListener(Value value) {
         this.value = value;
      }

      public void onLogin(AuthenticationEvent ae)
      {
         value.value = "login";
      }

      public void onLogout(AuthenticationEvent ae)
      {
         value.value = "logout";
      }
   }
}
