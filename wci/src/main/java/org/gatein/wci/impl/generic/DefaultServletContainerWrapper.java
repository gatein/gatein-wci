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
package org.gatein.wci.impl.generic;

import org.exoplatform.container.component.ComponentPlugin;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainer;
import org.gatein.wci.spi.ServletContainerContext;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class DefaultServletContainerWrapper extends DefaultServletContainer implements Startable
{
   public DefaultServletContainerWrapper()
   {  
   }

   public DefaultServletContainerWrapper(ServletContainerContext servletContainerContext)
   {
      this.register(servletContainerContext);
   }
   
   public void addWebAppListener(ComponentPlugin plugin)
   {
      if (plugin instanceof WebAppListener)
      {
         addWebAppListener((WebAppListener)plugin);
      }
   }

   public void start()
   {
      //do nothing, this method is required for the class to start when the configuration file is read
   }

   public void stop()
   {
      //do nothing, this method is required for the class to start when the configuration file is read
   }
      
}

