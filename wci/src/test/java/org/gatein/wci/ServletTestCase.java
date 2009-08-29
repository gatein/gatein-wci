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

import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.driver.DriverCommand;
import org.jboss.unit.driver.AbstractTestDriver;
import org.jboss.unit.info.TestCaseInfo;
import org.jboss.unit.info.ParameterInfo;
import org.jboss.unit.info.TestInfo;
import org.jboss.unit.TestId;
import org.jboss.unit.remote.ResponseContext;
import org.jboss.unit.remote.RequestContext;
import org.jboss.unit.remote.driver.RemoteTestDriver;
import org.gatein.wci.WebRequest;
import org.gatein.wci.WebResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public abstract class ServletTestCase extends AbstractTestDriver implements RemoteTestDriver, TestCaseInfo
{

   /** . */
   protected TestServlet testServlet;

   /** . */
   private RequestContext requestContext;

   /** . */
   private ResponseContext responseContext;

   protected ServletTestCase()
   {
   }

   public TestInfo getInfo()
   {
      return this;
   }

   public DriverResponse invoke(TestId testId, DriverCommand driverCommand)
   {
      testServlet.currentTestCase = this;

      //
      return invoke(testServlet, driverCommand);
   }

   public String getName()
   {
      return getClass().getSimpleName();
   }

   public String getDescription()
   {
      return "No description";
   }

   public Map<String, ? extends ParameterInfo> getParameters()
   {
      return Collections.emptyMap();
   }

   public Set<String> getKeywords()
   {
      return Collections.emptySet();
   }

   public void pushContext(TestId testId, RequestContext requestContext)
   {
      this.requestContext = requestContext;
   }

   public ResponseContext popContext(TestId testId)
   {
      return responseContext;
   }

   public int getRequestCount()
   {
      return requestContext.getRequestCount();
   }

   protected void setResponseContext(ResponseContext responseContext)
   {
      this.responseContext = responseContext;
   }

   public abstract DriverResponse service(TestServlet testServlet, WebRequest req, WebResponse resp) throws ServletException, IOException;

   public abstract DriverResponse invoke(TestServlet testServlet, DriverCommand driverCommand);

   public static String RANGE_0_255 = computeFrom0To255();

   public static String RANGE_256_512 = computeFrom256To512();

   private static String computeFrom0To255()
   {
      return compute(0, 256);
   }

   private static String computeFrom256To512()
   {
      return compute(256, 512);
   }

   public static String compute(int from, int to)
   {
      if (from < 0)
      {
         throw new IllegalArgumentException();
      }
      if (from > to)
      {
         throw new IllegalArgumentException();
      }
      StringBuffer tmp = new StringBuffer();
      for (int i = from; i < to; i++)
      {
         char c = (char)i;
         tmp.append(c);
      }
      return tmp.toString();
   }

   public static String compareString(String s1, String s2)
   {
      if (s1 == null)
      {
         return "s1 is null";
      }
      if (s2 == null)
      {
         return "s2 is null";
      }
      if (s1.length() != s2.length())
      {
         return "lengths don't match " + s1.length() + "!=" + s2.length();
      }
      for (int i = s1.length() - 1; i >= 0; i--)
      {
         char c1 = s1.charAt(i);
         char c2 = s2.charAt(i);
         if (c1 != c2)
         {
            return "char at position " + i + " are different " + (int)c1 + "!=" + (int)c2;
         }
      }
      return null;
   }
}
