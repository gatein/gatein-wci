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

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class ExceptionCallback extends AbstractCallback
{

   /** . */
   private final ServletContext target;

   /** . */
   private final Throwable expectedThrowable;

   /** . */
   private final Throwable throwable;

   public ExceptionCallback(ServletContext target, Throwable expectedThrowable, Throwable throwable)
   {
      this.target = target;
      this.expectedThrowable = expectedThrowable;
      this.throwable = throwable;
   }

   protected Throwable test(ServletContextDispatcher dispatcher)
   {
      try
      {
         dispatcher.include(target, this, null);

         //
         return new Exception("Got no throwable thrown was expecting " + expectedThrowable);
      }
      catch (Throwable throwable)
      {
         if (throwable instanceof ServletException)
         {
            throwable = throwable.getCause();
         }
         if (expectedThrowable != throwable)
         {
            return new Exception("Got throwable " + throwable + " instead of throwable " + expectedThrowable);
         }
      }

      //
      return null;
   }

   public Object doCallback(ServletContext dispatchedServletContext, HttpServletRequest dispatchedRequest, HttpServletResponse dispatchedResponse, Object handback) throws ServletException, IOException
   {
      if (throwable instanceof IOException)
      {
         throw (IOException)throwable;
      }
      if (throwable instanceof Exception)
      {
         throw new ServletException(throwable);
      }
      else if (throwable instanceof Error)
      {
         throw (Error)throwable;
      }
      return null;
   }
}
