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
package com.jme3.bullet.joints.motors;

import java.util.logging.Logger;

/**
 * A single-axis motor based on Bullet's btRotationalLimitMotor2, used to
 * control the rotation of a New6Dof constraint.
 *
 * @author sgold@sonic.net
 */
public class RotationMotor {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(RotationMotor.class.getName());
    // *************************************************************************
    // fields

    /**
     * Unique identifier of the btRotationalLimitMotor2. The constructor sets
     * this to a non-zero value. After that, the ID never changes.
     */
    private long motorId = 0L;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a motor for the identified btRotationalLimitMotor2.
     *
     * @param motor the unique identifier (not zero)
     */
    public RotationMotor(long motor) {
        assert motor != 0L;
        motorId = motor;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Read the specified parameter of this motor.
     *
     * @param param which parameter (not null)
     * @return the parameter value
     */
    public float get(MotorParam param) {
        float result;

        switch (param) {
            case Bounce:
                result = getBounce(motorId);
                break;

            case Damping:
                result = getDamping(motorId);
                break;

            case Equilibrium:
                result = getEquilibrium(motorId);
                break;

            case LowerLimit:
                result = getLowerLimit(motorId);
                break;

            case MaxMotorForce:
                result = getMaxMotorForce(motorId);
                break;

            case ServoTarget:
                result = getServoTarget(motorId);
                break;

            case Stiffness:
                result = getStiffness(motorId);
                break;

            case TargetVelocity:
                result = getTargetVelocity(motorId);
                break;

            case UpperLimit:
                result = getUpperLimit(motorId);
                break;

            default:
                int paramIndex = param.nativeIndex();
                result = getParameter(motorId, paramIndex);
        }

        return result;
    }

    /**
     * Read the ID of the btRotationalLimitMotor2.
     *
     * @return the unique identifier (not zero)
     */
    public long getMotor() {
        assert motorId != 0L;
        return motorId;
    }

    /**
     * Test whether the spring's damping is limited (m_springDampingLimited).
     *
     * @return true if limited, otherwise false
     */
    public boolean isDampingLimited() {
        boolean result = isDampingLimited(motorId);
        return result;
    }

    /**
     * Test whether this motor is enabled (m_enableMotor).
     *
     * @return true if enabled, otherwise false
     */
    public boolean isMotorEnabled() {
        boolean result = isMotorEnabled(motorId);
        return result;
    }

    /**
     * Test whether the servo is enabled (m_servoMotor).
     *
     * @return true if enabled, otherwise false
     */
    public boolean isServoEnabled() {
        boolean result = isServoEnabled(motorId);
        return result;
    }

    /**
     * Test whether the spring is enabled (m_enableSpring).
     *
     * @return true if enabled, otherwise false
     */
    public boolean isSpringEnabled() {
        boolean result = isSpringEnabled(motorId);
        return result;
    }

    /**
     * Test whether the spring's stiffness is limited
     * (m_springStiffnessLimited).
     *
     * @return true if limited, otherwise false
     */
    public boolean isStiffnessLimited() {
        boolean result = isStiffnessLimited(motorId);
        return result;
    }

    /**
     * Alter the specified parameter of this motor.
     *
     * @param param which parameter (not null)
     * @param value the desired parameter value
     */
    public void set(MotorParam param, float value) {
        switch (param) {
            case Bounce:
                setBounce(motorId, value);
                break;

            case Damping:
                setDamping(motorId, value);
                break;

            case Equilibrium:
                setEquilibrium(motorId, value);
                break;

            case LowerLimit:
                setLowerLimit(motorId, value);
                break;

            case MaxMotorForce:
                setMaxMotorForce(motorId, value);
                break;

            case ServoTarget:
                setServoTarget(motorId, value);
                break;

            case Stiffness:
                setStiffness(motorId, value);
                break;

            case TargetVelocity:
                setTargetVelocity(motorId, value);
                break;

            case UpperLimit:
                setUpperLimit(motorId, value);
                break;

            default:
                int paramIndex = param.nativeIndex();
                setParameter(motorId, paramIndex, value);
        }
    }

    /**
     * Limit or unlimit the spring's damping (m_springDampingLimited).
     *
     * @param limitDamping true&rarr;limit, false&rarr;don't limit
     * (default=false)
     */
    public void setDampingLimited(boolean limitDamping) {
        setDampingLimited(motorId, limitDamping);
    }

    /**
     * Enable or disable this motor (m_enableMotor).
     *
     * @param enableFlag true&rarr;enable, false&rarr;disable (default=false)
     */
    public void setMotorEnabled(boolean enableFlag) {
        setMotorEnabled(motorId, enableFlag);
    }

    /**
     * Enable or disable the servo (m_servoMotor).
     *
     * @param enableFlag true&rarr;enable, false&rarr;disable (default=false)
     */
    public void setServoEnabled(boolean enableFlag) {
        setServoEnabled(motorId, enableFlag);
    }

    /**
     * Enable or disable the spring (m_enableSpring).
     *
     * @param enableFlag true&rarr;enable, false&rarr;disable (default=false)
     */
    public void setSpringEnabled(boolean enableFlag) {
        setSpringEnabled(motorId, enableFlag);
    }

    /**
     * Limit or unlimit the spring's stiffness (m_springStiffnessLimited).
     *
     * @param limitStiffness true&rarr;limit, false&rarr;don't limit
     * (default=false)
     */
    public void setStiffnessLimited(boolean limitStiffness) {
        setStiffnessLimited(motorId, limitStiffness);
    }
    // *************************************************************************
    // native methods

    native private float getBounce(long motorId);

    native private float getDamping(long motorId);

    native private float getEquilibrium(long motorId);

    native private float getLowerLimit(long motorId);

    native private float getMaxMotorForce(long motorId);

    native private float getParameter(long motorId, int parameterIndex);

    native private float getServoTarget(long motorId);

    native private float getStiffness(long motorId);

    native private float getTargetVelocity(long motorId);

    native private float getUpperLimit(long motorId);

    native private boolean isDampingLimited(long motorId);

    native private boolean isMotorEnabled(long motorId);

    native private boolean isServoEnabled(long motorId);

    native private boolean isSpringEnabled(long motorId);

    native private boolean isStiffnessLimited(long motorId);

    native private void setBounce(long motorId, float bounce);

    native private void setDamping(long motorId, float damping);

    native private void setDampingLimited(long motorId, boolean limitFlag);

    native private void setEquilibrium(long motorId, float angle);

    native private void setLowerLimit(long motorId, float angle);

    native private void setMaxMotorForce(long motorId, float force);

    native private void setMotorEnabled(long motorId, boolean enableFlag);

    native private void setParameter(long motorId, int parameterIndex,
            float value);

    native private void setServoEnabled(long motorId, boolean enableFlag);

    native private void setServoTarget(long motorId, float target);

    native private void setSpringEnabled(long motorId, boolean enableFlag);

    native private void setStiffness(long motorId, float stiffness);

    native private void setStiffnessLimited(long motorId, boolean limitFlag);

    native private void setTargetVelocity(long motorId, float velocity);

    native private void setUpperLimit(long motorId, float angle);
}
