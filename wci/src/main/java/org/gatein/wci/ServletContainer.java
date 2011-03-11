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
package org.gatein.wci;

import org.gatein.wci.authentication.AuthenticationListener;
import org.gatein.wci.security.Credentials;
import org.gatein.wci.spi.ServletContainerContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A static registry for the servlet container context.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public interface ServletContainer
{

   /**
    * Add a web listener.
    *
    * @param listener the listener
    * @return true if the listener has been added
    */
   boolean addWebAppListener(WebAppListener listener);

   /**
    * Removes a web listener.
    *
    * @param listener the listener
    * @return true if the listener has been removed
    */
   boolean removeWebAppListener(WebAppListener listener);

   /**
    * Returns an executor that will use the provided request and response.
    *
    * @param request the request
    * @param response the response
    * @return an executor
    */
   WebExecutor getExecutor(HttpServletRequest request, HttpServletResponse response);

   /**
    * Generic detyped request dispatch to a servlet context using the include mechanism.
    *
    * @param targetServletContext the target servlet context to dispatch to
    * @param request              the request valid in the current servlet context
    * @param response             the response valid in the current servlet context
    * @param callback             the callback to perform after the dispatch operation
    * @param handback             the handback object that will be provided to the callback
    * @return the object returned by the callback
    * @throws ServletException any servlet exception
    * @throws IOException any io exception
    */
   public Object include(
      ServletContext targetServletContext,
      HttpServletRequest request,
      HttpServletResponse response,
      RequestDispatchCallback callback,
      Object handback) throws ServletException, IOException;

   /**
    * Register a servlet container context. The registration is considered as successful if no existing context is
    * already registered.
    *
    * @param context the servlet container context to register
    * @throws IllegalArgumentException if the context is null
    */
   void register(ServletContainerContext context);

   /**
    * Authentication support.
    *
    * @param request the request valid in the current servlet context
    * @param response the response valid in the current servlet context
    * @param credentials the credentials which try to authenticate
    */
   void login(HttpServletRequest request, HttpServletResponse response, Credentials credentials, long validityMillis) throws ServletException, IOException;

   /**
    * Authentication support.
    *
    * @param request the request valid in the current servlet context
    * @param response the response valid in the current servlet context
    * @param credentials the credentials which try to authenticate
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
    * Add the authentication listener.
    *
    * @param listener AuthenticationListener to add
    */
   void addAuthenticationListener(AuthenticationListener listener);

   /**
    * Remove the authentication listener.
    *
    * @param listener AuthenticationListener to remove
    */
   void removeAuthenticationlistener(AuthenticationListener listener);

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
    * Visit the registered WebApps
    *
    * @param visitor ServletContainerVisitor instance
    */
   void visit(ServletContainerVisitor visitor); 
}
