package org.ansj.lucene.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.domain.TermNature;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public final class AnsjTokenizer extends Tokenizer {

	// 当前词
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	// 偏移量
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	// 距离
	private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);

	private ToAnalysis ta = null;
	private Set<String> filter;
	private boolean pstemming;

	private final PorterStemmer stemmer = new PorterStemmer();

	public AnsjTokenizer(Reader input, Set<String> filter, boolean pstemming) {
		super(input);
		ta = new ToAnalysis(input);
		this.filter = filter;
		this.pstemming = pstemming;
	}

	@Override
	public boolean incrementToken() throws IOException {
		// TODO Auto-generated method stub
		clearAttributes();
		int position = 0;
		Term term = null;
		String name = null;
		int length = 0;
		boolean flag = true;
		do {
			term = ta.next();
			if (term == null) {
				break;
			}
			length = term.getName().length();
			if (pstemming && term.getTermNatures().termNatures[0] == TermNature.EN) {
				name = stemmer.stem(term.getName());
				term.setName(name);
			}
			position++;

			if (filter != null && filter.contains(name)) {
				continue;
			} else {
				flag = false;
			}
		} while (flag);
		if (term != null) {
			positionAttr.setPositionIncrement(position);
			termAtt.setEmpty().append(term.getName());
			offsetAtt.setOffset(term.getOffe(), term.getOffe() + length);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub
		super.reset();
		ta = new ToAnalysis(input);
	}

}
