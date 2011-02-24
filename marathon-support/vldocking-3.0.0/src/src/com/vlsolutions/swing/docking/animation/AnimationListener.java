/*
    VLDocking Framework 3.0
    Copyright VLSOLUTIONS, 2004-2009
    
    email : info at vlsolutions.com
------------------------------------------------------------------------
This software is distributed under the LGPL license

The fact that you are presently reading this and using this class means that you have had
knowledge of the LGPL license and that you accept its terms.

You can read the complete license here :

    http://www.gnu.org/licenses/lgpl.html

*/


package com.vlsolutions.swing.docking.animation;

/** The AnimationListener interface is used to notify listeners about the state
 * of an animation process (animation start and end).
 * <p>
 * This interface is used by ComponentAnimator, generally to block
 * events management during animation phase.
 *
 * @see ComponentAnimator
 * @see AnimationEvent
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public interface AnimationListener {

  /** This method is invoked when the animation state change.
   * <P> Changes are relative to animation start, sequence(frame) and end.
   * */
  public void animation(AnimationEvent e);

}
