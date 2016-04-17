

/* First created by JCasGen Thu Nov 19 12:08:03 EST 2015 */
package org.cleartk.discourse.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.cleartk.corpus.conll2015.RelationType;
import org.apache.uima.jcas.cas.FSArray;


/** 
 * Updated by JCasGen Fri Apr 15 12:41:45 EDT 2016
 * XML source: /Users/majid/Documents/git/CLaCDiscourseParser/discourse.parser.parent/discourse.conll.dataset/src/main/resources/org/cleartk/discourse/type/DiscourseArgument.xml
 * @generated */
public class DiscourseRelation extends TokenList {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DiscourseRelation.class);
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
  protected DiscourseRelation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public DiscourseRelation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DiscourseRelation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DiscourseRelation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  @Override
	public void addToIndexes(JCas jcas) {
		super.addToIndexes(jcas);
		if (getDiscourseConnective() != null)
			getDiscourseConnective().addToIndexes(jcas);
		for (int i = 0; i < getArguments().size(); i++){
			getArguments(i).addToIndexes();
		}
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
  //* Feature: arguments

  /** getter for arguments - gets 
   * @generated
   * @return value of the feature 
   */
  public FSArray getArguments() {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "org.cleartk.discourse.type.DiscourseRelation");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_arguments)));}
    
  /** setter for arguments - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setArguments(FSArray v) {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "org.cleartk.discourse.type.DiscourseRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_arguments, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for arguments - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public DiscourseArgument getArguments(int i) {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "org.cleartk.discourse.type.DiscourseRelation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_arguments), i);
    return (DiscourseArgument)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_arguments), i)));}

  /** indexed setter for arguments - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setArguments(int i, DiscourseArgument v) { 
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "org.cleartk.discourse.type.DiscourseRelation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_arguments), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_arguments), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: sense

  /** getter for sense - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSense() {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_sense == null)
      jcasType.jcas.throwFeatMissing("sense", "org.cleartk.discourse.type.DiscourseRelation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_sense);}
    
  /** setter for sense - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSense(String v) {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_sense == null)
      jcasType.jcas.throwFeatMissing("sense", "org.cleartk.discourse.type.DiscourseRelation");
    jcasType.ll_cas.ll_setStringValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_sense, v);}    
   
    
  //*--------------*
  //* Feature: relationType

  /** getter for relationType - gets 
   * @generated
   * @return value of the feature 
   */
  public String getRelationType() {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_relationType == null)
      jcasType.jcas.throwFeatMissing("relationType", "org.cleartk.discourse.type.DiscourseRelation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_relationType);}
    
  /** setter for relationType - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRelationType(String v) {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_relationType == null)
      jcasType.jcas.throwFeatMissing("relationType", "org.cleartk.discourse.type.DiscourseRelation");
    jcasType.ll_cas.ll_setStringValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_relationType, v);}    
   
    
  //*--------------*
  //* Feature: discourseConnectiveText

  /** getter for discourseConnectiveText - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDiscourseConnectiveText() {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_discourseConnectiveText == null)
      jcasType.jcas.throwFeatMissing("discourseConnectiveText", "org.cleartk.discourse.type.DiscourseRelation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_discourseConnectiveText);}
    
  /** setter for discourseConnectiveText - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDiscourseConnectiveText(String v) {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_discourseConnectiveText == null)
      jcasType.jcas.throwFeatMissing("discourseConnectiveText", "org.cleartk.discourse.type.DiscourseRelation");
    jcasType.ll_cas.ll_setStringValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_discourseConnectiveText, v);}    
   
    
  //*--------------*
  //* Feature: discourseConnective

  /** getter for discourseConnective - gets 
   * @generated
   * @return value of the feature 
   */
  public DiscourseConnective getDiscourseConnective() {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_discourseConnective == null)
      jcasType.jcas.throwFeatMissing("discourseConnective", "org.cleartk.discourse.type.DiscourseRelation");
    return (DiscourseConnective)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_discourseConnective)));}
    
  /** setter for discourseConnective - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDiscourseConnective(DiscourseConnective v) {
    if (DiscourseRelation_Type.featOkTst && ((DiscourseRelation_Type)jcasType).casFeat_discourseConnective == null)
      jcasType.jcas.throwFeatMissing("discourseConnective", "org.cleartk.discourse.type.DiscourseRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiscourseRelation_Type)jcasType).casFeatCode_discourseConnective, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    