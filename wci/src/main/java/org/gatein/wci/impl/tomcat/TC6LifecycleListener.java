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
package org.gatein.wci.impl.tomcat;

import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 1.1 $
 */
public class TC6LifecycleListener implements LifecycleListener
{

   /** . */
   private TC6ServletContainerContext containerContext;

   public synchronized void lifecycleEvent(LifecycleEvent event)
   {
      Lifecycle lifecycle = event.getLifecycle();

      //
      if (lifecycle instanceof Server)
      {
         Server server = (Server)lifecycle;

         //
         Engine engine = getEngine(server);

         //
         if (engine != null)
         {
            if (Lifecycle.START_EVENT.equals(event.getType()))
            {
               containerContext = new TC6ServletContainerContext(engine);
               containerContext.start();
            }
            else if (Lifecycle.STOP_EVENT.equals(event.getType()))
            {
               if (containerContext != null)
               {
                  containerContext.stop();
               }
            }
         }
      }
   }

   private Engine getEngine(Server server)
   {
      Service[] services = server.findServices();
      for (int i = 0; i < services.length; i++)
      {
         Service service = services[i];
         Engine engine = getEngine(service.getContainer());
         if (engine != null)
         {
            return engine;
         }
      }
      return null;
   }

   private Engine getEngine(Container container)
   {
      if (container instanceof Engine)
      {
         return (Engine)container;
      }
      return null;
   }
}
