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
import org.gatein.wci.authentication.AuthenticationException;
import org.gatein.wci.authentication.AuthenticationListener;
import org.gatein.wci.authentication.GenericAuthentication;
import org.gatein.wci.authentication.TicketService;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.gatein.wci.security.WCIController;
import org.jboss.unit.Failure;
import org.jboss.unit.driver.DriverCommand;
import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.response.EndTestResponse;
import org.jboss.unit.driver.response.FailureResponse;
import org.jboss.unit.remote.driver.handler.http.response.InvokeGetResponse;

import static org.jboss.unit.api.Assert.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SPIAuthenticationTestCase extends ServletTestCase
{

   /** . */
   private ServletContainer container;

   /** . */
   private final Value v = new Value();

   /** . */
   private WCIController wciController = new TestController();

   @Override
   public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
   {
      Credentials credentials = wciController.getCredentials(req, resp);
      
      if (getRequestCount() == 0)
      {
         assertEquals("/home", wciController.getInitialURI(req));
         req.setAttribute("javax.servlet.forward.request_uri", "/foo");
         assertEquals("/foo", wciController.getInitialURI(req));

         // Test Ticket Expiration
         String expireTicket = GenericAuthentication.TICKET_SERVICE.createTicket(new Credentials("foo", "bar"), 5);
         boolean expired = false;
         try
         {
            Thread.sleep(5);
            GenericAuthentication.TICKET_SERVICE.validateTicket(expireTicket, true);
         }
         catch (InterruptedException ignore)
         {
         }
         catch (AuthenticationException ae)
         {
            expired = true;
         }
         if (!expired) return new FailureResponse(Failure.createAssertionFailure(""));

         assertNull(req.getUserPrincipal());
         container = DefaultServletContainerFactory.getInstance().getServletContainer();
         container.addAuthenticationListener(new TestListener(v));
         assertEquals("", v.value);
         container.login(req, resp, credentials, TicketService.DEFAULT_VALIDITY);

         if ("Tomcat/7.x".equals(container.getContainerInfo()) || "JBossas/6.x".equals(container.getContainerInfo()))
         {
            assertEquals("login", v.value);
            assertNotNull(req.getUserPrincipal());
            assertTrue(req.isUserInRole("test"));
         }
         else
         {
            // Test Ticket Service
            String ticket = GenericAuthentication.TICKET_SERVICE.createTicket(credentials, TicketService.DEFAULT_VALIDITY);
            Credentials resultCredentials = GenericAuthentication.TICKET_SERVICE.validateTicket(ticket, false);
            assertEquals(credentials.getUsername(), resultCredentials.getUsername());
            assertEquals(credentials.getPassword(), resultCredentials.getPassword());
            assertNotNull(GenericAuthentication.TICKET_SERVICE.validateTicket(ticket, true));
            assertNull(GenericAuthentication.TICKET_SERVICE.validateTicket(ticket, true));

            // Test login Event
            assertEquals("login", v.value);
            assertTrue(resp.isCommitted());
         }

         //
         Map<String, String[]> params = new HashMap<String, String[]>();
         params.put("initialURI", new String[]{"/bar"});
         String url = resp.renderURL("/", params, null);
         return new InvokeGetResponse(url);
      }
      else if (getRequestCount() == 1)
      {
         assertEquals("/bar", wciController.getInitialURI(req));

         if ("Tomcat/7.x".equals(container.getContainerInfo()) || "JBossas/6.x".equals(container.getContainerInfo()))
         {
            assertEquals("login", v.value);

            container.logout(req, resp);

            assertEquals("logout", v.value);
            assertNull(req.getUserPrincipal());
         }
         else
         {
            // Test logout
            assertEquals("login", v.value);
            container.logout(req, resp);
            assertNull(req.getSession(false));

            // Test logout Event
            assertEquals("logout", v.value);
         }
         
         String url = resp.renderURL("/", null, null);
         return new InvokeGetResponse(url);
      }
      else if (getRequestCount() == 2)
      {
         assertEquals(
                 "/home/j_security_check?j_username=foo&j_password=bar",
                 wciController.getAuthURI(req, resp, credentials.getUsername(), credentials.getPassword())
                 );
         wciController.sendAuth(req, resp, credentials.getUsername(), credentials.getPassword());
         assertTrue(resp.isCommitted());
         return new EndTestResponse();
      }

      return new FailureResponse(Failure.createAssertionFailure("End test reached"));
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
