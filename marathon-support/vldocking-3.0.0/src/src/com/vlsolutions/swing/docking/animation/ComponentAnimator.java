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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;


/** Utility class used to perform move/resize animation for awt/swing components.
 * <p>
 * This class moves/resizes a Component given a start and end Rectangle and
 * a duration.
 * <p>
 * Movements and listeners notifications are processed in the Swing Event Thread.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 * */
public class ComponentAnimator {
  /** The animated component */
  protected Component comp;

  /** the component's start bounds */
  protected Rectangle startBounds;

  /** the component's end bounds */
  protected Rectangle endBounds;

  /** the animation duration in seconds */
  protected float duration;

  /** animation start time System.currentTimeMillis() */
  private long start;

  /** time elapsed since the beginnig of animation */
  protected float elapsed = 0;

  private Timer timer;

  private ArrayList listeners = new ArrayList(3);


  /** Single-shot animator (use another ComponentAnimator for a new animation).
   *
   *
   * @param comp     the component to animate
   * @param startBounds  initial bounds of the component
   * @param endBounds    end bounds of the component
   * @param duration     duration of animation, expressed in seconds
   * @param listener     single listener used for animation notification
   */
  public ComponentAnimator(Component comp, Rectangle startBounds,
      Rectangle endBounds, float duration, AnimationListener listener) {
    this.comp = comp;
    this.startBounds = startBounds;
    this.endBounds = endBounds;
    this.duration = duration;
    addAnimationListener(listener);
    start();

  }

  /** Reusable component animator.
   * To start an animation, don't forget to call the {@link #start() start() }method.
   * */
  public ComponentAnimator(Component comp, Rectangle startBounds,
      Rectangle endBounds, float duration) {
    this.comp = comp;
    this.startBounds = startBounds;
    this.endBounds = endBounds;
    this.duration = duration;
  }


  /** Starts the animation.
   * <P> The component is <code>setBounds</code>ed to startBounds and made visible,
   * than a Swing timer is started to process the animation (refresh rate is 100 ms).
   * <P> the ANIMATION_START event is then fired to all listeners.
   */
  public void start(){
      if (duration == 0 ){ // heavy weight == no animation
        fireAnimationEvent(new AnimationEvent(comp, AnimationEvent.ANIMATION_START));
        // already the end
        comp.setBounds(endBounds.x, endBounds.y, endBounds.width, endBounds.height);
        comp.invalidate();
        comp.validate();
        comp.repaint();
        fireAnimationEvent(new AnimationEvent(comp, AnimationEvent.ANIMATION_END));          
      } else {
        comp.setBounds(startBounds.x, startBounds.y,
            startBounds.width,
            startBounds.height);
        comp.validate();
        comp.setVisible(true);
        timer = new Timer(100, new AnimationActionListener());
        start = System.currentTimeMillis();
        timer.start();
        fireAnimationEvent(new AnimationEvent(comp, AnimationEvent.ANIMATION_START));
      }
    }

  /** Cancels the animation (the component is not reset to its initial location/size) */
  public void cancel(){
     timer.stop();
  }


  /** Adds a new listener to the animator
   * @param listener  the listener  */
  public void addAnimationListener(AnimationListener listener) {
    if (listener != null && ! listeners.contains(listener)){
      listeners.add(listener);
    }
  }

  /** loops over the listeners to fire animation event */
  private void fireAnimationEvent(AnimationEvent e){
    for (int i = 0; i < listeners.size(); i++) {
      ((AnimationListener) listeners.get(i)).animation(e);
    }
  }

  /** Returns the duration of the animation
   *
   * @return the duration of the animation, in seconds
   */
  public float getDuration() {
    return duration;
  }

  /** Sets the duration of the animation.
   * <P> Warning : do not change this value during an animation
   *
   * @param duration the new duration in seconds
   */
  public void setDuration(float duration) {
    this.duration = duration;
  }

  /** Returns the end bounds of the components.
   *
   * @return the end bounds of the components.
   */
  public Rectangle getEndBounds() {
    return endBounds;
  }

  /** Sets the end bounds of the component.
   * <P>Warning : do not change end bounds during an animation.
   *
   * @param endBounds
   */
  public void setEndBounds(Rectangle endBounds) {
    this.endBounds = endBounds;
  }

  /** Returns the start bounds of the component.
   *
   * @return the start bounds of the component (those of when animation starts).
   */
  public Rectangle getStartBounds() {
    return startBounds;
  }

  /** Sets the start bounds of the component for animation.
   * <P> Warning :do not change start bounds during an animation
   * @param startBounds the start bounds of the component.
   */
  public void setStartBounds(Rectangle startBounds) {
    this.startBounds = startBounds;
  }

  /** Inner action listener to hide actionPerformed from the public API. */
  private class AnimationActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      long time = System.currentTimeMillis();
      elapsed = (time - start) / 1000f;
      if (elapsed < duration) {
        float f1 = (duration - elapsed) / duration;
        float f2 = elapsed / duration;
        Rectangle newBounds = new Rectangle();
        newBounds.x = (int) (startBounds.x * f1 + endBounds.x * f2);
        newBounds.y = (int) (startBounds.y * f1 + endBounds.y * f2);
        newBounds.width = (int) (startBounds.width * f1 + endBounds.width * f2);
        newBounds.height = (int) (startBounds.height * f1 +
            endBounds.height * f2);
        comp.setBounds(newBounds.x, newBounds.y,
            newBounds.width,
            newBounds.height);
        comp.validate();
        fireAnimationEvent(new AnimationEvent(comp,
            AnimationEvent.ANIMATION_FRAME));
      } else {

        // the end
        comp.setBounds(endBounds.x, endBounds.y,
            endBounds.width,
            endBounds.height);
        comp.validate();
        timer.stop();
        fireAnimationEvent(new AnimationEvent(comp,
            AnimationEvent.ANIMATION_END));

      }

    }
  }

}
