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
import org.gatein.common.io.IOTools;
import org.gatein.wci.WebRequest;
import org.gatein.wci.Body;
import org.gatein.wci.WebResponse;
import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.DriverCommand;
import org.jboss.unit.driver.response.EndTestResponse;
import org.jboss.unit.driver.response.FailureResponse;
import org.jboss.unit.api.Assert;
import org.jboss.unit.remote.driver.handler.http.response.InvokePostResponse;
import org.jboss.unit.remote.http.HttpRequest;
import org.jboss.unit.Failure;

import static org.jboss.unit.api.Assert.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class PostMultipartFormDataTestCase extends EndPointTestCase
{

   /** . */
   private final FastURLEncoder encoder = FastURLEncoder.getUTF8Instance();

   public PostMultipartFormDataTestCase()
   {
   }

   public DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException
   {
      if (getRequestCount() == 0)
      {
         Map<String, String[]> queryParameters = Assert.assertNotNull(req.getQueryParameterMap());
         Assert.assertTrue(queryParameters.isEmpty());
         Body.Raw raw = Assert.assertInstanceOf(req.getBody(), Body.Raw.class);
         InputStream in = raw.getInputStream();
         byte[] bytes = IOTools.getBytes(in);
         assertTrue(Arrays.equals(new byte[]{0,1,1,2,3,5,8,13,21,34},bytes));
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
         HttpRequest.Raw raw = new HttpRequest.Raw();
         post.setBody(raw);
         raw.setBytes(new byte[]{0,1,1,2,3,5,8,13,21,34});
         post.setContentType(InvokePostResponse.MULTIPART_FORM_DATA);
         return post;
      }
      else
      {
         return new FailureResponse(Failure.createAssertionFailure(""));
      }
   }

}
