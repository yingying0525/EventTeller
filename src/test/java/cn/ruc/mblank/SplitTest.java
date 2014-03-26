package cn.ruc.mblank;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

import java.util.List;

/**
 * Created by mblank on 14-3-22.
 */
public class SplitTest {

    public static  void main(String[] args){
        String text = "我是一个好人";
        List<Term> terms = NlpAnalysis.parse(text);
        for(Term tm : terms){
            System.out.println(tm.getNatrue() + "\t" + tm.getName());
        }




    }
}
