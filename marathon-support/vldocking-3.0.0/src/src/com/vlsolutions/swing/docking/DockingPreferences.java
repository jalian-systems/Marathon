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


package com.vlsolutions.swing.docking;

import com.vlsolutions.swing.docking.ui.DockingUISettings;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;

/** This is the centralized settings repository for managing docking look and feel.
 * <p>
 * It acts as a facade for installing common settings.
 *<p>
 * This class contains only high-level settings, like the heavyweight components support
 * or the global desktop style (shadowed or flat). To access low-level properties, use
 * the DockingUISettings class instead.
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 1.1
 * @see com.vlsolutions.swing.docking.ui.DockingUISettings
 */
public class DockingPreferences {

    private static boolean isLightWeightUsageEnabled = true;
    private static boolean isSingleHeavyWeightComponent = false;
    
            
    /** private stuff : use static methods only  */
    private DockingPreferences() {
    }
    
    public static final AutoHidePolicy getAutoHidePolicy(){
        return AutoHidePolicy.getPolicy();
    }
    
    /** A simple utility method to configure the docking framework to work properly with 
     * heavyweight component.
     *<p>
     * Don't forget to invoke also the following general swing methods : 
     * <pre>
     *      ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
     *      JPopupMenu.setDefaultLightWeightPopupEnabled(false);        
     *</pre>
     *<p>
     * If you are sure you will always use heavyweight component, you can replace this 
     * method invocation by initHeavyWeightUsage(), which will do all the work for you
     * (even the general swing methods invocation).
     * 
     */
    public static void setLightweigthComponentEnabled(boolean lightweight){
        isLightWeightUsageEnabled = lightweight;
    }
    
    public static boolean isLightWeightUsageEnabled(){
        return isLightWeightUsageEnabled;
    }

    /** Notifies the framework that there is only one dockable that is heavyweight.
     * <p> This allows optimizations and workarounds (for properly using the JDIC WebBrowser 
     *  for example).
     */
    public static void setSingleHeavyWeightComponent(boolean single){
        isSingleHeavyWeightComponent = single;
    }
    
    public static boolean isSingleHeavyWeightComponent(){
        return isSingleHeavyWeightComponent;
    }

    
    /** Facade method used to allow mixing of lightweight and heavyweight components in the 
     * desktop.
     * <p>
     *  This method is a shortcut for : 
     * <pre>
     *        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
     *        JPopupMenu.setDefaultLightWeightPopupEnabled(false);        
     *        setLightweigthComponentEnabled(false);
     *        getAutoHidePolicy().setExpansionDuration(0);
     * </pre>
     */
    public static void initHeavyWeightUsage(){
        // for now, there are only two methods used.
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);        
        setLightweigthComponentEnabled(false);
        getAutoHidePolicy().setExpansionDuration(0);
    }

    /** Facade method used to allow mixing of lightweight and heavyweight components in the 
     * desktop.
     */
    public static void initHeavyWeightUsage(boolean isSingleHeavyWeightComponent){
        initHeavyWeightUsage();
        setSingleHeavyWeightComponent(isSingleHeavyWeightComponent);
    }

    
    
    /** Convenience method to use the VLDocking 1.0 layout and border style (with shadows around dockables) */
    public static void setShadowDesktopStyle(){
      DockingUISettings.getInstance().installUI();
      UIManager.put("DockView.singleDockableBorder", new ShadowBorder());
      UIManager.put("DockView.tabbedDockableBorder", new ShadowBorder(false));
      UIManager.put("TabbedDockableContainer.tabPlacement", new Integer(SwingConstants.BOTTOM));      
    }

    /** Convenience method to use the VLDocking 2.0 layout and border style.
     * <p> 
     * The new "flat style" uses light borders around dockables and top tab placements.
     * <p>
     * This style is the new default one, so there is no need to call this method unless you want
     * to swap dynamically between desktop styles.
     */
    public static void setFlatDesktopStyle(){
      DockingUISettings.getInstance().installUI();
     
      // flat style is the default (outside : empty 1 pix / inside : hightlight-top-left + shadow-bottom-right
      Color shadow = UIManager.getColor("VLDocking.shadow");
      Color highlight = UIManager.getColor("VLDocking.highlight");

      Border innerFlatSingleBorder =  BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1,1,0,0, highlight),
        BorderFactory.createMatteBorder(0,0,1,1, shadow));

      Border flatSingleBorder = BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(1,1,1,1), 
          innerFlatSingleBorder);
      UIManager.put("DockView.singleDockableBorder", flatSingleBorder);
      UIManager.put("DockView.tabbedDockableBorder", null);
      UIManager.put("TabbedDockableContainer.tabPlacement", new Integer(SwingConstants.TOP));      
    }

    /** Another docking style with ligh dots between dockables.
     */
    public static void setDottedDesktopStyle(){
      DockingUISettings.getInstance().installUI();
     
      Color shadow = UIManager.getColor("VLDocking.shadow");
      Color highlight = UIManager.getColor("VLDocking.highlight");

      Border innerFlatSingleBorder =  BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1,1,0,0, highlight),
        BorderFactory.createMatteBorder(0,0,1,1, shadow));
      
      UIManager.put("DockView.singleDockableBorder", innerFlatSingleBorder);
          //BorderFactory.createLineBorder(shadow));
      UIManager.put("DockView.tabbedDockableBorder", null);
      UIManager.put("TabbedDockableContainer.tabPlacement", new Integer(SwingConstants.TOP));      
      
      UIManager.put("SplitContainer.drawDotsDelimitors", Boolean.TRUE);
    }

    /** Another docking style with custom tiles between dockables.
     */
    public static void setCustomSplitDesktopStyle(BufferedImage horizontalTile, 
        BufferedImage verticalTile){
      
      DockingUISettings.getInstance().installUI();
           
      //UIManager.put("DockView.singleDockableBorder", null);
      UIManager.put("DockView.tabbedDockableBorder", null);
      UIManager.put("TabbedDockableContainer.tabPlacement", new Integer(SwingConstants.TOP));      
      
      UIManager.put("SplitContainer.drawDotsDelimitors", Boolean.FALSE);
      UIManager.put("SplitContainer.hImage", horizontalTile);
      UIManager.put("SplitContainer.vImage", verticalTile);
    
      UIManager.put("SplitContainer.useCustomImages", Boolean.TRUE);
    }
    
}
