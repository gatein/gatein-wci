/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.gatein.wci.session;

import javax.servlet.http.HttpSession;

import org.gatein.wci.ServletContainerVisitor;
import org.gatein.wci.WebApp;

/**
 * Wrapper around {@link SessionTask}. The point of this visitor is the possibility to run given task on every existing HTTP session
 * with given sessionID in all deployed web applications
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SessionTaskVisitor implements ServletContainerVisitor
{

   private final String sessionId;
   private final SessionTask sessionTask;

   public SessionTaskVisitor(String sessionId, SessionTask sessionTask)
   {
      this.sessionId = sessionId;
      this.sessionTask = sessionTask;
   }

   @Override
   public void accept(WebApp webApp)
   {
      HttpSession session = webApp.getHttpSession(sessionId);
      if (session != null)
      {
         sessionTask.executeTask(session);
      }
   }
}
