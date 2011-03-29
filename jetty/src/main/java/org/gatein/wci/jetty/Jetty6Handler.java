package org.gatein.wci.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

public class Jetty6Handler extends AbstractHandler
{
   private static final Logger log = LoggerFactory.getLogger(Jetty6Handler.class);

   /** Servlet context init parameter name that can be used to turn off cross-context logout */
   private static final String CROSS_CONTEXT_LOGOUT_KEY = "org.gatein.wci.cross_context_logout";

	Server server;
	Jetty6ServletContainerContext containerContext;

	public Jetty6Handler (Server server)
	{
		this.server = server;
		System.out.println("SERVER : " + server);
		server.addHandler(this);
	}
	
	protected void doStart() throws Exception {
		super.doStart();
        containerContext = new Jetty6ServletContainerContext(server);
        containerContext.setCrossContextLogout(getCrossContextLogoutConfig());
        containerContext.start();
	}
	
	protected void doStop() throws Exception {
		super.doStop();
		
		if (containerContext != null)
		{
			containerContext.stop();
			containerContext = null;
		}
	}

	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {
		// Do Nothing for now. This doesn't actually handle anything, but needs to be a handler
		// to tie in the jetty lifecycle and to be able to have access to the server object..
	}

   private boolean getCrossContextLogoutConfig() {

      String val = (String) server.getAttribute(CROSS_CONTEXT_LOGOUT_KEY);
      if (val == null || Boolean.valueOf(val))
         return true;

      if (!"false".equalsIgnoreCase(val))
         log.warn("Context init param " + CROSS_CONTEXT_LOGOUT_KEY + " value is invalid: " + val + " - falling back to: false");

      log.info("Cross-context session invalidation on logout disabled");
      return false;
   }
}