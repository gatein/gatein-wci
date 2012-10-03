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
package org.gatein.wci.test.jetty8.crosscontext;

import org.gatein.wci.test.authentication.AuthenticationServlet;
import org.gatein.wci.test.crosscontext.AbstractCrossContextTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import java.net.URL;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 10/2/12
 */
public class CrossContextTestCase extends AbstractCrossContextTestCase
{
   @Deployment(name = "crosscontextwci")
   public static WebArchive wciDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "crosscontextwci.war");
      war.setWebXML("org/gatein/wci/test/jetty8/crosscontext/web.xml");
      war.addAsWebResource("org/gatein/wci/test/jetty8/crosscontext/jetty-web.xml", "WEB-INF/jetty-web.xml");
      war.addClass(AuthenticationServlet.class);

      return war;
   }

   /**
    * Override to skip this test ATM
    */
   @Override
   public void testFoo(@ArquillianResource @OperateOnDeployment("crosscontextwci") URL requestDispatchURL) throws Exception
   {
      //super.testFoo(requestDispatchURL);
   }
}
