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

import org.servalproject.servald.IPeerListListener;
import org.servalproject.servald.Peer;
import org.servalproject.servald.PeerComparator;
import org.servalproject.servald.PeerListService;
import org.servalproject.servaldna.SubscriberId;

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

	private static class Score {
		private static List<Peer> peers = new ArrayList<Peer>();
		private static List<Peer> neighbors = new ArrayList<Peer>();
		private static int neighborHopCount = Integer.MAX_VALUE;
	}

	private static PeerAPI self;

	private static IPeerListListener listener;

	Thread t1;

	private PeerAPI() {

		listener = new IPeerListListener() {
			@Override
			public void peerChanged(final Peer p) {
				t1 = new Thread(new Runnable() {
					public void run() {
						peerUpdated(p);
					}
				});
				t1.start();
				/*activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						peerUpdated(p);
					};

				});*/
			}
		};

	}

	public synchronized static PeerAPI getInstance()
	{
		if(self != null) {
			Score.peers.clear();
		}else {
			self = new PeerAPI();

		}

		PeerListService.addListener(listener);


		return self;

	}

	//****************************************
	//Peer info
	//****************************************

	public int getNumPeers(){
		return Score.peers.size();
	}

	public List<Peer> getPeers(){
		return Score.peers;
	}

	public int getPeerHopCount(SubscriberId subscriberId){
		for(Peer peer: Score.peers){
			if(peer.getSubscriberId() == subscriberId){
				return peer.getHopCount();
			}
		}
		return -1;
	}

	public int getPeerHopCount(String name){
		for(Peer peer: Score.peers){
			if(peer.getDisplayName() == name){
				return peer.getHopCount();
			}
		}
		return -1;
	}

	public int getPeerHopCount(int phoneNumber){
		for(Peer peer: Score.peers){
			if(peer.getDid() == Integer.toString(phoneNumber)){
				return peer.getHopCount();
			}
		}
		return -1;
	}

	public double getPeerRSSI(SubscriberId subscriberId){
		for(Peer peer: Score.peers){
			if(peer.getSubscriberId() == subscriberId){
				return peer.getRSSI();
			}
		}
		return -1.0;
	}

	//****************************************
	//Neighbor info
	//****************************************

	public int getNumNeighbors(){
		return Score.neighbors.size();
	}

	public List<Peer> getNeighbors(){
		return Score.neighbors;
	}

	public void removeListener(){
		PeerListService.removeListener(listener);
	}

	private void peerUpdated(Peer p) {
		if (!Score.peers.contains(p)){
			if (!p.isReachable())
				return;

			Score.peers.add(p);

			if(p.getHopCount() < Score.neighborHopCount){
				Score.neighborHopCount = p.getHopCount();
				Score.neighbors.clear();

				for(Peer peer: Score.peers){
					if(peer.getHopCount() == Score.neighborHopCount){
						Score.neighbors.add(peer);
					}
				}
			}else if(p.getHopCount() < Score.neighborHopCount){
				Score.neighbors.add(p);
			}

			Collections.sort(Score.peers, new PeerComparator());
			Collections.sort(Score.neighbors, new PeerComparator());

		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		removeListener();
		super.finalize();
	}

}
