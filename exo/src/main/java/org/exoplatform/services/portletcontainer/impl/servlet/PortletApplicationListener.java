/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.portletcontainer.impl.servlet;

import javax.servlet.ServletContextEvent;
import org.gatein.wci.api.GateInServletListener;

/**
 * <p>This class is used as a wrapper around the GateInServletListener. This class
 * only exists to provide backwards compatiblity for portlets that have been setup to be
 * deployable on versions of eXo portal.</p>
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class PortletApplicationListener extends GateInServletListener {
}
