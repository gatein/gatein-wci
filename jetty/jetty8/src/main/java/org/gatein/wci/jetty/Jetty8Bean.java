/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.wci.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 9/24/12
 */
public class Jetty8Bean extends AbstractLifeCycle
{
   private final static Logger LOG = LoggerFactory.getLogger(Jetty8Bean.class);

   private final Server server;

   private Jetty8ServletContainerContext containerContext;

   public Jetty8Bean(Server server)
   {
      this.server = server;
   }

   @Override
   protected void doStart() throws Exception
   {
      LOG.debug("Init WCI infrastructure in Jetty Server");
      containerContext = new Jetty8ServletContainerContext(server);
      containerContext.start();
   }

   @Override
   protected void doStop() throws Exception
   {
      LOG.debug("Remove WCI infrastructure from Jetty Server");
      if(containerContext != null)
      {
         containerContext.stop();
      }
   }
}
