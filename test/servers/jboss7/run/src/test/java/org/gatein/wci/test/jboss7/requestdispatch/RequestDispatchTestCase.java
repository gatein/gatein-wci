/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.gatein.wci.test.jboss7.requestdispatch;

import org.gatein.wci.test.requestdispatch.AbstractCallback;
import org.gatein.wci.test.requestdispatch.ExceptionCallback;
import org.gatein.wci.test.requestdispatch.NormalCallback;
import org.gatein.wci.test.WebAppRegistry;
import org.gatein.wci.test.requestdispatch.AbstractRequestDispatchTestCase;
import org.gatein.wci.test.requestdispatch.RequestDispatchServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RequestDispatchTestCase extends AbstractRequestDispatchTestCase
{

   @Deployment(name = "rdwci")
   public static WebArchive wciDeployment()
   {
      WebArchive war = wciJBoss7Deployment("rdwci.war");
      war.setWebXML("org/gatein/wci/test/jboss7/requestdispatch/web.xml");
      war.addClass(WebAppRegistry.class);
      war.addClass(RequestDispatchServlet.class);
      war.addClass(AbstractCallback.class);
      war.addClass(ExceptionCallback.class);
      war.addClass(NormalCallback.class);
      return war;
   }
}
