
/* First created by JCasGen Thu Nov 19 12:08:03 EST 2015 */
package org.cleartk.discourse.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Thu Nov 19 12:08:03 EST 2015
 * @generated */
public class DiscourseArgument_Type extends TokenList_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DiscourseArgument_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DiscourseArgument_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DiscourseArgument(addr, DiscourseArgument_Type.this);
  			   DiscourseArgument_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DiscourseArgument(addr, DiscourseArgument_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DiscourseArgument.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.discourse.type.DiscourseArgument");
 
  /** @generated */
  final Feature casFeat_argumentType;
  /** @generated */
  final int     casFeatCode_argumentType;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getArgumentType(int addr) {
        if (featOkTst && casFeat_argumentType == null)
      jcas.throwFeatMissing("argumentType", "org.cleartk.discourse.type.DiscourseArgument");
    return ll_cas.ll_getStringValue(addr, casFeatCode_argumentType);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setArgumentType(int addr, String v) {
        if (featOkTst && casFeat_argumentType == null)
      jcas.throwFeatMissing("argumentType", "org.cleartk.discourse.type.DiscourseArgument");
    ll_cas.ll_setStringValue(addr, casFeatCode_argumentType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public DiscourseArgument_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_argumentType = jcas.getRequiredFeatureDE(casType, "argumentType", "uima.cas.String", featOkTst);
    casFeatCode_argumentType  = (null == casFeat_argumentType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_argumentType).getCode();

  }
}



    