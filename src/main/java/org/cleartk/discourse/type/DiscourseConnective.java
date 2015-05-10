

/* First created by JCasGen Tue Mar 10 10:16:59 EDT 2015 */
package org.cleartk.discourse.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.cleartk.score.type.ScoredAnnotation;


/** 
 * Updated by JCasGen Sat Apr 11 09:33:24 EDT 2015
 * XML source: /Users/majid/Documents/git-projects/conll2015cleartk-code/conll2015/src/main/resources/org/cleartk/discourse/type/DiscourseConnective.xml
 * @generated */
public class DiscourseConnective extends TokenList {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DiscourseConnective.class);
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
  protected DiscourseConnective() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public DiscourseConnective(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DiscourseConnective(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DiscourseConnective(JCas jcas, int begin, int end) {
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
  //* Feature: discourseRelation

  /** getter for discourseRelation - gets 
   * @generated
   * @return value of the feature 
   */
  public DiscourseRelation getDiscourseRelation() {
    if (DiscourseConnective_Type.featOkTst && ((DiscourseConnective_Type)jcasType).casFeat_discourseRelation == null)
      jcasType.jcas.throwFeatMissing("discourseRelation", "org.cleartk.discourse.type.DiscourseConnective");
    return (DiscourseRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiscourseConnective_Type)jcasType).casFeatCode_discourseRelation)));}
    
  /** setter for discourseRelation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDiscourseRelation(DiscourseRelation v) {
    if (DiscourseConnective_Type.featOkTst && ((DiscourseConnective_Type)jcasType).casFeat_discourseRelation == null)
      jcasType.jcas.throwFeatMissing("discourseRelation", "org.cleartk.discourse.type.DiscourseConnective");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiscourseConnective_Type)jcasType).casFeatCode_discourseRelation, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    