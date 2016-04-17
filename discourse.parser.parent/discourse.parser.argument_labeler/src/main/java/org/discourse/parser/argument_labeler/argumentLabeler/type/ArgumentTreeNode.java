

/* First created by JCasGen Fri Apr 15 12:45:54 EDT 2016 */
package org.discourse.parser.argument_labeler.argumentLabeler.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.discourse.type.DiscourseArgument;


/** 
 * Updated by JCasGen Fri Apr 15 12:50:10 EDT 2016
 * XML source: /Users/majid/Documents/git/CLaCDiscourseParser/discourse.parser.parent/discourse.parser.argument_labeler/src/main/resources/org/discourse/parser/argument_labeler/argumentLabeler/type/ArgumentTreeNode.xml
 * @generated */
public class ArgumentTreeNode extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(ArgumentTreeNode.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ArgumentTreeNode() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ArgumentTreeNode(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ArgumentTreeNode(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ArgumentTreeNode(JCas jcas, int begin, int end) {
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
  //* Feature: treeNode

  /** getter for treeNode - gets 
   * @generated
   * @return value of the feature 
   */
  public Annotation getTreeNode() {
    if (ArgumentTreeNode_Type.featOkTst && ((ArgumentTreeNode_Type)jcasType).casFeat_treeNode == null)
      jcasType.jcas.throwFeatMissing("treeNode", "org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((ArgumentTreeNode_Type)jcasType).casFeatCode_treeNode)));}
    
  /** setter for treeNode - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTreeNode(Annotation v) {
    if (ArgumentTreeNode_Type.featOkTst && ((ArgumentTreeNode_Type)jcasType).casFeat_treeNode == null)
      jcasType.jcas.throwFeatMissing("treeNode", "org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode");
    jcasType.ll_cas.ll_setRefValue(addr, ((ArgumentTreeNode_Type)jcasType).casFeatCode_treeNode, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: discourseArgument

  /** getter for discourseArgument - gets 
   * @generated
   * @return value of the feature 
   */
  public DiscourseArgument getDiscourseArgument() {
    if (ArgumentTreeNode_Type.featOkTst && ((ArgumentTreeNode_Type)jcasType).casFeat_discourseArgument == null)
      jcasType.jcas.throwFeatMissing("discourseArgument", "org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode");
    return (DiscourseArgument)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((ArgumentTreeNode_Type)jcasType).casFeatCode_discourseArgument)));}
    
  /** setter for discourseArgument - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDiscourseArgument(DiscourseArgument v) {
    if (ArgumentTreeNode_Type.featOkTst && ((ArgumentTreeNode_Type)jcasType).casFeat_discourseArgument == null)
      jcasType.jcas.throwFeatMissing("discourseArgument", "org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode");
    jcasType.ll_cas.ll_setRefValue(addr, ((ArgumentTreeNode_Type)jcasType).casFeatCode_discourseArgument, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    