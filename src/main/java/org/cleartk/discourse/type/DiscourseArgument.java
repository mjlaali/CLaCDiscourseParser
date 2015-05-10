

/* First created by JCasGen Tue Mar 10 10:16:59 EDT 2015 */
package org.cleartk.discourse.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.cleartk.srl.type.Argument;


/** 
 * Updated by JCasGen Sat Apr 11 09:33:24 EDT 2015
 * XML source: /Users/majid/Documents/git-projects/conll2015cleartk-code/conll2015/src/main/resources/org/cleartk/discourse/type/DiscourseConnective.xml
 * @generated */
public class DiscourseArgument extends TokenList {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DiscourseArgument.class);
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
  }

    