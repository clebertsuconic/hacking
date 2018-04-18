/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.qpid.proton4j.engine.state;

import org.apache.qpid.proton4j.amqp.transport.ErrorCondition;

public abstract class Endpoint
{
    private EndpointState _localState = EndpointState.UNINITIALIZED;
    private EndpointState _remoteState = EndpointState.UNINITIALIZED;
    private ErrorCondition _condition;
    private ErrorCondition _remoteError;

    private Object _context;

    private int refcount = 1;
    boolean freed = false;

    void incref() {
        refcount++;
    }

    void decref() {
        refcount--;
        if (refcount == 0) {
            postFinal();
        } else if (refcount < 0) {
            throw new IllegalStateException();
        }
    }

    abstract void postFinal();

    abstract void localOpen();

    abstract void localClose();

    public void open()
    {
        if (getLocalState() != EndpointState.ACTIVE)
        {
            _localState = EndpointState.ACTIVE;
            localOpen();
            modified();
        }
    }

    public void close()
    {
        if (getLocalState() != EndpointState.CLOSED)
        {
            _localState = EndpointState.CLOSED;
            localClose();
            modified();
        }
    }

    public EndpointState getLocalState()
    {
        return _localState;
    }

    public EndpointState getRemoteState()
    {
        return _remoteState;
    }

    public ErrorCondition getCondition()
    {
        return _condition;
    }

    public void setCondition(ErrorCondition condition)
    {
        this._condition = condition;
    }

    public ErrorCondition getRemoteCondition()
    {
        return _remoteError;
    }

    public void setLocalState(EndpointState localState)
    {
        _localState = localState;
    }

    public void setRemoteState(EndpointState remoteState)
    {
        // TODO - check state change legal
        _remoteState = remoteState;
    }

    void modified()
    {
        modified(true);
    }

    void modified(boolean emit)
    {
    }

    abstract void doFree();

    final public void free()
    {
        if (freed) return;
        freed = true;

        doFree();
        decref();
    }

    public Object getContext()
    {
        return _context;
    }

    public void setContext(Object context)
    {
        _context = context;
    }


}
