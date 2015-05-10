
/* First created by JCasGen Tue Mar 10 10:25:00 EDT 2015 */
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
import org.cleartk.score.type.ScoredAnnotation_Type;

/** 
 * Updated by JCasGen Sat Apr 11 09:33:24 EDT 2015
 * @generated */
public class DiscourseRelation_Type extends TokenList_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DiscourseRelation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DiscourseRelation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DiscourseRelation(addr, DiscourseRelation_Type.this);
  			   DiscourseRelation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DiscourseRelation(addr, DiscourseRelation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = DiscourseRelation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.cleartk.discourse.type.DiscourseRelation");
 
  /** @generated */
  final Feature casFeat_arguments;
  /** @generated */
  final int     casFeatCode_arguments;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getArguments(int addr) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "org.cleartk.discourse.type.DiscourseRelation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_arguments);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setArguments(int addr, int v) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "org.cleartk.discourse.type.DiscourseRelation");
    ll_cas.ll_setRefValue(addr, casFeatCode_arguments, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getArguments(int addr, int i) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "org.cleartk.discourse.type.DiscourseRelation");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setArguments(int addr, int i, int v) {
        if (featOkTst && casFeat_arguments == null)
      jcas.throwFeatMissing("arguments", "org.cleartk.discourse.type.DiscourseRelation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_arguments), i, v);
  }
 
 
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
      jcas.throwFeatMissing("sense", "org.cleartk.discourse.type.DiscourseRelation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_sense);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSense(int addr, String v) {
        if (featOkTst && casFeat_sense == null)
      jcas.throwFeatMissing("sense", "org.cleartk.discourse.type.DiscourseRelation");
    ll_cas.ll_setStringValue(addr, casFeatCode_sense, v);}
    
  
 
  /** @generated */
  final Feature casFeat_relationType;
  /** @generated */
  final int     casFeatCode_relationType;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getRelationType(int addr) {
        if (featOkTst && casFeat_relationType == null)
      jcas.throwFeatMissing("relationType", "org.cleartk.discourse.type.DiscourseRelation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_relationType);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRelationType(int addr, String v) {
        if (featOkTst && casFeat_relationType == null)
      jcas.throwFeatMissing("relationType", "org.cleartk.discourse.type.DiscourseRelation");
    ll_cas.ll_setStringValue(addr, casFeatCode_relationType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_discourseConnectiveText;
  /** @generated */
  final int     casFeatCode_discourseConnectiveText;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getDiscourseConnectiveText(int addr) {
        if (featOkTst && casFeat_discourseConnectiveText == null)
      jcas.throwFeatMissing("discourseConnectiveText", "org.cleartk.discourse.type.DiscourseRelation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_discourseConnectiveText);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDiscourseConnectiveText(int addr, String v) {
        if (featOkTst && casFeat_discourseConnectiveText == null)
      jcas.throwFeatMissing("discourseConnectiveText", "org.cleartk.discourse.type.DiscourseRelation");
    ll_cas.ll_setStringValue(addr, casFeatCode_discourseConnectiveText, v);}
    
  
 
  /** @generated */
  final Feature casFeat_discourseConnective;
  /** @generated */
  final int     casFeatCode_discourseConnective;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDiscourseConnective(int addr) {
        if (featOkTst && casFeat_discourseConnective == null)
      jcas.throwFeatMissing("discourseConnective", "org.cleartk.discourse.type.DiscourseRelation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_discourseConnective);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDiscourseConnective(int addr, int v) {
        if (featOkTst && casFeat_discourseConnective == null)
      jcas.throwFeatMissing("discourseConnective", "org.cleartk.discourse.type.DiscourseRelation");
    ll_cas.ll_setRefValue(addr, casFeatCode_discourseConnective, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public DiscourseRelation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_arguments = jcas.getRequiredFeatureDE(casType, "arguments", "uima.cas.FSArray", featOkTst);
    casFeatCode_arguments  = (null == casFeat_arguments) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_arguments).getCode();

 
    casFeat_sense = jcas.getRequiredFeatureDE(casType, "sense", "uima.cas.String", featOkTst);
    casFeatCode_sense  = (null == casFeat_sense) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sense).getCode();

 
    casFeat_relationType = jcas.getRequiredFeatureDE(casType, "relationType", "uima.cas.String", featOkTst);
    casFeatCode_relationType  = (null == casFeat_relationType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relationType).getCode();

 
    casFeat_discourseConnectiveText = jcas.getRequiredFeatureDE(casType, "discourseConnectiveText", "uima.cas.String", featOkTst);
    casFeatCode_discourseConnectiveText  = (null == casFeat_discourseConnectiveText) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_discourseConnectiveText).getCode();

 
    casFeat_discourseConnective = jcas.getRequiredFeatureDE(casType, "discourseConnective", "org.cleartk.discourse.type.DiscourseConnective", featOkTst);
    casFeatCode_discourseConnective  = (null == casFeat_discourseConnective) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_discourseConnective).getCode();

  }
}



    