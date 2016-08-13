package ca.concordia.clac.uima.engines;

import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIndexRepository;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasCopier;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class ViewAnnotationCopier extends JCasAnnotator_ImplBase{
	public static final String PARAM_SOURCE_VIEW_NAME = "sourceViewName";
	public static final String PARAM_TARGET_VIEW_NAME = "targetViewName";

	@ConfigurationParameter(name=PARAM_SOURCE_VIEW_NAME,defaultValue=CAS.NAME_DEFAULT_SOFA)
	private String sourceViewName;

	@ConfigurationParameter(name=PARAM_TARGET_VIEW_NAME, mandatory=true)
	private String targetViewName;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		JCas sourceView;
		JCas targetView;
		try {
			sourceView = aJCas.getView(sourceViewName);
			try{
				targetView = aJCas.getView(targetViewName);
				
			} catch (CASRuntimeException e){
				targetView = aJCas.createView(targetViewName);
			}
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
		
		CasCopier copier = new CasCopier(sourceView.getCas(),
				targetView.getCas());

		FSIndexRepository indexRep =
				sourceView.getFSIndexRepository();
		Iterator<String> labelIterators = indexRep.getLabels();
		while (labelIterators.hasNext()) {
			String label = labelIterators.next();
			FSIndex<FeatureStructure> fsIndex = indexRep.getIndex(label);
			FSIterator<FeatureStructure> fsiterator = fsIndex.iterator();
			while (fsiterator.hasNext()) {
				FeatureStructure currAnnot = fsiterator.next();
				if (currAnnot instanceof DocumentMetaData){
					DocumentMetaData.copy(sourceView, targetView);
				} else {
					//make a copy of the annotation
					FeatureStructure newAnnot = copier.copyFs(currAnnot);
					Feature sofaFeature = newAnnot.getType().getFeatureByBaseName("sofa");
					newAnnot.setFeatureValue(sofaFeature, targetView.getSofa());
					targetView.addFsToIndexes(newAnnot);
				}

			}
		}
	}

}
