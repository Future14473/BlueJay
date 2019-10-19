package org.firstinspires.ftc.teamcode.Original.hardware

/**
 * Represents a Spinnable. Links directly to hardware (or simulation).
 */
interface Spinnable : InitStoppable {

    /**
     * The max voltage this motor can take.
     */
    val maxVoltage: Double

    /**
     * Powers the motor with the supplied [voltage] normalizing in bounds if necessary.
     */
    var voltage: Double

    /**
     * Gets the position ***in radians*** of this motor; with the same sign direction as [voltage]
     */
    val position: Double

    /**
     * Gets the velocity ***in radians per second*** of this motor, in the same direction as [voltage]
     */
    val velocity: Double

    /**
     * Resets the position so that the current position is 0.0; Following position gets calls will be relative
     * to this position.
     */
    fun resetPosition()

    /**
     * Performs any necessary configurations on this motor before start.
     */
    override fun init()

    /**
     * Stops this motor completely.
     */
    override fun stop() {
        voltage = 0.0
    }
}