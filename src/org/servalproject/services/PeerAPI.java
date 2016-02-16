/**
 * Copyright (C) 2011 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.servalproject.services;

import android.app.Activity;
import org.servalproject.servald.IPeerListListener;
import org.servalproject.servald.Peer;
import org.servalproject.servald.PeerComparator;
import org.servalproject.servald.PeerListService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 *     	   @author Jason Wong <jlwong@iastate.edu>
 *
 *         Retrieve peer and neighbor information
 */
public class PeerAPI {

	private static PeerAPI self;

	private static List<Peer> peers = new ArrayList<Peer>();
	private static List<Peer> neighbors = new ArrayList<Peer>();

	private static int neighborHopCount = Integer.MAX_VALUE;

	private static Activity activity;
	private static IPeerListListener listener;

	public PeerAPI(Activity callingActivity) {

		activity = callingActivity;

		listener = new IPeerListListener() {
			@Override
			public void peerChanged(final Peer p) {
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						peerUpdated(p);
					};

				});
			}
		};

	}

	public synchronized static PeerAPI getInstance(Activity activity)
	{
		if(self != null) {
			peers.clear();
		}else {
			self = new PeerAPI(activity);

		}

		PeerListService.addListener(listener);
		return self;

	}

	public int getNumPeers(){
		return peers.size();
	}

	public List<Peer> getPeers(){
		return peers;
	}

	public int getNumNeighbors(){
		return neighbors.size();
	}

	public List<Peer> getNeighbors(){
		return neighbors;
	}

	public void removeListener(){
		PeerListService.removeListener(listener);
	}

	private void peerUpdated(Peer p) {
		if (!peers.contains(p)){
			if (!p.isReachable())
				return;

			peers.add(p);

			if(p.getHopCount() < neighborHopCount){
				neighborHopCount = p.getHopCount();
				neighbors.clear();

				for(Peer peer: peers){
					if(peer.getHopCount() == neighborHopCount){
						neighbors.add(peer);
					}
				}
			}else if(p.getHopCount() < neighborHopCount){
				neighbors.add(p);
			}

		}

		Collections.sort(peers, new PeerComparator());
		Collections.sort(neighbors, new PeerComparator());
	}

	@Override
	protected void finalize() throws Throwable
	{
		removeListener();
		super.finalize();
	}

}
