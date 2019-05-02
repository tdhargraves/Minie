/*
 * Copyright (c) 2019 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.bullet.objects.infos;

import java.util.logging.Logger;

/**
 * Named collision flags for use with a PhysicsSoftBody.Config
 *
 * @author Stephen Gold sgold@sonic.net
 * @see com.jme3.bullet.objects.PhysicsSoftBody.Config#getCollisionsFlags(long)
 */
public class ConfigFlags {
    // *************************************************************************
    // constants and loggers

    /**
     * enable the SDF-based handler for rigid-versus-soft collisions
     */
    public final static int SDF_RS = 0x1;
    /**
     * enable the Cluster-versus-Convex handler for rigid-versus-soft collisions
     */
    public final static int CL_RS = 0x2;
    /**
     * enable the Vertex-versus-Face handler for soft-versus-soft collisions
     */
    public final static int VF_SS = 0x10;
    /**
     * enable the Cluster-versus-Cluster handler for soft-versus-soft collisions
     */
    public final static int CL_SS = 0x20;
    /**
     * enable self collisions for clusters
     */
    public final static int CL_SELF = 0x40;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(ConfigFlags.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private ConfigFlags() {
    }
}
