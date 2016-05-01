

/* First created by JCasGen Thu Nov 19 12:08:03 EST 2015 */
package org.cleartk.discourse.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Sun May 01 13:05:41 EDT 2016
 * XML source: /Users/majid/Documents/git/CLaCDiscourseParser/discourse.parser.parent/discourse.conll.dataset/src/main/resources/org/cleartk/discourse/type/DiscourseRelation.xml
 * @generated */
public class DiscourseArgument extends TokenList {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DiscourseArgument.class);
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
  protected DiscourseArgument() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public DiscourseArgument(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DiscourseArgument(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DiscourseArgument(JCas jcas, int begin, int end) {
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
  //* Feature: argumentType

  /** getter for argumentType - gets Label's of the argumen, (i.e. arg1 or arg2)
   * @generated
   * @return value of the feature 
   */
  public String getArgumentType() {
    if (DiscourseArgument_Type.featOkTst && ((DiscourseArgument_Type)jcasType).casFeat_argumentType == null)
      jcasType.jcas.throwFeatMissing("argumentType", "org.cleartk.discourse.type.DiscourseArgument");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DiscourseArgument_Type)jcasType).casFeatCode_argumentType);}
    
  /** setter for argumentType - sets Label's of the argumen, (i.e. arg1 or arg2) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setArgumentType(String v) {
    if (DiscourseArgument_Type.featOkTst && ((DiscourseArgument_Type)jcasType).casFeat_argumentType == null)
      jcasType.jcas.throwFeatMissing("argumentType", "org.cleartk.discourse.type.DiscourseArgument");
    jcasType.ll_cas.ll_setStringValue(addr, ((DiscourseArgument_Type)jcasType).casFeatCode_argumentType, v);}    
   
    
  //*--------------*
  //* Feature: discouresRelation

  /** getter for discouresRelation - gets 
   * @generated
   * @return value of the feature 
   */
  public DiscourseRelation getDiscouresRelation() {
    if (DiscourseArgument_Type.featOkTst && ((DiscourseArgument_Type)jcasType).casFeat_discouresRelation == null)
      jcasType.jcas.throwFeatMissing("discouresRelation", "org.cleartk.discourse.type.DiscourseArgument");
    return (DiscourseRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiscourseArgument_Type)jcasType).casFeatCode_discouresRelation)));}
    
  /** setter for discouresRelation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDiscouresRelation(DiscourseRelation v) {
    if (DiscourseArgument_Type.featOkTst && ((DiscourseArgument_Type)jcasType).casFeat_discouresRelation == null)
      jcasType.jcas.throwFeatMissing("discouresRelation", "org.cleartk.discourse.type.DiscourseArgument");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiscourseArgument_Type)jcasType).casFeatCode_discouresRelation, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    