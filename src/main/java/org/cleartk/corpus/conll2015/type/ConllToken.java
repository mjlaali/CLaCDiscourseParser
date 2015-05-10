

/* First created by JCasGen Mon Mar 09 17:46:03 EDT 2015 */
package org.cleartk.corpus.conll2015.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.cleartk.token.type.Token;


/** 
 * Updated by JCasGen Mon Mar 09 18:06:18 EDT 2015
 * XML source: /Users/majid/Documents/git-projects/conll2015-cleartk/conll2015/src/main/resources/org/cleartk/corpus/conll2015/type/ConllToken.xml
 * @generated */
public class ConllToken extends Token {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(ConllToken.class);
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
  protected ConllToken() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ConllToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ConllToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ConllToken(JCas jcas, int begin, int end) {
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
  //* Feature: documentOffset

  /** getter for documentOffset - gets 
   * @generated
   * @return value of the feature 
   */
  public int getDocumentOffset() {
    if (ConllToken_Type.featOkTst && ((ConllToken_Type)jcasType).casFeat_documentOffset == null)
      jcasType.jcas.throwFeatMissing("documentOffset", "org.cleartk.corpus.conll2015.type.ConllToken");
    return jcasType.ll_cas.ll_getIntValue(addr, ((ConllToken_Type)jcasType).casFeatCode_documentOffset);}
    
  /** setter for documentOffset - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentOffset(int v) {
    if (ConllToken_Type.featOkTst && ((ConllToken_Type)jcasType).casFeat_documentOffset == null)
      jcasType.jcas.throwFeatMissing("documentOffset", "org.cleartk.corpus.conll2015.type.ConllToken");
    jcasType.ll_cas.ll_setIntValue(addr, ((ConllToken_Type)jcasType).casFeatCode_documentOffset, v);}    
  }

    