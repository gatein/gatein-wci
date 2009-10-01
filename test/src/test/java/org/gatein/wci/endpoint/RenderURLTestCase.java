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
import org.gatein.common.servlet.URLFormat;
import org.gatein.common.util.Tools;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss-portal.org">Julien Viet</a>
 * @version $Revision: 630 $
 */
public class RenderURLTestCase extends EndPointTestCase
{

   /** . */
   private static final URLFormat format = URLFormat.create(null, null, null, null, null);

   public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
   {
      if (getRequestCount() == 0)
      {
         try
         {
            resp.renderURL(null, new HashMap<String, String[]>(), format);
            fail();
         }
         catch (IllegalArgumentException ignore)
         {
         }
         try
         {
            resp.renderURL("", new HashMap<String, String[]>(), format);
            fail();
         }
         catch (IllegalArgumentException ignore)
         {
         }
         try
         {
            HashMap<String, String[]> corruptedMap = new HashMap<String, String[]>();
            corruptedMap.put(null, new String[0]);
            resp.renderURL("/", corruptedMap, format);
            fail();
         }
         catch (IllegalArgumentException ignore)
         {
         }
         try
         {
            HashMap<String, String[]> corruptedMap = new HashMap<String, String[]>();
            corruptedMap.put("foo", new String[]{null});
            resp.renderURL("/", corruptedMap, format);
            fail();
         }
         catch (IllegalArgumentException ignore)
         {
         }

         //
         String url = resp.renderURL("/", null, null);
         return new InvokeGetResponse(url);
      }
      else if (getRequestCount() == 1)
      {
         assertEquals("/", req.getWebRequestPath());
         assertEquals(new HashMap<String, String[]>(), req.getQueryParameterMap());

         //
         String url = resp.renderURL("/", new HashMap<String, String[]>(), null);
         return new InvokeGetResponse(url);
      }
      else if (getRequestCount() == 2)
      {
         assertEquals("/", req.getWebRequestPath());
         assertEquals(new HashMap<String, String[]>(), req.getQueryParameterMap());

         //
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put("foo", new String[]{"foo_value"});
         parameters.put("bar", new String[]{"bar_value_1", "bar_value_2"});
         parameters.put("juu", new String[0]);
         String url = resp.renderURL("/", parameters, null);
         return new InvokeGetResponse(url);
      }
      else if (getRequestCount() == 3)
      {
         assertEquals("/", req.getWebRequestPath());
         Map<String, String[]> parameters = assertNotNull(req.getQueryParameterMap());
         assertEquals(2, parameters.size());
         assertEquals(Tools.toSet("foo", "bar"), parameters.keySet());
         assertEquals(new String[]{"foo_value"}, parameters.get("foo"));
         assertEquals(new String[]{"bar_value_1","bar_value_2"}, parameters.get("bar"));

         //
         String url = resp.renderURL("/blah", null, null);
         return new InvokeGetResponse(url);
      }
      else if (getRequestCount() == 4)
      {
         assertEquals("/blah", req.getWebRequestPath());
         assertEquals(new HashMap<String, String[]>(), req.getQueryParameterMap());

         //
         String url = resp.renderURL("/blah", new HashMap<String, String[]>(), null);
         return new InvokeGetResponse(url);
      }
      else if (getRequestCount() == 5)
      {
         assertEquals("/blah", req.getWebRequestPath());
         assertEquals(new HashMap<String, String[]>(), req.getQueryParameterMap());

         //
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         parameters.put("foo", new String[]{"foo_value"});
         parameters.put("bar", new String[]{"bar_value_1", "bar_value_2"});
         parameters.put("juu", new String[0]);
         String url = resp.renderURL("/blah", parameters, null);
         return new InvokeGetResponse(url);
      }
      else if (getRequestCount() == 6)
      {
         assertEquals("/blah", req.getWebRequestPath());
         Map<String, String[]> parameters = assertNotNull(req.getQueryParameterMap());
         assertEquals(2, parameters.size());
         assertEquals(Tools.toSet("foo", "bar"), parameters.keySet());
         assertEquals(new String[]{"foo_value"}, parameters.get("foo"));
         assertEquals(new String[]{"bar_value_1","bar_value_2"}, parameters.get("bar"));

         //
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