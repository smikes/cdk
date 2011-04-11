/* Copyright (C) 2003-2007  The Chemistry Development Kit (CDK) project
 * 
 * Contact: cdk-devel@lists.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.validate;

import org.openscience.cdk.interfaces.IChemObject;

/**
 * Error found during semantical validation of a {@link IChemObject}.
 *
 * @author   Egon Willighagen
 * @cdk.githash
 * @cdk.created  2003-03-28
 *
 * @cdk.keyword atom, chemical validation
 * @cdk.module  valid
 */ 
public class ValidationTest {

    /** {@link ValidationTestType} which describes the error type. */
    private ValidationTestType type;
    /** IChemObject which has the error. */
    private IChemObject object;

    /** Multiline String with details on the error. */
    private String details;

    /**
     * @deprecated
     */
    public ValidationTest(IChemObject object, String error) {
        this(object, error, "");
    }
    
    /**
     * @deprecated
     */
    public ValidationTest(IChemObject object, String error, String details) {
        this(new ValidationTestType(error) {}, object, details);
    }

    public ValidationTest(ValidationTestType type, IChemObject object) {
        this(type, object, "");
    }
    
    public ValidationTest(ValidationTestType type, IChemObject object, String details) {
        this.type = type;
        this.object = object;
        this.details = details;
    }

    public IChemObject getChemObject() {
        return this.object;
    }
    
    public String getError() {
        return this.type.getError();
    }
    
    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
