/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.wci.endpoint;

import org.gatein.wci.TestServlet;
import org.gatein.wci.WebRequest;
import org.gatein.wci.WebResponse;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.security.WCILoginController;
import org.jboss.unit.Failure;
import org.jboss.unit.driver.DriverCommand;
import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.response.EndTestResponse;
import org.jboss.unit.driver.response.FailureResponse;
import org.jboss.unit.remote.driver.handler.http.response.InvokeGetResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.unit.api.Assert.assertEquals;
import static org.jboss.unit.api.Assert.assertNotNull;
import static org.jboss.unit.api.Assert.assertNull;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LoginControllerTestCase extends EndPointTestCase
{
   private final TestWCILoginController wciLoginController = new TestWCILoginController();

   @Override
   public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
   {
      if (getRequestCount() == 0)
      {
         // Test that credentials are not set
         assertNull("Credentials should be null before first invocation of WCILoginController", wciLoginController.getCredentials(req));

         // Processing request with wciLoginController
         wciLoginController.service(req, resp);

         // Test that credentials are set at this moment and are equalst to "root"/"gtn"
         Credentials credentials = wciLoginController.getCredentials(req);
         testCredentials(credentials, "root", "gtn");

         // Test that we can change credentials by invoke of setCredentials
         Credentials johnCredentials = new Credentials("john", "johnPassword");
         wciLoginController.setCredentials(req, johnCredentials);
         testCredentials(wciLoginController.getCredentials(req), "john", "johnPassword");

         // Use mary credentials for next request
         Map<String, String[]> params = new HashMap<String, String[]>();
         params.put("username", new String[]{"mary"});
         params.put("password", new String[]{"maryPassword"});
         String url = resp.renderURL("/", params, null);
         return new InvokeGetResponse(url);
      }
      else if (getRequestCount() == 1)
      {
         // Test that we still have credentials of john
         testCredentials(wciLoginController.getCredentials(req), "john", "johnPassword");

         // Test that we have credentials of mary after processing request
         wciLoginController.service(req, resp);
         testCredentials(wciLoginController.getCredentials(req), "mary", "maryPassword");

         return new EndTestResponse();
      }

      return new FailureResponse(Failure.createAssertionFailure("End test reached"));
   }

   @Override
   public DriverResponse invoke(TestServlet testServlet, DriverCommand driverCommand)
   {
      if (getRequestCount() == -1)
      {
         return new InvokeGetResponse(rewriteURL(testServlet, "/?username=root&password=gtn"));
      }
      else
      {
         return new FailureResponse(Failure.createAssertionFailure(""));
      }
   }

   private void testCredentials(Credentials credentials, String expectedUsername, String expectedPassword)
   {
      assertNotNull("Credentials should not be null", credentials);
      assertEquals(credentials.getUsername(), expectedUsername);
      assertEquals(credentials.getPassword(), expectedPassword);
   }

   // This subclass is needed for access to protected methods getCredentials and setCredentials of WCILoginController
   private class TestWCILoginController extends WCILoginController
   {

      @Override
      protected Credentials getCredentials(HttpServletRequest req)
      {
         return super.getCredentials(req);
      }

      @Override
      protected void setCredentials(HttpServletRequest req, Credentials credentials)
      {
         super.setCredentials(req, credentials);
      }
   }
}
