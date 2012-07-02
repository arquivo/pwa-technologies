/* Nutchwax
 * 
 * $Id: NutchwaxDistributedSearch.java 1896 2007-08-01 21:44:31Z jlee-archive $
 * 
 * Created on November 14th, 2006
 *
 * Copyright (C) 2006 Internet Archive.
 * 
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 * 
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 * 
 * Heritrix is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.access.nutch;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.ipc.RPC;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.global.Global;


/** 
 * Script to start up a Nutchwax Distributed Searcher.
 * @author stack
 * @version $Revision: 1896 $ $Date: 2007-08-01 22:44:31 +0100 (Wed, 01 Aug 2007) $
 */
public class NutchwaxDistributedSearch
{
	public static class Server
	{
		private Server()
		{
			super();
		}

		/** 
		 * Use to start org.apache.nutch.searcher.DistributedSearch$Server
		 * but with nutchwax configuration mixed in so nutchwax plugins
		 * can be found (and properly configured).
		 */
		public static void main(String[] args) throws Exception
		{
			String usage =
				"NutchwaxDistributedSearch$Server <port> <index dir> <blacklist dir>";

			if (args.length == 0 || args.length > 3) {
				System.err.println(usage);
				System.exit(-1);
			}

			int port = Integer.parseInt(args[0]);
			Path directory = new Path(args[1]);
			Path blacklistDir = null;
			if (args.length==3 && args[2]!=null) {
				blacklistDir = new Path(args[2]);
			}

			Configuration conf = NutchwaxConfiguration.getConfiguration();
			NutchBean bean = new NutchBean(conf, directory, blacklistDir);
			int numHandlers=conf.getInt(Global.NUMBER_HANDLERS, -1);
		    boolean ipcVerbose=conf.getBoolean(Global.IPC_VERBOSE, false);

			org.apache.hadoop.ipc.Server server = RPC.getServer(bean,
					"0.0.0.0", port, numHandlers, ipcVerbose, conf);
								   
			server.start();
			server.join();
		}
	}
}