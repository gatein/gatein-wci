package org.gatein.wci.test.tomcat7.servletlistener;

import org.gatein.wci.test.authentication.AuthenticationServlet;
import org.gatein.wci.test.servletlistener.AbstractServletListenerTestCase;
import org.gatein.wci.test.servletlistener.ListenerServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class ServletListenerTestCase extends AbstractServletListenerTestCase
{

   static
   {
      AbstractServletListenerTestCase.webXml = "org/gatein/wci/test/tomcat7/servletlistener/listener_web.xml";
   }

   @Deployment(name = "servletlistenerwci")
   public static WebArchive wciDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "servletlistenerwci.war");
      war.setWebXML("org/gatein/wci/test/tomcat7/servletlistener/web.xml");
      return war;
   }
}
