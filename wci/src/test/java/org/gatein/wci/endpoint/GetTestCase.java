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
package org.gatein.wci.endpoint;

import org.gatein.wci.TestServlet;
import org.gatein.common.text.FastURLEncoder;
import org.gatein.wci.WebRequest;
import org.gatein.wci.IllegalRequestException;
import org.gatein.wci.WebResponse;
import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.DriverCommand;
import org.jboss.unit.driver.response.EndTestResponse;
import org.jboss.unit.driver.response.FailureResponse;

import static org.jboss.unit.api.Assert.*;
import org.jboss.unit.remote.driver.handler.http.response.InvokeGetResponse;
import org.jboss.unit.Failure;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class GetTestCase extends EndPointTestCase
{

   /** . */
   private final FastURLEncoder encoder = FastURLEncoder.getUTF8Instance();

   public GetTestCase()
   {
   }

   public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
   {
      if (getRequestCount() == 0)
      {
         Map<String, String[]> queryParameters = assertNotNull(req.getQueryParameterMap());
         assertNull(req.getBody());
         assertTrue(queryParameters.isEmpty());

         //
         StringBuffer tmp = new StringBuffer(rewriteURL(testServlet, "/"));
         tmp.append('?').append(encoder.encode("a")).append("=").append(encoder.encode("a_value"));
         tmp.append('&').append(encoder.encode("b")).append("=").append(encoder.encode("b_value_1"));
         tmp.append('&').append(encoder.encode("b")).append("=").append(encoder.encode("b_value_2"));
         tmp.append('&').append(encoder.encode("c")).append("=").append(encoder.encode(RANGE_0_255));
         return new InvokeGetResponse(tmp.toString());
      }
      else if (getRequestCount() == 1)
      {
         try
         {
            Map<String, String[]> queryParameters = assertNotNull(req.getQueryParameterMap());
            assertNull(req.getBody());
            assertEquals(3, queryParameters.size());
            assertEquals(new String[]{"a_value"}, queryParameters.get("a"));
            assertEquals(new String[]{"b_value_1","b_value_2"}, queryParameters.get("b"));
            assertEquals(new String[]{RANGE_0_255}, queryParameters.get("c"));
         }
         catch (IllegalRequestException e)
         {
            fail(e);
         }
      }
      else
      {
         fail();
      }

      //
      return new EndTestResponse();
   }

   public DriverResponse invoke(TestServlet testServlet, DriverCommand driverCommand)
   {
      if (getRequestCount() == -1)
      {
         return new InvokeGetResponse(rewriteURL(testServlet, "/"));
      }
      else
      {
         return new FailureResponse(Failure.createAssertionFailure(""));
      }
   }
}
