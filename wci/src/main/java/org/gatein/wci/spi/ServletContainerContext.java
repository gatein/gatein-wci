/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wci.spi;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.security.Credentials;

/**
 * Defines the service provider interface for a servlet container. It is an attempt to abstract the non
 * portable services required by a portal with respect to the web container layer.
 *
 * @todo add session invalidation mechanism
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public interface ServletContainerContext
{
   /**
    * Generic detyped request dispatch to a servlet context using the include mechanism.
    *
    * @param targetServletContext the target servlet context to dispatch to
    * @param request the request valid in the current servlet context
    * @param response the response valid in the current servlet context
    * @param callback the callback to perform after the dispatch operation
    * @param handback the handback object that will be provided to the callback
    * @return the object returned by the callback
    * @throws ServletException any servlet exception
    * @throws IOException any io exception
    */
   Object include(
      ServletContext targetServletContext, HttpServletRequest request,
      HttpServletResponse response,
      RequestDispatchCallback callback,
      Object handback) throws ServletException, IOException;

   /**
    * Install the call back object.
    *
    * @param registration the call back
    */
   void setCallback(Registration registration);

   /**
    * Uninstall the call back object.
    *
    * @param registration the call back
    */
   void unsetCallback(Registration registration);

   /**
    * Manually register a webapp with this ServletContainerContext.
    *
    * @param webappContext the WebAppContext associated with the application
    * @param dispathPath the path to be used
    */
   void registerWebApp(WebAppContext webappContext, String dispatchPath);

   /**
    * Manually unregister a webapp associated with this ServletContainerContext.
    *
    * @param servletContext the servletContext of the application to be deregistered
    */
   void unregisterWebApp(ServletContext servletContext);

   /**
    * Authentication support.
    *
    * @param request the request valid in the current servlet context
    * @param response the response valid in the current servlet context
    * @param credentials the credentials which try to authenticate
    * @param validityMillis the validity of the authentication
    */
   void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials, long validityMillis) throws ServletException, IOException;

   /**
    * Authentication support.
    *
    * @param request the request valid in the current servlet context
    * @param response the response valid in the current servlet context
    * @param credentials the credentials which try to authenticate
    * @param validityMillis the validity of the authentication
    */
   void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials, long validityMillis, String initialURI) throws ServletException, IOException;

   /**
    * Authentication support.
    *
    * @param request the request valid in the current servlet context
    * @param response the response valid in the current servlet context
    */
   void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException;

  /**
   * Returns the name and version of the servlet container in which the
   * context is running.
   *
   * <P>
   * The form of the returned string is <code>containername/versionnumber</code>.
   *
   *
   * @return   the string containing at least name and version number
   */
   public String getContainerInfo();

   /**
    * The callback interface that a servlet container context can obtain from its registration against
    * the <code>org.jboss.portal.web.ServletContainer</code> singleton.
    */
   interface Registration
   {

      /**
       * Registers a web application.
       *
       * @param webAppContext the web application context
       * @return true if the registration was done
       * @throws IllegalArgumentException if the argument is null
       * @throws IllegalStateException if the registration is cancelled
       */
      boolean registerWebApp(WebAppContext webAppContext) throws IllegalStateException, IllegalArgumentException;

      /**
       * Unregister a web application.
       *
       * @param contextPath the web application id
       * @return true if the unregistration was done
       * @throws IllegalArgumentException if the argument is null
       * @throws IllegalStateException if the registration is cancelled
       */
      boolean unregisterWebApp(String contextPath) throws IllegalStateException, IllegalArgumentException;

      /**
       * Cancel the registration against the servlet container.
       *
       * @throws IllegalStateException if the registration is cancelled
       */
      void cancel() throws IllegalStateException;
   }

}
