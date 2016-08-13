package org.cleartk.corpus.conll2015;

import java.io.PrintStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.discourse.type.TokenList;
import org.eclipse.persistence.jaxb.MarshallerProperties;

@XmlAccessorType(XmlAccessType.FIELD)
class Span {
	int begin;
	int end;
	String text;
	
	public Span() {
	}
	
	public Span(TokenList aTokenList){
		if (aTokenList == null)
			return;
		
		text = TokenListTools.getTokenListText(aTokenList);
		begin = aTokenList.getBegin();
		end = aTokenList.getEnd();
	}
}

@XmlAccessorType(XmlAccessType.FIELD)
class JsonDiscourseRelation{
	int begin;
	int end;
    protected String type;
    protected String sense;
    protected Span arg1;
    protected Span arg2;
    protected Span discourseConnective;
    protected String context;

    public JsonDiscourseRelation() {
	}
    
    public JsonDiscourseRelation(DiscourseRelation aRelation) {
    	arg1 = new Span(aRelation.getArguments(0));
    	arg2 = new Span(aRelation.getArguments(1));
    	discourseConnective = new Span(aRelation.getDiscourseConnective());
    	type = aRelation.getRelationType();
    	sense = aRelation.getSense();
    	context = aRelation.getCoveredText();
    	begin = aRelation.getBegin();
    	end = aRelation.getEnd();
    }
    
}


public class DiscourseRelationToJSonConverter {
	private Marshaller marshaller;
	
	public DiscourseRelationToJSonConverter() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(JsonDiscourseRelation.class);
		marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
	}
	
	public Marshaller getMarshaller() {
		return marshaller;
	}

	public void marshal(DiscourseRelation next, PrintStream out) throws JAXBException {
		marshaller.marshal(new JsonDiscourseRelation(next), out); 
	}
	
	public static void main(String[] args) throws JAXBException {
		DiscourseRelationToJSonConverter discourseRelationToJSonConverter = new DiscourseRelationToJSonConverter();
		JsonDiscourseRelation span = new JsonDiscourseRelation();
		discourseRelationToJSonConverter.getMarshaller().marshal(span, System.out);
	}
	
}
