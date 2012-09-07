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
package org.gatein.wci.test.jboss7.crosscontext;

import org.gatein.wci.test.WebAppRegistry;
import org.gatein.wci.test.crosscontext.AbstractCrossContextTestCase;
import org.gatein.wci.test.crosscontext.CrossContextServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CrossContextTestCase extends AbstractCrossContextTestCase
{

   @Deployment(name = "crosscontextwci")
   public static WebArchive wciDeployment()
   {
      WebArchive war = wciJBoss7Deployment("crosscontextwci.war");
      war.setWebXML("org/gatein/wci/test/jboss7/crosscontext/web.xml");
      war.addAsWebInfResource("org/gatein/wci/test/jboss7/crosscontext/jboss-web.xml", "jboss-web.xml");
      war.addClass(WebAppRegistry.class);
      war.addClass(CrossContextServlet.class);
      war.addClass(AbstractCrossContextTestCase.class);
      return war;
   }
}
