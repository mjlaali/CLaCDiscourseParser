
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
 * Updated by JCasGen Thu Nov 19 12:11:45 EST 2015
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

  }
}



    