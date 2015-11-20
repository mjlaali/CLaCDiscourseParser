
/* First created by JCasGen Tue Mar 24 16:40:12 EDT 2015 */
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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence_Type;

/** 
 * Updated by JCasGen Thu Nov 19 12:11:45 EST 2015
 * @generated */
public class SentenceWithSyntax_Type extends Sentence_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SentenceWithSyntax_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SentenceWithSyntax_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SentenceWithSyntax(addr, SentenceWithSyntax_Type.this);
  			   SentenceWithSyntax_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SentenceWithSyntax(addr, SentenceWithSyntax_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SentenceWithSyntax.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.corpus.conll2015.type.SentenceWithSyntax");
 
  /** @generated */
  final Feature casFeat_syntaxTree;
  /** @generated */
  final int     casFeatCode_syntaxTree;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSyntaxTree(int addr) {
        if (featOkTst && casFeat_syntaxTree == null)
      jcas.throwFeatMissing("syntaxTree", "org.cleartk.corpus.conll2015.type.SentenceWithSyntax");
    return ll_cas.ll_getStringValue(addr, casFeatCode_syntaxTree);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSyntaxTree(int addr, String v) {
        if (featOkTst && casFeat_syntaxTree == null)
      jcas.throwFeatMissing("syntaxTree", "org.cleartk.corpus.conll2015.type.SentenceWithSyntax");
    ll_cas.ll_setStringValue(addr, casFeatCode_syntaxTree, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public SentenceWithSyntax_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_syntaxTree = jcas.getRequiredFeatureDE(casType, "syntaxTree", "uima.cas.String", featOkTst);
    casFeatCode_syntaxTree  = (null == casFeat_syntaxTree) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_syntaxTree).getCode();

  }
}



    