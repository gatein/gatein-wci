/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2008, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wci.endpoint;

import org.xml.sax.SAXException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.gatein.common.xml.XMLTools;
import org.gatein.common.io.IOTools;
import org.gatein.common.text.FastURLDecoder;
import org.gatein.wci.WebRequest;
import org.gatein.wci.WebResponse;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * A web end point.
 *
 * @author <a href="mailto:julien@jboss-portal.org">Julien Viet</a>
 * @version $Revision: 630 $
 */
public abstract class EndPointServlet extends HttpServlet
{

   /** . */
   private final static Logger log = Logger.getLogger(EndPointServlet.class);

   /** . */
   private FastURLDecoder decoder = FastURLDecoder.getUTF8Instance();

   /** Describes a default servlet mapping. */
   public static final int DEFAULT_SERVLET_MAPPING = 0;

   /** Describes a root path mapping. */
   public static final int ROOT_PATH_MAPPING = 1;

   /** Describes a path mapping. */
   public static final int PATH_MAPPING = 2;

   /** . */
   private Integer mappingType;

   public void init() throws ServletException
   {
      String servletName = getServletConfig().getServletName();

      //
      ServletContext servletContext = getServletContext();

      // XPath expression for selecting the url pattern for this servlet instance
      String exprValue = "//servlet-mapping[servlet-name='" + servletName + "']/url-pattern";
      XPath xpath = XPathFactory.newInstance().newXPath();
      XPathExpression expr;
      try
      {
         expr = xpath.compile(exprValue);
      }
      catch (XPathExpressionException e)
      {
         throw new ServletException(e);
      }

      //
      log.debug("Going to look for the configuration of the end point servlet " + servletName);

      // Obtain url pattern values in order to find out how this servlet instance is mapped
      InputStream in = servletContext.getResourceAsStream("/WEB-INF/web.xml");
      byte[] bytes;
      try
      {
         bytes = IOTools.getBytes(in);

         // That's the descriptor but we won't parse it as the encoding may not be correct, it's just here for debugging purpose
         String descriptor = new String(bytes);
         log.debug("The a priori descriptor is " + descriptor);
      }
      catch (IOException e)
      {
         throw new ServletException("The end point servlet " + servletName + " is not able to load the web descriptor");
      }
      finally
      {
         IOTools.safeClose(in);
      }

      //
      try
      {
         Document doc = XMLTools.getDocumentBuilderFactory().newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
         
         //
         NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);

         //
         for (int i = 0;i < nodes.getLength();i++)
         {
            Element urlPatternElt = (Element)nodes.item(i);

            //
            String urlPattern = XMLTools.asString(urlPatternElt, true);

            //
            log.debug("Found url pattern " + urlPattern + " for end point servlet " + servletName);

            //
            if (mappingType != null)
            {
               throw new ServletException("The same end point servlet " + servletName + " is mapped several times and this is not allowed");
            }

            //
            if (urlPattern.equals("/"))
            {
               mappingType = DEFAULT_SERVLET_MAPPING;
            }
            else if (urlPattern.equals("/*"))
            {
               mappingType = ROOT_PATH_MAPPING;
            }
            else if (urlPattern.startsWith("/") && urlPattern.endsWith("/*"))
            {
               mappingType = PATH_MAPPING;
            }
            else
            {
               throw new ServletException("The end point servlet " + servletName + " is mapped with a pattern value " + urlPattern);
            }
         }
      }
      catch (IOException e)
      {
         throw new ServletException(e);
      }
      catch (SAXException e)
      {
         throw new ServletException(e);
      }
      catch (ParserConfigurationException e)
      {
         throw new ServletException(e);
      }
      catch (XPathExpressionException e)
      {
         throw new ServletException(e);
      }
      finally
      {
         IOTools.safeClose(in);
      }

      //
      if (mappingType == null)
      {
         throw new ServletException("The same end point servlet " + servletName + " was not able to detect its mapping");
      }
   }

   protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      String requestURI = req.getRequestURI();
      String servletPath = req.getServletPath();
      String contextPath = req.getContextPath();

      // Determine the request path
      String webRequestPath = null;
      String webContextPath = null;
      switch (mappingType)
      {
         case DEFAULT_SERVLET_MAPPING:
            webRequestPath = requestURI.substring(contextPath.length());
            webContextPath = requestURI.substring(0, contextPath.length());
            break;
         case ROOT_PATH_MAPPING:
            webRequestPath = requestURI.substring(contextPath.length());
            webContextPath = requestURI.substring(0, contextPath.length());
            break;
         case PATH_MAPPING:
            webRequestPath = requestURI.substring(contextPath.length() + servletPath.length());
            webContextPath = requestURI.substring(0, contextPath.length() + servletPath.length());
            if (webRequestPath.length() == 0)
            {
               webRequestPath = "/";
            }
            break;
      }

      // Apply the url decoding
      webRequestPath = decoder.encode(webRequestPath);
      webContextPath = decoder.encode(webContextPath);

      //
      EndPointRequest wreq = new EndPointRequest(req, webRequestPath, webContextPath, mappingType);
      EndPointResponse wresp = new EndPointResponse(wreq, resp);

      //
      service(wreq, wresp);
   }

   protected abstract void service(WebRequest req, WebResponse resp) throws ServletException, IOException;

   /**
    * Returns the style of mapping of the endpoint.
    *
    * @return the mapping style
    */
   public Integer getMappingType()
   {
      return mappingType;
   }
}
