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

import javax.swing.*;
import java.beans.*;
import java.util.HashMap;

/**
 * Provides a unique identification for a Dockable and runtime properties, like icon, name,
 * tooltip, preferred audohide-zone...
 * 
 *
 * <p>
 * As it is the object which <b>identifies uniquely a user Dockable component</b>,
 * it should be associated with one and only one <code>Dockable</code>.
 * <p>
 * The unique key used for equals() comparison is the <b>dockKey</b> property,
 * other properties can be shared by a set of DockKey (for example,
 * you can share an icon, or even a display name between
 * dockable Components).
 *
 * <p>
 * Another usage is the decoration of a dockable container, providing informations
 * such as its name, tooltip or icon.
 * <P>
 * Properties of a DockKey are listened to by the docking framework, so a change
 * of name or icon is reflected on the GUI without further programming.
 *
 * @author Lilian Chamontin, vlsolutions.
 * @version 1.0
 *
 * @update 2006/12/01 Lilian Chamontin : added client property support
 *
 */
public class DockKey {
  /** Key identifying a change in the tooltip property */
  public  static final String PROPERTY_TOOLTIP = "tooltip";
  
  /** Key identifying a change in the name property */
  public  static final String PROPERTY_NAME = "name";
  
  /** Key identifying a change in the tabname property (title used by tabbed containers) */
  public  static final String PROPERTY_TAB_NAME = "tabName";
  
  
  /**
   * Key identifying a change in the dockKey property
   */
  public  static final String PROPERTY_DOCKKEY = "dockKey";
  
  /** Key identifying a change in the icon property */
  public  static final String PROPERTY_ICON = "icon";
  
  /** Key identifying a change in the autohideEnabled property */
  public  static final String PROPERTY_AUTOHIDEABLE = "autohide";
  
  /** Key identifying a change in the closeEnabled property */
  public static final String PROPERTY_CLOSEABLE = "close";
  
  /** Key identifying a change in the maximizedEnabled property */
  public static final String PROPERTY_MAXIMIZABLE = "maximizable";
  
  /** Key identifying a change in the notification property */
  public static final String PROPERTY_NOTIFICATION = "notification";
  
  /** Key identifying a change in the floatableEnabled property */
  public static final String PROPERTY_FLOATABLE = "floatable";
  
  /**
   * Key identifying a change in the dockable dockableState
   */
  public static final String PROPERTY_DOCKABLE_STATE = "dockablestate";
  
  
  private String dockKey;
  private String name;
  /** new as 2.1 : the tab name property is used to display the dockable title when tabbed*/
  private String tabName;
  private String tooltip;
  private Icon icon;
  private DockingConstants.Hide autoHideBorder ;
  
  private boolean isAutoHideEnabled = true;
  private boolean isCloseEnabled = true;
  private boolean isMaximizeEnabled = true;
  private boolean isFloatEnabled = false; // for compatiblity with version 1.1
  
  /** resize weight of the dockable, this is not a bound property and should be set
   * during initialization.
   */
  private float resizeWeight = 0f;
  
  private boolean notification = false;

  /** additional client properties
   * @since 2.1.2
   */ 
  private HashMap clientProperties = null; 
  
  /**
   * Current visibility dockableState of the dockable (DockableState.CLOSED, STATE_AUTO_HIDE, DOCKED, MAXIMIZED,
   * FLOATING)
   */
  private DockableState.Location location = DockableState.Location.CLOSED;
  
  // these are not bound properties
  private DockGroup dockGroup;
  private DockableActionCustomizer actionCustomizer;
  
  
  private transient PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
  
  /**
   * JavaBeans constructor : If used, also think to set the dockKey property.
   */
  public DockKey() {
  }
  
  /**
   * Constructs a DockKey with dockKey (unique key) and name set to the same value
   */
  public DockKey(String dockKey){
    this(dockKey, dockKey, null, null, null);
  }
  
  /**
   * Constructs a DockKey with dockKey (unique key) and a displayed name
   */
  public DockKey(String dockKey, String name){
    this(dockKey, name, null, null, null);
  }
  
  /**
   * Constructs a DockKey with dockKey (unique key), a displayed name and a tooltip
   */
  public DockKey(String dockKey, String name, String tooltip){
    this(dockKey, name, tooltip, null, null);
  }
  
  /**
   * Constructs a DockKey with dockKey (unique key), a displayed name, a tooltip and
   * an icon.
   */
  public DockKey(String dockKey, String name, String tooltip, Icon icon){
    this(dockKey, name, tooltip, icon, null);
  }
  
  /**
   * Constructs a DockKey with dockKey (unique key), a displayed name, a tooltip, an icon
   * and a default autohide border.
   */
  public DockKey(String dockKey, String name, String tooltip, Icon icon, DockingConstants.Hide hideBorder){
    this.dockKey = dockKey;
    this.name = name;
    this.tooltip = tooltip;
    this.icon = icon;
    this.autoHideBorder = hideBorder;
  }
  
  /** Hook for property change notification */
  public void addPropertyChangeListener(PropertyChangeListener listener){
    propertySupport.addPropertyChangeListener(listener);
  }
  /** Remove a property change notification */
  public void removePropertyChangeListener(PropertyChangeListener listener){
    propertySupport.removePropertyChangeListener(listener);
    
  }
  
  /** Returns the icon displayed in title bars  */
  public Icon getIcon() {
    return icon;
  }
  /** Returns the name (or title) displayed in title bars  */
  public String getName() {
    return name;
  }
  
  /** Returns the tooltip associated to the title bar  */
  public String getTooltip() {
    if (tooltip == null) {
      return name;
    }
    return tooltip;
  }
  
  /** Returns the <b>unique id</b> designating the user component.
   *<p>
   * Note : This used to be the getDockName prior version 2.0. It has been renamed
   * to clarify the concept (there was a naming problem between getName() and getDockName()
   *
   */
  public String getKey() {
    return dockKey;
  }
  
  /** Updates the tooltip property.
   * <P> PropertyListeners are notified of that change
   * */
  public void setTooltip(String tooltip) {
    String old = this.tooltip;
    this.tooltip = tooltip;
    propertySupport.firePropertyChange(PROPERTY_TOOLTIP, old, tooltip);
  }
  
  /** Updates the name property.
   * The name property is used by dockable container headers to associate a title with a dockable.
   * <P> PropertyListeners are notified of that change
   * */
  public void setName(String name) {
    String old = this.name;
    this.name = name;
    propertySupport.firePropertyChange(PROPERTY_NAME, old, name);
  }
  
  /** Returns the tab name (or tab title) displayed when the component is contained into a tabbed container.
   */
  public String getTabName() {
    return tabName;
  }
  
  /** Updates the tabname property.
   * This property is used by tabbed containers to display a shorter version of the title of this dockable.
   * <p> 
   * Default value is null, meaning the name
   * <p> 
   * PropertyListeners are notified of that change
   *
   *@since 2.1
   * */
  public void setTabName(String tabName) {
    String old = this.tabName;
    this.tabName = tabName;
    propertySupport.firePropertyChange(PROPERTY_TAB_NAME, old, tabName);
  }
  
  
  /** Updates the icon property.
   * <P>
   * PropertyListeners are notified of that change */
  public void setIcon(Icon icon) {
    Icon old = this.icon;
    this.icon = icon;
    propertySupport.firePropertyChange(PROPERTY_ICON, old, icon);
  }
  
  /**
   * Updates the dockKey property.
   * <P>
   * Although PropertyListeners are notified of that change,
   * it is not recommended to change dynamicaly the dockKey property, as it is heavily used in
   * the docking framework to identify dockable components.
   * <p>
   * Note : This used to be the getDockName prior version 2.0. It has been renamed
   * to clarify the concept (there was a naming problem between getName() and getDockName()
   */
  public void setKey(String dockKey) {
    String old = this.dockKey;
    this.dockKey = dockKey;
    propertySupport.firePropertyChange(PROPERTY_DOCKKEY, old, dockKey);
  }
  
  /** @see #getKey()
   * @deprecated use getKey instead
   */
  public String getDockName(){
    return getKey();
  }
  
  /** @see #setKey(String)
   * @deprecated use setKey instead
   */
  public void setDockName(String name){
    setKey(name);
  }
  
  /** Returns the autohide border of this dockable, or null if not set*/
  public DockingConstants.Hide getAutoHideBorder(){
    return autoHideBorder;
  }
  
  /** Updates the autohide border property */
  public void setAutoHideBorder(DockingConstants.Hide border){
    this.autoHideBorder = border;
  }
  
  /** Returns try if autohiding is enabled */
  public boolean isAutoHideEnabled(){
    return isAutoHideEnabled;
  }
  
  /** Updates the autohideEnabled propety.
   * <p>
   * PropertyListeners are notified of that change
   * */
  public void setAutoHideEnabled(boolean enabled) {
    boolean old = this.isAutoHideEnabled;
    this.isAutoHideEnabled = enabled;
    propertySupport.firePropertyChange(PROPERTY_AUTOHIDEABLE, old, enabled);
  }
  
  /** Returns true if this dockable can be closed */
  public boolean isCloseEnabled(){
    return isCloseEnabled;
  }
  
  /** Updates the closeEnabled propety.
   * <P> PropertyListeners are notified of that change
   * */
  public void setCloseEnabled(boolean enabled) {
    boolean old = this.isCloseEnabled;
    this.isCloseEnabled = enabled;
    propertySupport.firePropertyChange(PROPERTY_CLOSEABLE, old, enabled);
  }
  
  /** Returns true if this dockable can be maximized */
  public boolean isMaximizeEnabled(){
    return isMaximizeEnabled;
  }
  
  /** Updates the maximizeEnabled propety.
   * <P> PropertyListeners are notified of that change
   * */
  public void setMaximizeEnabled(boolean enabled) {
    boolean old = this.isMaximizeEnabled;
    this.isMaximizeEnabled = enabled;
    propertySupport.firePropertyChange(PROPERTY_MAXIMIZABLE, old, enabled);
  }
  
  /** Returns true if this dockable can be detached from its desktop */
  public boolean isFloatEnabled(){
    return isFloatEnabled;
  }
  
  /** Updates the floatEnabled propety.
   * <P> PropertyListeners are notified of that change
   * */
  public void setFloatEnabled(boolean enabled) {
    boolean old = this.isFloatEnabled;
    this.isFloatEnabled = enabled;
    propertySupport.firePropertyChange(PROPERTY_FLOATABLE, old, enabled);
  }
  
  
  /** Returns true is a notification has been set.
   * <p> default notification is making title bars blink.
   * */
  public boolean isNotification(){
    return notification;
  }
  
  /** Updates the notification propety. Notification results in
   * a visual change of the dockable in order to attract attention from the
   * user to this dockable.
   *
   * <P> PropertyListeners are notified of that change.
   * */
  public void setNotification(boolean notification) {
    boolean old = this.notification;
    this.notification = notification;
    propertySupport.firePropertyChange(PROPERTY_NOTIFICATION, old, notification);
  }
  
  
  /** Overriden for Map storage needs */
  public int hashCode(){
    return dockKey.hashCode();
  }
  
  /** Overriden for Map storage needs */
  public boolean equals(Object o){
    return o instanceof DockKey && dockKey.equals(((DockKey)o).dockKey);
  }
  
  public String toString(){
    return "DockKey[" + name +']';
  }
  
  /** Returns the action customizer associated to this dockkey (may return null)
   *
   */
  public DockableActionCustomizer getActionCustomizer(){
    return actionCustomizer;
  }
  
  /** Updates the action customizer of this dockable
   *
   */
  public void setActionCustomizer(DockableActionCustomizer actionCustomizer){
    this.actionCustomizer = actionCustomizer;
  }
  
  /** Updates the dockGroup of this dockable.
   */
  public void setDockGroup(DockGroup group){
    this.dockGroup = group;
  }
  
  /** returns the dockGroup of this dockable  */
  public DockGroup getDockGroup(){
    return this.dockGroup;
  }
  
  /**
   * returns the current visible location of the dockable
   * (see DockableState.Location for enumeration values)
   * @see DockableState
   */
  public DockableState.Location getLocation(){
    return location;
  }
  
  /** updates the location property.
   * <p>
   * Warning : do not call this method, it is for the sole use of the DockingDesktop API.
   * @see DockableState
   */
  public void setLocation(DockableState.Location location){
    DockableState.Location old = this.location;
    this.location = location;
    propertySupport.firePropertyChange(PROPERTY_DOCKABLE_STATE, old, location);
  }
  
  public float getResizeWeight(){
    return this.resizeWeight;
  }
  
  /** updates the resize weight of this dockable. Valid values range between 0.0f and 1.0f */
  public void setResizeWeight(float weight){
    this.resizeWeight = weight;
  }

  /** Allows any property to be stored in a map associated with this dockkey. A property change event 
   * is propagated to listeners (with a property name equal to "clientProperty." + name)
   *
   *
   * @param name the name used to lookup the property
   * @param value the value of the property
   * @since 2.1.2
   */
  public void putProperty(String name, Object value) {  
    if (clientProperties == null){
      clientProperties = new HashMap();
    }
    clientProperties.put(name, value);
    propertySupport.firePropertyChange("clientProperty."+name, null, value);
  }
  
  /** returns a property associated to this name, or null if the property is undefined
   * @param name the name used to lookup the property
   * @since 2.1.2
   */
  public Object getProperty(String name) {
    if (clientProperties != null){
      return clientProperties.get(name);
    } else {
      return null;
    }
  }

  /** returns and removes a property associated to this name, or null if the property is undefined
   * @param name the name used to lookup the property
   * @since 2.1.2
   */
  public Object removeProperty(String name){
    if (clientProperties != null){
      return clientProperties.remove(name);
    } else {
      return null;
    }
  }
  
  
  
}
