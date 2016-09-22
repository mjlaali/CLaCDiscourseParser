package ca.concordia.clac.uima.engines;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Objects;

import ca.concordia.clac.ml.classifier.InstanceExtractor;

public class AnnotationMergerAnnotator<T extends Annotation> extends JCasAnnotator_ImplBase{
	public static final String PARAM_MERGE_POLICY_CLS_NAME = "annotationClsName";

	@ConfigurationParameter(name=PARAM_MERGE_POLICY_CLS_NAME)
	private String mergePolicyClsName;
	
	private InstanceExtractor<T> instanceExtractor;
	private BiFunction<T, T, T> merger;
	
	
	public static <T extends Annotation> AnalysisEngineDescription getDescription(
			Class<? extends MergePolicy<T>> policyCls) throws ResourceInitializationException {
		
		return createEngineDescription(AnnotationMergerAnnotator.class, 
				PARAM_MERGE_POLICY_CLS_NAME, policyCls.getName());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		MergePolicy<T> policy = InitializableFactory.create(context, mergePolicyClsName, MergePolicy.class);
		instanceExtractor = policy.getInstanceExtractor();
		merger = policy.getMerger();
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<T> instances = instanceExtractor.getInstances(aJCas);

		Map<Span, T> resolvedAnnotation = new HashMap<>();
		for (T instance: instances){
			Span annotationSpan = new Span(instance);
			T anotherAnnotation = resolvedAnnotation.get(annotationSpan);
			T resolved = instance;
			if (anotherAnnotation != null){
				resolved = merger.apply(anotherAnnotation, instance); 
			}
			resolvedAnnotation.put(annotationSpan, resolved);
		}
		
		Set<T> toBeRemovedSet = new HashSet<T>(instances);
		toBeRemovedSet.removeAll(resolvedAnnotation.values());
		
		for (T toBeRemoved: toBeRemovedSet){
			toBeRemoved.removeFromIndexes();
		}
	}
	

	private static class Span {

	    public int end;

	    public int begin;

	    public Span(Annotation annotation) {
	      this.begin = annotation.getBegin();
	      this.end = annotation.getEnd();
	    }

	    @Override
	    public int hashCode() {
	      return Objects.hashCode(this.begin, this.end);
	    }

	    @Override
	    public boolean equals(Object obj) {
	      if (!this.getClass().equals(obj.getClass())) {
	        return false;
	      }
	      Span that = (Span) obj;
	      return this.begin == that.begin && this.end == that.end;
	    }

	    @Override
	    public String toString() {
	      ToStringHelper helper = MoreObjects.toStringHelper(this);
	      helper.add("begin", this.begin);
	      helper.add("end", this.end);
	      return helper.toString();
	    }
	  }



}
