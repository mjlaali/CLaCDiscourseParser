
/* First created by JCasGen Fri Apr 15 12:45:54 EDT 2016 */
package org.discourse.parser.argument_labeler.argumentLabeler.type;

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
 * Updated by JCasGen Fri Apr 15 12:50:10 EDT 2016
 * @generated */
public class ArgumentTreeNode_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ArgumentTreeNode_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ArgumentTreeNode_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ArgumentTreeNode(addr, ArgumentTreeNode_Type.this);
  			   ArgumentTreeNode_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ArgumentTreeNode(addr, ArgumentTreeNode_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = ArgumentTreeNode.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode");
 
  /** @generated */
  final Feature casFeat_treeNode;
  /** @generated */
  final int     casFeatCode_treeNode;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTreeNode(int addr) {
        if (featOkTst && casFeat_treeNode == null)
      jcas.throwFeatMissing("treeNode", "org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode");
    return ll_cas.ll_getRefValue(addr, casFeatCode_treeNode);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTreeNode(int addr, int v) {
        if (featOkTst && casFeat_treeNode == null)
      jcas.throwFeatMissing("treeNode", "org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode");
    ll_cas.ll_setRefValue(addr, casFeatCode_treeNode, v);}
    
  
 
  /** @generated */
  final Feature casFeat_discourseArgument;
  /** @generated */
  final int     casFeatCode_discourseArgument;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDiscourseArgument(int addr) {
        if (featOkTst && casFeat_discourseArgument == null)
      jcas.throwFeatMissing("discourseArgument", "org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode");
    return ll_cas.ll_getRefValue(addr, casFeatCode_discourseArgument);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDiscourseArgument(int addr, int v) {
        if (featOkTst && casFeat_discourseArgument == null)
      jcas.throwFeatMissing("discourseArgument", "org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode");
    ll_cas.ll_setRefValue(addr, casFeatCode_discourseArgument, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ArgumentTreeNode_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_treeNode = jcas.getRequiredFeatureDE(casType, "treeNode", "uima.tcas.Annotation", featOkTst);
    casFeatCode_treeNode  = (null == casFeat_treeNode) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_treeNode).getCode();

 
    casFeat_discourseArgument = jcas.getRequiredFeatureDE(casType, "discourseArgument", "org.cleartk.discourse.type.DiscourseArgument", featOkTst);
    casFeatCode_discourseArgument  = (null == casFeat_discourseArgument) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_discourseArgument).getCode();

  }
}



    