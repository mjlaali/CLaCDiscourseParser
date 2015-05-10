

/* First created by JCasGen Sun Mar 08 22:09:08 EDT 2015 */
package org.cleartk.score.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** 
 * Updated by JCasGen Sat Apr 11 09:33:29 EDT 2015
 * XML source: /Users/majid/Documents/git-projects/conll2015cleartk-code/conll2015/src/main/resources/org/cleartk/discourse/type/TokenList.xml
 * @generated */
public class ScoredTOP extends TOP {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ScoredTOP.class);
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
  protected ScoredTOP() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ScoredTOP(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ScoredTOP(JCas jcas) {
    super(jcas);
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
  //* Feature: score

  /** getter for score - gets 
   * @generated
   * @return value of the feature 
   */
  public double getScore() {
    if (ScoredTOP_Type.featOkTst && ((ScoredTOP_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "org.cleartk.score.type.ScoredTOP");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((ScoredTOP_Type)jcasType).casFeatCode_score);}
    
  /** setter for score - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setScore(double v) {
    if (ScoredTOP_Type.featOkTst && ((ScoredTOP_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "org.cleartk.score.type.ScoredTOP");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((ScoredTOP_Type)jcasType).casFeatCode_score, v);}    
  }

    