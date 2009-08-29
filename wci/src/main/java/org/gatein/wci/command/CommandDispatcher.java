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

import org.gatein.wci.RequestDispatchCallback;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class CommandDispatcher
{

   public Object include(
      ServletContext targetServletContext,
      HttpServletRequest req,
      HttpServletResponse resp,
      RequestDispatchCallback callback,
      Object handback) throws ServletException, IOException
   {
      CallbackCommand cmd = new CallbackCommand(targetServletContext, callback, handback);

      //
      return CommandServlet.include(req, resp, cmd, targetServletContext);
   }

   public static class CallbackCommand
   {

      /** . */
      private final ServletContext servletContext;

      /** . */
      private final RequestDispatchCallback invocation;

      /** . */
      private final Object handback;

      public CallbackCommand(ServletContext servletContext, RequestDispatchCallback invocation, Object handback)
      {
         this.servletContext = servletContext;
         this.invocation = invocation;
         this.handback = handback;
      }

      public Object execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
      {
         return invocation.doCallback(servletContext, req, resp, handback);
      }
   }
}
