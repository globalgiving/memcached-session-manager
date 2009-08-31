/*
 * Copyright 2009 Martin Grotzke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.javakaffee.web.msm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.NodeLocator;
import net.spy.memcached.ops.Operation;

/**
 * Locates nodes based on their id which is a part of the sessionId (key).
 * 
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 * @version $Id$
 */
class SuffixBasedNodeLocator implements NodeLocator {
    
    // private final Logger _logger = Logger.getLogger( SuffixBasedNodeLocator.class.getName() );

    private final List<MemcachedNode> _nodes;
    private final NodeIdResolver _resolver;
    private final Map<String, MemcachedNode> _nodesMap;
    private final SessionIdFormat _sessionIdFormat;

    /**
     * Create a new {@link SuffixBasedNodeLocator}.
     * @param nodes the nodes to select from.
     * @param resolver used to resolve the node id for the address of a memcached node.
     * @param sessionIdFormat used to extract the node id from the session id.
     */
    public SuffixBasedNodeLocator( List<MemcachedNode> nodes,
            NodeIdResolver resolver,
            SessionIdFormat sessionIdFormat ) {
        _nodes = nodes;
        _resolver = resolver;
        
        final Map<String,MemcachedNode> map = new HashMap<String, MemcachedNode>( nodes.size(), 1 );
        for ( int i = 0; i < nodes.size(); i++ ) {
            final MemcachedNode memcachedNode = nodes.get( i );
            final String nodeId = resolver.getNodeId( (InetSocketAddress) memcachedNode.getSocketAddress() );
            map.put( nodeId, memcachedNode );
        }
        _nodesMap = map;
        
        _sessionIdFormat = sessionIdFormat;
    }

    @Override
    public Collection<MemcachedNode> getAll() {
        return _nodesMap.values();
    }

    @Override
    public MemcachedNode getPrimary( String key ) {
        final MemcachedNode result = _nodesMap.get( getNodeId( key ) );
        if ( result == null ) {
            throw new IllegalArgumentException( "No node found for key " + key );
        }
        return result;
    }

    private String getNodeId( String key ) {
        return _sessionIdFormat.extractMemcachedId( key );
    }

    @Override
    public Iterator<MemcachedNode> getSequence( String key ) {
        final String nodeId = getNodeId( key );
        throw new NodeFailureException( "The node " + nodeId + " is not available.", nodeId );
    }

    @Override
    public NodeLocator getReadonlyCopy() {
        final List<MemcachedNode> nodes = new ArrayList<MemcachedNode>();
        for ( MemcachedNode node : _nodes ) {
            nodes.add( new MyMemcachedNodeROImpl( node ) );
        }
        return new SuffixBasedNodeLocator( nodes, _resolver, _sessionIdFormat );
    }
    
    static class MyMemcachedNodeROImpl implements MemcachedNode {

        private final MemcachedNode _root;

        public MyMemcachedNodeROImpl(MemcachedNode node) {
            _root = node;
        }

        @Override
        public String toString() {
            return _root.toString();
        }

        public void addOp(Operation op) {
            throw new UnsupportedOperationException();
        }

        public void connected() {
            throw new UnsupportedOperationException();
        }

        public void copyInputQueue() {
            throw new UnsupportedOperationException();
        }

        public void fillWriteBuffer(boolean optimizeGets) {
            throw new UnsupportedOperationException();
        }

        public void fixupOps() {
            throw new UnsupportedOperationException();
        }

        public int getBytesRemainingToWrite() {
            throw new UnsupportedOperationException();
        }

        public SocketChannel getChannel() {
            throw new UnsupportedOperationException();
        }

        public Operation getCurrentReadOp() {
            throw new UnsupportedOperationException();
        }

        public Operation getCurrentWriteOp() {
            throw new UnsupportedOperationException();
        }

        public ByteBuffer getRbuf() {
            throw new UnsupportedOperationException();
        }

        public int getReconnectCount() {
            throw new UnsupportedOperationException();
        }

        public int getSelectionOps() {
            throw new UnsupportedOperationException();
        }

        public SelectionKey getSk() {
            throw new UnsupportedOperationException();
        }

        public SocketAddress getSocketAddress() {
            return _root.getSocketAddress();
        }

        public ByteBuffer getWbuf() {
            throw new UnsupportedOperationException();
        }

        public boolean hasReadOp() {
            throw new UnsupportedOperationException();
        }

        public boolean hasWriteOp() {
            throw new UnsupportedOperationException();
        }

        public boolean isActive() {
            throw new UnsupportedOperationException();
        }

        public void reconnecting() {
            throw new UnsupportedOperationException();
        }

        public void registerChannel(SocketChannel ch, SelectionKey selectionKey) {
            throw new UnsupportedOperationException();
        }

        public Operation removeCurrentReadOp() {
            throw new UnsupportedOperationException();
        }

        public Operation removeCurrentWriteOp() {
            throw new UnsupportedOperationException();
        }

        public void setChannel(SocketChannel to) {
            throw new UnsupportedOperationException();
        }

        public void setSk(SelectionKey to) {
            throw new UnsupportedOperationException();
        }

        public void setupResend() {
            throw new UnsupportedOperationException();
        }

        public void transitionWriteItem() {
            throw new UnsupportedOperationException();
        }

        public int writeSome() throws IOException {
            throw new UnsupportedOperationException();
        }

        public Collection<Operation> destroyInputQueue() {
            throw new UnsupportedOperationException();
        }
    }
    
}