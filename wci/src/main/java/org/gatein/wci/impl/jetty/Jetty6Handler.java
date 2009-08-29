//package org.jboss.portal.web.impl.jetty;
//
//import java.io.IOException;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.mortbay.jetty.Server;
//import org.mortbay.jetty.handler.AbstractHandler;
//
//public class Jetty6Handler extends AbstractHandler
//{
//	Server server;
//	Jetty6ServletContainerContext containerContext;
//	
//	public Jetty6Handler (Server server)
//	{
//		this.server = server;
//		System.out.println("SERVER : " + server);
//		server.addHandler(this);
//	}
//	
//	protected void doStart() throws Exception {
//		super.doStart();
//        containerContext = new Jetty6ServletContainerContext(server);
//        containerContext.start();
//	}
//	
//	protected void doStop() throws Exception {
//		super.doStop();
//		
//		if (containerContext != null)
//		{
//			containerContext.stop();
//			containerContext = null;
//		}
//	}
//
//	public void handle(String target, HttpServletRequest request,
//			HttpServletResponse response, int dispatch) throws IOException,
//			ServletException {
//		// Do Nothing for now. This doesn't actually handle anything, but needs to be a handler
//		// to tie in the jetty lifecycle and to be able to have access to the server object..
//	}
//	
//}