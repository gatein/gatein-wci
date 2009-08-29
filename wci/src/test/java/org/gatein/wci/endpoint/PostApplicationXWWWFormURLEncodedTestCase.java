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
import org.gatein.wci.Body;
import org.gatein.wci.WebResponse;
import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.DriverCommand;
import org.jboss.unit.driver.response.EndTestResponse;
import org.jboss.unit.driver.response.FailureResponse;
import org.jboss.unit.remote.driver.handler.http.response.InvokePostResponse;
import org.jboss.unit.remote.http.HttpRequest;
import org.jboss.unit.Failure;
import org.jboss.unit.api.Assert;

import static org.jboss.unit.api.Assert.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class PostApplicationXWWWFormURLEncodedTestCase extends EndPointTestCase
{

   /** . */
   private final FastURLEncoder encoder = FastURLEncoder.getUTF8Instance();

   public PostApplicationXWWWFormURLEncodedTestCase()
   {
   }

   public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
   {
      if (getRequestCount() == 0)
      {
         Map<String, String[]> queryParameters = Assert.assertNotNull(req.getQueryParameterMap());
         assertTrue(queryParameters.isEmpty());
         Body.Form form = Assert.assertInstanceOf(req.getBody(), Body.Form.class);
         Map<String, String[]> formParameters = form.getParameters();
         assertTrue(formParameters.isEmpty());

         //
         InvokePostResponse post = new InvokePostResponse(rewriteURL(testServlet, "?a=a_value_query"));
         post.setBody(new HttpRequest.Form());
         post.setContentType(InvokePostResponse.APPLICATION_X_WWW_FORM_URLENCODED);
         return post;
      }
      else if (getRequestCount() == 1)
      {
         Map<String, String[]> queryParameters = Assert.assertNotNull(req.getQueryParameterMap());
         assertEquals(1, queryParameters.size());
         assertEquals(new String[]{"a_value_query"}, queryParameters.get("a"));
         Body.Form form = Assert.assertInstanceOf(req.getBody(), Body.Form.class);
         Map<String, String[]> formParameters = form.getParameters();
         assertTrue(formParameters.isEmpty());

         //
         InvokePostResponse post = new InvokePostResponse(rewriteURL(testServlet, "/"));
         HttpRequest.Form requestForm = new HttpRequest.Form();
         requestForm.addParameter("a", new String[]{"a_value_body"});
         post.setBody(requestForm);
         post.setContentType(InvokePostResponse.APPLICATION_X_WWW_FORM_URLENCODED);
         return post;
      }
      else if (getRequestCount() == 2)
      {
         Map<String, String[]> queryParameters = Assert.assertNotNull(req.getQueryParameterMap());
         assertTrue(queryParameters.isEmpty());
         Body.Form form = Assert.assertInstanceOf(req.getBody(), Body.Form.class);
         Map<String, String[]> formParameters = form.getParameters();
         assertEquals(1, formParameters.size());
         assertEquals(new String[]{"a_value_body"}, formParameters.get("a"));

         //
         InvokePostResponse post = new InvokePostResponse(rewriteURL(testServlet, "?a=a_value_query"));
         post.setBody(new HttpRequest.Form());
         HttpRequest.Form requestForm = new HttpRequest.Form();
         requestForm.addParameter("a", new String[]{"a_value_form"});
         post.setBody(requestForm);
         post.setContentType(InvokePostResponse.APPLICATION_X_WWW_FORM_URLENCODED);
         return post;
      }
      else if (getRequestCount() == 3)
      {
         Map<String, String[]> queryParameters = Assert.assertNotNull(req.getQueryParameterMap());
         assertEquals(1, queryParameters.size());
         assertEquals(new String[]{"a_value_query"}, queryParameters.get("a"));
         Body.Form form = Assert.assertInstanceOf(req.getBody(), Body.Form.class);
         Map<String, String[]> formParameters = form.getParameters();
         assertEquals(1, formParameters.size());
         assertEquals(new String[]{"a_value_form"}, formParameters.get("a"));

         //
         InvokePostResponse post = new InvokePostResponse(rewriteURL(testServlet, "?a=" + encoder.encode(RANGE_0_255)));
         post.setBody(new HttpRequest.Form());
         HttpRequest.Form requestForm = new HttpRequest.Form();
         requestForm.addParameter("a", new String[]{RANGE_256_512});
         post.setBody(requestForm);
         post.setContentType(InvokePostResponse.APPLICATION_X_WWW_FORM_URLENCODED + "; charset=UTF-8");
         return post;
      }
      else if (getRequestCount() == 4)
      {
         Map<String, String[]> queryParameters = Assert.assertNotNull(req.getQueryParameterMap());
         assertEquals(1, queryParameters.size());
         assertNull(compareString(RANGE_0_255, queryParameters.get("a")[0]));
         Body.Form form = Assert.assertInstanceOf(req.getBody(), Body.Form.class);
         Map<String, String[]> formParameters = form.getParameters();
         assertEquals(1, formParameters.size());
         assertNull(compareString(RANGE_256_512, formParameters.get("a")[0]));

         //
         InvokePostResponse post = new InvokePostResponse(rewriteURL(testServlet, "?a=" + encoder.encode(RANGE_256_512)));
         post.setBody(new HttpRequest.Form());
         HttpRequest.Form requestForm = new HttpRequest.Form();
         requestForm.addParameter("a", new String[]{RANGE_0_255});
         post.setBody(requestForm);
         post.setContentType(InvokePostResponse.APPLICATION_X_WWW_FORM_URLENCODED + "; charset=UTF-8");
         return post;
      }
      else if (getRequestCount() == 5)
      {
         Map<String, String[]> queryParameters = Assert.assertNotNull(req.getQueryParameterMap());
         assertEquals(1, queryParameters.size());
         assertNull(compareString(RANGE_256_512, queryParameters.get("a")[0]));
         Body.Form form = Assert.assertInstanceOf(req.getBody(), Body.Form.class);
         Map<String, String[]> formParameters = form.getParameters();
         assertEquals(1, formParameters.size());
         assertNull(compareString(RANGE_0_255, formParameters.get("a")[0]));
      }
      else
      {
         Assert.fail();
      }

      //
      return new EndTestResponse();
   }

   public DriverResponse invoke(TestServlet testServlet, DriverCommand driverCommand)
   {
      if (getRequestCount() == -1)
      {
         InvokePostResponse post = new InvokePostResponse(rewriteURL(testServlet, "/"));
         post.setBody(new HttpRequest.Form());
         post.setContentType(InvokePostResponse.APPLICATION_X_WWW_FORM_URLENCODED);
         return post;
      }
      else
      {
         return new FailureResponse(Failure.createAssertionFailure(""));
      }
   }
}
