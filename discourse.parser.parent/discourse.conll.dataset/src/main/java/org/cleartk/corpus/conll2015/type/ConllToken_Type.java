
/* First created by JCasGen Mon Mar 09 17:46:03 EDT 2015 */
package org.cleartk.corpus.conll2015.type;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token_Type;

/** 
 * Updated by JCasGen Thu Apr 21 15:12:04 EDT 2016
 * @generated */
public class ConllToken_Type extends Token_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ConllToken_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ConllToken_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ConllToken(addr, ConllToken_Type.this);
  			   ConllToken_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ConllToken(addr, ConllToken_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = ConllToken.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.corpus.conll2015.type.ConllToken");



  /** @generated */
  final Feature casFeat_documentOffset;
  /** @generated */
  final int     casFeatCode_documentOffset;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDocumentOffset(int addr) {
        if (featOkTst && casFeat_documentOffset == null)
      jcas.throwFeatMissing("documentOffset", "org.cleartk.corpus.conll2015.type.ConllToken");
    return ll_cas.ll_getIntValue(addr, casFeatCode_documentOffset);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDocumentOffset(int addr, int v) {
        if (featOkTst && casFeat_documentOffset == null)
      jcas.throwFeatMissing("documentOffset", "org.cleartk.corpus.conll2015.type.ConllToken");
    ll_cas.ll_setIntValue(addr, casFeatCode_documentOffset, v);}
    
  
 
  /** @generated */
  final Feature casFeat_sentenceOffset;
  /** @generated */
  final int     casFeatCode_sentenceOffset;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSentenceOffset(int addr) {
        if (featOkTst && casFeat_sentenceOffset == null)
      jcas.throwFeatMissing("sentenceOffset", "org.cleartk.corpus.conll2015.type.ConllToken");
    return ll_cas.ll_getIntValue(addr, casFeatCode_sentenceOffset);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSentenceOffset(int addr, int v) {
        if (featOkTst && casFeat_sentenceOffset == null)
      jcas.throwFeatMissing("sentenceOffset", "org.cleartk.corpus.conll2015.type.ConllToken");
    ll_cas.ll_setIntValue(addr, casFeatCode_sentenceOffset, v);}
    
  
 
  /** @generated */
  final Feature casFeat_offsetInSentence;
  /** @generated */
  final int     casFeatCode_offsetInSentence;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getOffsetInSentence(int addr) {
        if (featOkTst && casFeat_offsetInSentence == null)
      jcas.throwFeatMissing("offsetInSentence", "org.cleartk.corpus.conll2015.type.ConllToken");
    return ll_cas.ll_getIntValue(addr, casFeatCode_offsetInSentence);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setOffsetInSentence(int addr, int v) {
        if (featOkTst && casFeat_offsetInSentence == null)
      jcas.throwFeatMissing("offsetInSentence", "org.cleartk.corpus.conll2015.type.ConllToken");
    ll_cas.ll_setIntValue(addr, casFeatCode_offsetInSentence, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ConllToken_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_documentOffset = jcas.getRequiredFeatureDE(casType, "documentOffset", "uima.cas.Integer", featOkTst);
    casFeatCode_documentOffset  = (null == casFeat_documentOffset) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_documentOffset).getCode();

 
    casFeat_sentenceOffset = jcas.getRequiredFeatureDE(casType, "sentenceOffset", "uima.cas.Integer", featOkTst);
    casFeatCode_sentenceOffset  = (null == casFeat_sentenceOffset) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sentenceOffset).getCode();

 
    casFeat_offsetInSentence = jcas.getRequiredFeatureDE(casType, "offsetInSentence", "uima.cas.Integer", featOkTst);
    casFeatCode_offsetInSentence  = (null == casFeat_offsetInSentence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_offsetInSentence).getCode();

  }
}



    