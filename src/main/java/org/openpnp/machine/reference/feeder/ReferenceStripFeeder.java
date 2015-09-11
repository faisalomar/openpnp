/*
 	Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 	
 	This file is part of OpenPnP.
 	
	OpenPnP is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenPnP is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenPnP.  If not, see <http://www.gnu.org/licenses/>.
 	
 	For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.reference.feeder;



import javax.swing.Action;
import javax.vecmath.Vector2d;

import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.ReferenceFeeder;
import org.openpnp.machine.reference.feeder.wizards.ReferenceStripFeederConfigurationWizard;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.PropertySheetHolder;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of Feeder that indexes through a strip of cut tape.
 * This is a specialization of the tray feeder that knows specifics
 * about tape so that vision capabilities can be added.
 */

/**
 * SMD tape standard info from http://www.liteplacer.com/setup-tape-positions-2/
 * holes 1.5mm
 * hole pitch 4mm
 * reference hole to part is 2mm
 * tape width is multiple of 4mm
 * part pitch is multiple of 4mm except for 0402 and smaller, where it is 2mm 
 * hole to part is tape width / 2 - 0.5mm 
 */
public class ReferenceStripFeeder extends ReferenceFeeder {
	private final static Logger logger = LoggerFactory.getLogger(ReferenceStripFeeder.class);
	
	@Element(required=false)
    private Location referenceHoleLocation = new Location(LengthUnit.Millimeters);

    @Element(required=false)
    private Location lastHoleLocation = new Location(LengthUnit.Millimeters);

    @Element(required=false)
    private Length partPitch = new Length(4, LengthUnit.Millimeters);
    
    @Element(required=false)
    private Length tapeWidth = new Length(8, LengthUnit.Millimeters);

    private Length holeDiameter = new Length(1.5, LengthUnit.Millimeters);
    
    private Length holePitch = new Length(4, LengthUnit.Millimeters);
    
    private Length referenceHoleToPartLinear = new Length(2, LengthUnit.Millimeters);
    
    @Attribute
	private int feedCount = 0;
    
    private Length getHoleToPartLateral() {
        Length tapeWidth = this.tapeWidth.convertToUnits(LengthUnit.Millimeters);
        return new Length(tapeWidth.getValue() / 2 - 0.5, LengthUnit.Millimeters);
    }
	
	@Override
	public boolean canFeedToNozzle(Nozzle nozzle) {
//		boolean result = feedCount < partCount;
//		logger.debug("{}.canFeedToNozzle({}) => {}", new Object[]{getName(), nozzle, result});
//		return result;
	    return true;
	}
	
    static public Location linearInterpolation(Location a, Location b, Length distance) {
//        b = b.convertToUnits(a.getUnits());
//        distance = distance.convertToUnits(a.getUnits());
//        Location vAb = b.subtract(a);
//        double vAbLen = b.getLinearDistanceTo(a);
//        Location uVab = vAb.derive(vAb.getX() / vAbLen, vAb.getY() / vAbLen, null, null);
//        Location vT = uVab.multiply(distance.getValue(), distance.getValue(), 1, 1);
//        return vT;
        
          Vector2d vab = new Vector2d(b.getX() - a.getX(), b.getY() - a.getY());
          double lab = vab.length();
          Vector2d vu = new Vector2d(vab.x / lab, vab.y / lab);
          vu.scale(distance.getValue());
          return a.add(new Location(a.getUnits(), vu.x, vu.y, 0, 0));
    }
    
	@Override
    public Location getPickLocation() throws Exception {
	    return linearInterpolation(
	            referenceHoleLocation, 
	            lastHoleLocation, 
	            new Length(feedCount * partPitch.getValue(), partPitch.getUnits()));
    }

    public void feed(Nozzle nozzle)
			throws Exception {
        feedCount++;
	}
    
	public Location getReferenceHoleLocation() {
        return referenceHoleLocation;
    }

    public void setReferenceHoleLocation(Location referenceHoleLocation) {
        this.referenceHoleLocation = referenceHoleLocation;
    }

    public Location getLastHoleLocation() {
        return lastHoleLocation;
    }

    public void setLastHoleLocation(Location lastHoleLocation) {
        this.lastHoleLocation = lastHoleLocation;
    }

    public Length getHoleDiameter() {
        return holeDiameter;
    }

    public void setHoleDiameter(Length holeDiameter) {
        this.holeDiameter = holeDiameter;
    }

    public Length getHolePitch() {
        return holePitch;
    }

    public void setHolePitch(Length holePitch) {
        this.holePitch = holePitch;
    }

    public Length getPartPitch() {
        return partPitch;
    }

    public void setPartPitch(Length partPitch) {
        this.partPitch = partPitch;
    }

    public Length getTapeWidth() {
        return tapeWidth;
    }

    public void setTapeWidth(Length tapeWidth) {
        this.tapeWidth = tapeWidth;
    }

    public int getFeedCount() {
        return feedCount;
    }

    public void setFeedCount(int feedCount) {
        this.feedCount = feedCount;
    }
    
    public Length getReferenceHoleToPartLinear() {
        return referenceHoleToPartLinear;
    }

    public void setReferenceHoleToPartLinear(Length referenceHoleToPartLinear) {
        this.referenceHoleToPartLinear = referenceHoleToPartLinear;
    }

    @Override
	public String toString() {
		return getName();
	}

    @Override
    public Wizard getConfigurationWizard() {
        return new ReferenceStripFeederConfigurationWizard(this);
    }
    
    @Override
    public String getPropertySheetHolderTitle() {
        return getClass().getSimpleName() + " " + getName();
    }

    @Override
    public PropertySheetHolder[] getChildPropertySheetHolders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PropertySheet[] getPropertySheets() {
        return new PropertySheet[] {
                new PropertySheetWizardAdapter(getConfigurationWizard())
        };
    }

    @Override
    public Action[] getPropertySheetHolderActions() {
        // TODO Auto-generated method stub
        return null;
    }  
}
