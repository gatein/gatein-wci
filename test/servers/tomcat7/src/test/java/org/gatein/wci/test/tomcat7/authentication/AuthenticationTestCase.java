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
package org.gatein.wci.test.tomcat7.authentication;

import org.gatein.wci.test.authentication.AbstractAuthenticationTestCase;
import org.gatein.wci.test.authentication.AuthenticationServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AuthenticationTestCase extends AbstractAuthenticationTestCase
{

   @Deployment
   public static WebArchive wciDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class);
      war.setWebXML("org/gatein/wci/test/tomcat7/authentication/web.xml");
      war.addAsManifestResource("org/gatein/wci/test/tomcat7/authentication/context.xml", "context.xml");
      war.addClass(AuthenticationServlet.class);
      war.addClass(AbstractAuthenticationTestCase.class);

      //
      return war;
   }
}
