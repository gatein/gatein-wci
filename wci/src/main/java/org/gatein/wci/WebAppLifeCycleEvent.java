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

/**
 * Web application life cycle event.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class WebAppLifeCycleEvent extends WebAppEvent
{

   /** Application is removed. */
   public final static int REMOVED = 0;

   /** Application is added. */
   public final static int ADDED = 1;

   /** The type of the life cycle which can be <code>ADDED</code> or <code>REMOVED</code> . */
   private final int type;

   /**
    * Creates a new web application life cycle event.
    *
    * @param webApp the web application
    * @param type the life cycle type
    * @throws IllegalArgumentException if the web application is null or the type value is not valid
    */
   public WebAppLifeCycleEvent(WebApp webApp, int type)
      throws IllegalArgumentException
   {
      super(webApp);

      //
      if (type < REMOVED || type > ADDED)
      {
         throw new IllegalArgumentException("Type " + type + " not accepted");
      }

      //
      this.type = type;
   }

   /**
    * Returns the life cycle type.
    *
    * @return the life cycle type
    */
   public int getType()
   {
      return type;
   }
}
