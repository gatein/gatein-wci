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
package org.gatein.wci.spi.callbacks;

import org.gatein.wci.ServletContextDispatcher;
import org.jboss.unit.driver.response.FailureResponse;
import org.jboss.unit.driver.DriverResponse;
import org.jboss.unit.Failure;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class NormalCallback extends AbstractCallback
{

   /** . */
   private final ServletContext expectedContext;

   /** . */
   private final ClassLoader expectedThreadContextClassLoader;

   /** . */
   private final Object expectedHandback;

   /** . */
   private final Object expectedReturnedValue;

   /** . */
   private Object handback;

   /** . */
   private ClassLoader threadContextClassLoader;

   /** . */
   private boolean invoked;

   public NormalCallback(ServletContext expectedContext, ClassLoader expectedThreadContextClassLoader)
   {
      this.expectedContext = expectedContext;
      this.expectedThreadContextClassLoader = expectedThreadContextClassLoader;
      this.expectedHandback = new Object();
      this.expectedReturnedValue = new Object();

      //
      this.invoked = false;
      this.handback = null;
   }

   public Object doCallback(ServletContext dispatchedServletContext, HttpServletRequest dispatchedRequest, HttpServletResponse dispatchedResponse, Object handback) throws ServletException, IOException
   {
      this.invoked = true;
      this.threadContextClassLoader = Thread.currentThread().getContextClassLoader();
      this.handback = handback;

      //
      return expectedReturnedValue;
   }

   protected DriverResponse test(ServletContextDispatcher dispatcher)
   {
      try
      {
         Object returnedValue = dispatcher.include(expectedContext, this, expectedHandback);

         //
         if (!invoked)
         {
            return new FailureResponse(Failure.createAssertionFailure("The callback was not invoked"));
         }
         if (expectedHandback != handback)
         {
            return new FailureResponse(Failure.createAssertionFailure("The provided handback is not the same than the expected handback"));
         }
         if (expectedReturnedValue != returnedValue)
         {
            return new FailureResponse(Failure.createAssertionFailure("The returned value is not the same than the expected one"));
         }
         if (expectedThreadContextClassLoader != threadContextClassLoader)
         {
            return new FailureResponse(Failure.createAssertionFailure("The thread context class loader is not the same than the expected one"));
         }
      }
      catch (Exception e)
      {
         return new FailureResponse(Failure.createErrorFailure(e));
      }

      //
      return null;
   }
}
