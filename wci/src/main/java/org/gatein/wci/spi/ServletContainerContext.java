/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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
    * The callback interface that a servlet container context can obtain from its registration against
    * the <code>org.gatein.wci.ServletContainer</code> singleton.
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
