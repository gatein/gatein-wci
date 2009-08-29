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
package org.gatein.wci.command;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This servlet is used to execute command coming from another context through a dispatching request. The invocation is
 * detyped in order to allow redeployment and avoid class cast exception.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class CommandServlet extends HttpServlet
{

   /** . */
   private static final ThreadLocal localCmd = new ThreadLocal();

   /** . */
   private static final ThreadLocal localResponse = new ThreadLocal();

   /** . */
   private static final ThreadLocal localThrowable = new ThreadLocal();

   /**
    * <p>Execute a command after having performed a request dispatch in the target servlet context.</p>
    * <p/>
    * <p>The provided callback argument must expose a public non static and non abstract method with the signature
    * <code>execute(HttpServletRequest,HttpServletResponse)</code>. This method must return an object and can declare
    * any exception. This method will be invoked after the request dispatch operation is done.</p>
    * <p/>
    * <p>Any throwable thrown by the callback invocation will be wrapped in a <code>ServletException</code> and
    * rethrown, unless it is an instance of <code>ServletException</code> or <code>IOException</code>.</p>
    *
    * @param callback      the callback to invoke after the inclusion is done
    * @param targetContext the target servlet context
    * @throws IOException      likely thrown by the request dispatch operation
    * @throws ServletException wraps any exception thrown by the callback
    */
   public static Object include(
      HttpServletRequest request,
      HttpServletResponse response,
      Object callback,
      ServletContext targetContext) throws ServletException, IOException
   {
      try
      {
         localCmd.set(callback);
         RequestDispatcher switcher = targetContext.getRequestDispatcher("/jbossportlet");
         switcher.include(request, response);

         //
         Throwable throwable = (Throwable)localThrowable.get();

         //
         if (throwable != null)
         {
            if (throwable instanceof IOException)
            {
               throw (IOException)throwable;
            }
            else if (throwable instanceof ServletException)
            {
               throw (ServletException)throwable;
            }
            else
            {
               ServletException se = new ServletException();
               se.initCause(throwable);
               throw se;
            }
         }

         //
         return localResponse.get();
      }
      finally
      {
         localCmd.set(null);
         localResponse.set(null);
         localThrowable.set(null);
      }
   }

   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      Object cmd = localCmd.get();

      //
      if (cmd != null)
      {
         try
         {
            Method methods = cmd.getClass().getMethod(
               "execute",
               new Class[]{
                  HttpServletRequest.class,
                  HttpServletResponse.class});

            //
            Object response = methods.invoke(cmd, new Object[]{req, resp});

            //
            localResponse.set(response);
         }
         catch (NoSuchMethodException e)
         {
            throw new Error("No execute method found on the command", e);
         }
         catch (InvocationTargetException e)
         {
            // Log the wrappee
            Throwable wrappee = e.getTargetException();

            // Here we wrap it and rethrow
            localThrowable.set(wrappee);
         }
         catch (IllegalAccessException e)
         {
            throw new Error("Unexpected IllegalAccessException during command invocation", e);
         }
      }
      else
      {
         // That should not happen
         throw new Error("No command found");
      }
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      doGet(req, resp);
   }
}
