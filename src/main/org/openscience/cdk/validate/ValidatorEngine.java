/* Copyright (C) 2003-2008,2011  Egon Willighagen <egonw@users.sf.net>
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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.openscience.cdk.annotations.TestClass;
import org.openscience.cdk.annotations.TestMethod;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemSequence;
import org.openscience.cdk.interfaces.ICrystal;
import org.openscience.cdk.interfaces.IElectronContainer;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

/**
 * Engine that performs the validation by traversing the {@link IChemObject}
 * hierarchy. Basic use of the ValidatorEngine is:
 * <pre>
 * ValidatorEngine engine = new ValidatorEngine();
 * engine.addValidator(new BasicValidator());
 * ValidationReport report = engine.validateMolecule(new Molecule());
 * </pre>
 *
 * @author   Egon Willighagen <egonw@sci.kun.nl>
 * @cdk.githash
 * @cdk.created  2003-08-22
 * @cdk.module   valid
 */
@TestClass("org.openscience.cdk.validate.ValidatorEngineTest")
public class ValidatorEngine implements IValidator {
    
    private Map<String,IValidator> validators;
    private static ILoggingTool logger =
        LoggingToolFactory.createLoggingTool(ValidatorEngine.class);
    
    @TestMethod("testConstructor")
    public ValidatorEngine() {
        validators = new Hashtable<String,IValidator>();
    }

    @TestMethod("testAddValidator")
    public void addValidator(IValidator validator) {
        logger.info("Registering validator: " + validator.getClass().getName());
        String validatorName = validator.getClass().getName();
        if (validators.containsKey(validatorName)) {
            logger.warn("  already registered.");
        } else {
            validators.put(validatorName, validator);
        }
    }
    
    @TestMethod("testRemoveValidator")
    public void removeValidator(IValidator validator) {
        logger.info("Removing validator: " + validator.getClass().getName());
        String validatorName = validator.getClass().getName();
        if (!validators.containsKey(validatorName)) {
            logger.warn("  not in list.");
        } else {
            validators.remove(validatorName);
        }
    }

    @TestMethod("testValidateAtom")
    public ValidationReport validateAtom(IAtom subject) {
        logger.info("Validating org.openscience.cdk.Atom");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateAtom(subject));
        }
        // traverse into super class
        report.addReport(validateAtomType(subject));
        // traverse into hierarchy
        return report;
    }

    @TestMethod("testValidateAtomContainer")
    public ValidationReport validateAtomContainer(IAtomContainer subject) {
        logger.info("Validating org.openscience.cdk.AtomContainer");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateAtomContainer(subject));
        }
        // traverse into super class
        report.addReport(validateChemObject(subject));
        // traverse into hierarchy
        Iterator<IAtom> atoms = subject.atoms().iterator();
        while (atoms.hasNext()) {
            report.addReport(validateAtom((IAtom)atoms.next()));
        }

        Iterator<IBond> bonds = subject.bonds().iterator();
        while (bonds.hasNext()) {
            IBond bond = (IBond) bonds.next();
            report.addReport(validateBond(bond));
        }
        return report;
    }

    @TestMethod("testValidateAtomType")
    public ValidationReport validateAtomType(IAtomType subject) {
        logger.info("Validating org.openscience.cdk.AtomType");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateAtomType(subject));
        }
        // traverse into super class
        report.addReport(validateIsotope(subject));
        // traverse into hierarchy
        return report;
    }

    @TestMethod("testValidateBond")
    public ValidationReport validateBond(IBond subject) {
        logger.info("Validating org.openscience.cdk.Bond");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateBond(subject));
        }
        // traverse into super class
        report.addReport(validateElectronContainer(subject));
        // traverse into hierarchy
        Iterator<IAtom> atoms = subject.atoms().iterator();
        while (atoms.hasNext()) {
            report.addReport(validateAtom((IAtom)atoms.next()));
        }
        return report;
    }

    @TestMethod("testValidateChemFile")
    public ValidationReport validateChemFile(IChemFile subject) {
        logger.info("Validating org.openscience.cdk.ChemFile");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateChemFile(subject));
        }
        // traverse into super class
        report.addReport(validateChemObject(subject));
        // traverse into hierarchy
        for (int i=0; i< subject.getChemSequenceCount(); i++) {
            report.addReport(validateChemSequence(subject.getChemSequence(i)));
        }
        return report;
    }

    @TestMethod("testValidateChemModel")
    public ValidationReport validateChemModel(IChemModel subject) {
        logger.info("Validating org.openscience.cdk.ChemModel");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateChemModel(subject));
        }
        // traverse into super class
        report.addReport(validateChemObject(subject));
        // traverse into hierarchy
        ICrystal crystal = subject.getCrystal();
        if (crystal != null) {
            report.addReport(validateCrystal(crystal));
        }
        IReactionSet reactionSet = subject.getReactionSet();
        if (reactionSet != null) {
            report.addReport(validateReactionSet(reactionSet));
        }
        IMoleculeSet moleculeSet = subject.getMoleculeSet();
        if (moleculeSet != null) {
            report.addReport(validateMoleculeSet(moleculeSet));
        }
        return report;
    }

    @TestMethod("testValidateChemObject")
    public ValidationReport validateChemObject(IChemObject subject) {
        logger.info("Validating org.openscience.cdk.ChemObject");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateChemObject(subject));
        }
        // traverse into super class
        // traverse into hierarchy
        return report;
    }

    @TestMethod("testValidateChemSequence")
    public ValidationReport validateChemSequence(IChemSequence subject) {
        logger.info("Validating org.openscience.cdk.ChemSequence");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateChemSequence(subject));
        }
        // traverse into super class
        report.addReport(validateChemObject(subject));
        // traverse into hierarchy
        Iterator<IChemModel> models = subject.chemModels().iterator();
        while (models.hasNext()) {
            report.addReport(validateChemModel(models.next()));
        }
        return report;
    }

    @TestMethod("testValidateCrystal")
    public ValidationReport validateCrystal(ICrystal subject) {
        logger.info("Validating org.openscience.cdk.Crystal");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateCrystal(subject));
        }
        // traverse into super class
        report.addReport(validateAtomContainer(subject));
        // traverse into hierarchy
        return report;
    }

    @TestMethod("testValidateElectronContainer")
    public ValidationReport validateElectronContainer(IElectronContainer subject) {
        logger.info("Validating org.openscience.cdk.ElectronContainer");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateElectronContainer(subject));
        }
        // traverse into super class
        report.addReport(validateChemObject(subject));
        // traverse into hierarchy
        return report;
    }

    @TestMethod("testValidateElement")
    public ValidationReport validateElement(IElement subject) {
        logger.info("Validating org.openscience.cdk.Element");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateElement(subject));
        }
        // traverse into super class
        report.addReport(validateChemObject(subject));
        // traverse into hierarchy
        return report;
    }

    @TestMethod("testValidateIsotope")
    public ValidationReport validateIsotope(IIsotope subject) {
        logger.info("Validating org.openscience.cdk.Isotope");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateIsotope(subject));
        }
        // traverse into super class
        report.addReport(validateElement(subject));
        // traverse into hierarchy
        return report;
    }

    @TestMethod("testValidateMolecule")
    public ValidationReport validateMolecule(IMolecule subject) {
        logger.info("Validating org.openscience.cdk.Molecule");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateMolecule(subject));
        }
        // traverse into super class
        report.addReport(validateAtomContainer(subject));
        // traverse into hierarchy
        return report;
    }

    @TestMethod("testValidateReaction")
    public ValidationReport validateReaction(IReaction subject) {
        logger.info("Validating org.openscience.cdk.Reaction");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateReaction(subject));
        }
        // traverse into super class
        report.addReport(validateChemObject(subject));
        // traverse into hierarchy
        IMoleculeSet reactants = subject.getReactants();
        for (int i=0; i<reactants.getAtomContainerCount(); i++) {
            report.addReport(validateMolecule(reactants.getMolecule(i)));
        }
        IMoleculeSet products = subject.getProducts();
        for (int i=0; i<products.getAtomContainerCount(); i++) {
            report.addReport(validateMolecule(products.getMolecule(i)));
        }
        return report;
    }

    @TestMethod("testValidateMoleculeSet")
    public ValidationReport validateMoleculeSet(IMoleculeSet subject) {
        logger.info("Validating org.openscience.cdk.MoleculeSet");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateMoleculeSet(subject));
        }
        // traverse into super class
        report.addReport(validateChemObject(subject));
        // traverse into hierarchy
        for (int i=0; i<subject.getAtomContainerCount(); i++) {
            report.addReport(validateMolecule(subject.getMolecule(i)));
        }
        return report;
    }

    @TestMethod("testValidateReactionSet")
    public ValidationReport validateReactionSet(IReactionSet subject) {
        logger.info("Validating org.openscience.cdk.ReactionSet");
        ValidationReport report = new ValidationReport();
        // apply validators
        for (IValidator test : validators.values()) {
            report.addReport(test.validateReactionSet(subject));
        }
        // traverse into super class
        report.addReport(validateChemObject(subject));
        // traverse into hierarchy
        for (Iterator<IReaction> iter = subject.reactions().iterator(); iter.hasNext();) {
            report.addReport(validateReaction((IReaction)iter.next()));
        }
        return report;
    }
    
}
