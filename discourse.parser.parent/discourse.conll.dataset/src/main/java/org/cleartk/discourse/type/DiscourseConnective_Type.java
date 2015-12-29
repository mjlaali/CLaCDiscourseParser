
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
public class DiscourseConnective_Type extends TokenList_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DiscourseConnective_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DiscourseConnective_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DiscourseConnective(addr, DiscourseConnective_Type.this);
  			   DiscourseConnective_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DiscourseConnective(addr, DiscourseConnective_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DiscourseConnective.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.discourse.type.DiscourseConnective");
 
  /** @generated */
  final Feature casFeat_discourseRelation;
  /** @generated */
  final int     casFeatCode_discourseRelation;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDiscourseRelation(int addr) {
        if (featOkTst && casFeat_discourseRelation == null)
      jcas.throwFeatMissing("discourseRelation", "org.cleartk.discourse.type.DiscourseConnective");
    return ll_cas.ll_getRefValue(addr, casFeatCode_discourseRelation);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDiscourseRelation(int addr, int v) {
        if (featOkTst && casFeat_discourseRelation == null)
      jcas.throwFeatMissing("discourseRelation", "org.cleartk.discourse.type.DiscourseConnective");
    ll_cas.ll_setRefValue(addr, casFeatCode_discourseRelation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_sense;
  /** @generated */
  final int     casFeatCode_sense;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSense(int addr) {
        if (featOkTst && casFeat_sense == null)
      jcas.throwFeatMissing("sense", "org.cleartk.discourse.type.DiscourseConnective");
    return ll_cas.ll_getStringValue(addr, casFeatCode_sense);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSense(int addr, String v) {
        if (featOkTst && casFeat_sense == null)
      jcas.throwFeatMissing("sense", "org.cleartk.discourse.type.DiscourseConnective");
    ll_cas.ll_setStringValue(addr, casFeatCode_sense, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public DiscourseConnective_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_discourseRelation = jcas.getRequiredFeatureDE(casType, "discourseRelation", "org.cleartk.discourse.type.DiscourseRelation", featOkTst);
    casFeatCode_discourseRelation  = (null == casFeat_discourseRelation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_discourseRelation).getCode();

 
    casFeat_sense = jcas.getRequiredFeatureDE(casType, "sense", "uima.cas.String", featOkTst);
    casFeatCode_sense  = (null == casFeat_sense) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sense).getCode();

  }
}



    