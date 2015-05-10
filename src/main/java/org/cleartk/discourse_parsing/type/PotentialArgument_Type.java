
/* First created by JCasGen Wed Mar 25 18:27:17 EDT 2015 */
package org.cleartk.discourse_parsing.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Wed Mar 25 18:27:17 EDT 2015
 * @generated */
public class PotentialArgument_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (PotentialArgument_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = PotentialArgument_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new PotentialArgument(addr, PotentialArgument_Type.this);
  			   PotentialArgument_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new PotentialArgument(addr, PotentialArgument_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = PotentialArgument.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.discourse_parsing.type.PotentialArgument");
 
  /** @generated */
  final Feature casFeat_treebankNode;
  /** @generated */
  final int     casFeatCode_treebankNode;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTreebankNode(int addr) {
        if (featOkTst && casFeat_treebankNode == null)
      jcas.throwFeatMissing("treebankNode", "org.cleartk.discourse_parsing.type.PotentialArgument");
    return ll_cas.ll_getRefValue(addr, casFeatCode_treebankNode);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTreebankNode(int addr, int v) {
        if (featOkTst && casFeat_treebankNode == null)
      jcas.throwFeatMissing("treebankNode", "org.cleartk.discourse_parsing.type.PotentialArgument");
    ll_cas.ll_setRefValue(addr, casFeatCode_treebankNode, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public PotentialArgument_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_treebankNode = jcas.getRequiredFeatureDE(casType, "treebankNode", "org.cleartk.syntax.constituent.type.TreebankNode", featOkTst);
    casFeatCode_treebankNode  = (null == casFeat_treebankNode) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_treebankNode).getCode();

  }
}



    