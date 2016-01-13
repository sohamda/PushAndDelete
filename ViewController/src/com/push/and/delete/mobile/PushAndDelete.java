package com.push.and.delete.mobile;

import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

public class PushAndDelete implements Cloneable {
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public PushAndDelete() {
        super();
    }
    
    public PushAndDelete(Integer padId, String firstName, String lastName) {
        this.padId = padId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    private Integer padId;
    private String firstName;
    private String lastName;

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    public void setPadId(Integer padId) {
        Integer oldPadId = this.padId;
        this.padId = padId;
        propertyChangeSupport.firePropertyChange("padId", oldPadId, padId);
    }

    public Integer getPadId() {
        return padId;
    }

    public void setFirstName(String firstName) {
        String oldFirstName = this.firstName;
        this.firstName = firstName;
        propertyChangeSupport.firePropertyChange("firstName", oldFirstName, firstName);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        String oldLastName = this.lastName;
        this.lastName = lastName;
        propertyChangeSupport.firePropertyChange("lastName", oldLastName, lastName);
    }

    public String getLastName() {
        return lastName;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
}
