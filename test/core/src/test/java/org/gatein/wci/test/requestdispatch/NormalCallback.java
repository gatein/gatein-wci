/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.gatein.wci.test.requestdispatch;

import org.gatein.wci.ServletContextDispatcher;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

   @Override
   protected Throwable test(ServletContextDispatcher dispatcher)
   {
      try
      {
         Object returnedValue = dispatcher.include(expectedContext, this, expectedHandback);

         //
         if (!invoked)
         {
            return new Exception("The callback was not invoked");
         }
         if (expectedHandback != handback)
         {
            return new Exception("The provided handback is not the same than the expected handback");
         }
         if (expectedReturnedValue != returnedValue)
         {
            return new Exception("The returned value is not the same than the expected one");
         }
         if (expectedThreadContextClassLoader != threadContextClassLoader)
         {
            return new Exception("The thread context class loader is not the same than the expected one");
         }
      }
      catch (Exception e)
      {
         return e;
      }

      //
      return null;
   }
}
