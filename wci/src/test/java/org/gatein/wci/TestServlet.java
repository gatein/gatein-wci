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
package org.gatein.wci;

import org.jboss.unit.remote.ResponseContext;
import org.jboss.unit.driver.impl.composite.CompositeTestDriver;
import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.response.FailureResponse;
import org.jboss.unit.Failure;
import org.jboss.unit.info.TestInfo;
import org.gatein.common.mc.bootstrap.WebBootstrap;
import org.gatein.wci.endpoint.EndPointServlet;
import org.gatein.wci.WebRequest;
import org.gatein.wci.WebResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public final class TestServlet extends EndPointServlet
{

   /** . */
   private CompositeTestDriver testSuite;

   /** . */
   ServletTestCase currentTestCase;

   public void init() throws ServletException
   {
      super.init();

      //
      testSuite = (CompositeTestDriver)getServletContext().getAttribute(WebBootstrap.BEAN_PREFIX + "TestSuite");

      // Init the test cases
      for (String name : testSuite.getNames())
      {
         ((ServletTestCase)testSuite.getDriver(name)).testServlet = this;
      }
   }

   protected void service(WebRequest req, WebResponse resp) throws ServletException, IOException
   {
      DriverResponse response;
      try
      {
         response = currentTestCase.service(this, req, resp);
      }
      catch (AssertionError e)
      {
         response = new FailureResponse(Failure.createFailure(e));
      }
      currentTestCase.setResponseContext(new ResponseContext(response, new HashMap<String, Serializable>()));
      resp.setStatus(200);
   }

   public void destroy()
   {
      testSuite = null;

      //
      super.destroy();
   }

   public TestInfo getInfo()
   {
      return testSuite.getInfo();
   }
}
