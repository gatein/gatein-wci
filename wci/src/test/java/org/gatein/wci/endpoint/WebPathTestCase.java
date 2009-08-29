/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2008, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wci.endpoint;

import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.DriverCommand;
import org.jboss.unit.driver.response.FailureResponse;
import org.jboss.unit.driver.response.EndTestResponse;
import org.jboss.unit.remote.driver.handler.http.response.InvokeGetResponse;
import org.jboss.unit.Failure;
import static org.jboss.unit.api.Assert.*;
import org.gatein.wci.TestServlet;
import org.gatein.wci.WebRequest;
import org.gatein.wci.WebResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author <a href="mailto:julien@jboss-portal.org">Julien Viet</a>
 * @version $Revision: 630 $
 */
public class WebPathTestCase extends EndPointTestCase
{

   public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
   {
      String requestURI = req.getContextPath() + req.getServletPath() + (req.getPathInfo() != null ? req.getPathInfo() : "");
      String webURI = req.getWebContextPath() + req.getWebRequestPath();
      assertTrue("At interaction " + getRequestCount() + " expected " + requestURI + " to be a prefix of " + webURI, webURI.startsWith(requestURI));

      //
      if (getRequestCount() == 0)
      {
         assertEquals("/", req.getWebRequestPath());
         return new InvokeGetResponse(rewriteURL(testServlet, "/"));
      }
      else if (getRequestCount() == 1)
      {
         assertEquals("/", req.getWebRequestPath());
         return new InvokeGetResponse(rewriteURL(testServlet, "/bar"));
      }
      else if (getRequestCount() == 2)
      {
         assertEquals("/bar", req.getWebRequestPath());
         return new EndTestResponse();
      }

      //
      return new FailureResponse(Failure.createAssertionFailure(""));
   }

   public DriverResponse invoke(TestServlet testServlet, DriverCommand driverCommand)
   {
      if (getRequestCount() == -1)
      {
         return new InvokeGetResponse(rewriteURL(testServlet, ""));
      }
      else
      {
         return new FailureResponse(Failure.createAssertionFailure(""));
      }
   }
}
