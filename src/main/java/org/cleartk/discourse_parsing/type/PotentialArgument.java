

/* First created by JCasGen Wed Mar 25 18:27:17 EDT 2015 */
package org.cleartk.discourse_parsing.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.cleartk.syntax.constituent.type.TreebankNode;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Mar 25 18:27:17 EDT 2015
 * XML source: /Users/majid/Documents/git-projects/conll2015cleartk-code/conll2015/src/main/resources/org/cleartk/discourse_parsing/type/PotentialArgument.xml
 * @generated */
public class PotentialArgument extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(PotentialArgument.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected PotentialArgument() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public PotentialArgument(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public PotentialArgument(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public PotentialArgument(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: treebankNode

  /** getter for treebankNode - gets 
   * @generated
   * @return value of the feature 
   */
  public TreebankNode getTreebankNode() {
    if (PotentialArgument_Type.featOkTst && ((PotentialArgument_Type)jcasType).casFeat_treebankNode == null)
      jcasType.jcas.throwFeatMissing("treebankNode", "org.cleartk.discourse_parsing.type.PotentialArgument");
    return (TreebankNode)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((PotentialArgument_Type)jcasType).casFeatCode_treebankNode)));}
    
  /** setter for treebankNode - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTreebankNode(TreebankNode v) {
    if (PotentialArgument_Type.featOkTst && ((PotentialArgument_Type)jcasType).casFeat_treebankNode == null)
      jcasType.jcas.throwFeatMissing("treebankNode", "org.cleartk.discourse_parsing.type.PotentialArgument");
    jcasType.ll_cas.ll_setRefValue(addr, ((PotentialArgument_Type)jcasType).casFeatCode_treebankNode, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    