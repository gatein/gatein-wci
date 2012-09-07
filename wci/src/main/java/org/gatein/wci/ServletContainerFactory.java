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

import org.gatein.wci.spi.ServletContainerContext;

/**
 * It's rather a provider rather than a real factory. But I prefer the factory name than the provider name.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public final class ServletContainerFactory
{

   /** . */
   public static final ServletContainerFactory instance = new ServletContainerFactory();

   /** . */
   private final ServletContainer container = new ServletContainer();

   /**
    * Returns the singleton instance.
    *
    * @return the singleton instance
    */
   public static ServletContainer getServletContainer()
   {
      return instance.container;
   }

   /**
    * Registers a servlet container context. The registration is considered as successful if no existing context is
    * already registered.
    *
    * @param context the servlet container context to register
    * @throws IllegalArgumentException if the context is null
    */
   public static void registerContext(ServletContainerContext context) throws IllegalArgumentException
   {
      instance.container.register(context);
   }
}
