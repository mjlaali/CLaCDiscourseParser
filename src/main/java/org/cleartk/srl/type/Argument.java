

/* First created by JCasGen Tue Mar 10 10:18:24 EDT 2015 */
package org.cleartk.srl.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.score.type.ScoredAnnotation;


/** 
 * Updated by JCasGen Tue Mar 10 17:15:17 EDT 2015
 * XML source: /Users/majid/Documents/git-projects/conll2015-cleartk/conll2015/src/main/resources/org/cleartk/discourse/type/DiscourseRelation.xml
 * @generated */
public class Argument extends ScoredAnnotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Argument.class);
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
  protected Argument() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Argument(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Argument(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Argument(JCas jcas, int begin, int end) {
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
  //* Feature: annotation

  /** getter for annotation - gets 
   * @generated
   * @return value of the feature 
   */
  public Annotation getAnnotation() {
    if (Argument_Type.featOkTst && ((Argument_Type)jcasType).casFeat_annotation == null)
      jcasType.jcas.throwFeatMissing("annotation", "org.cleartk.srl.type.Argument");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Argument_Type)jcasType).casFeatCode_annotation)));}
    
  /** setter for annotation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setAnnotation(Annotation v) {
    if (Argument_Type.featOkTst && ((Argument_Type)jcasType).casFeat_annotation == null)
      jcasType.jcas.throwFeatMissing("annotation", "org.cleartk.srl.type.Argument");
    jcasType.ll_cas.ll_setRefValue(addr, ((Argument_Type)jcasType).casFeatCode_annotation, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    