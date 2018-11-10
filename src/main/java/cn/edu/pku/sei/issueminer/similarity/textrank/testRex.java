package cn.edu.pku.sei.issueminer.similarity.textrank;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testRex {

    public static void main(String[] args) {
        String str = "abc 124 ewqeq qeqe   qeqe   qeqe  aaaa  fs fsdfs d    sf sf sf  sf sfada dss dee ad a f s f sa a'lfsd;'l";
        Pattern pt = Pattern.compile("\\b\\w{3}\\b");
        Matcher match = pt.matcher(str);
        while (match.find()) {
            //System.out.println(match.group());
        }
        String str2 = "dadaadad   da da   dasK[PWEOO-123- DASJAD@DHSJK.COM DADA@DAD.CN =0KFPOS9IR23J0IS ADHAJ@565@ADA.COM.CN shuqi@162.com UFSFJSFI-SI- ";
        Pattern pt2 = Pattern.compile("\\b\\w+@\\w+(\\.\\w{2,4})+\\b");
        Matcher matcher = pt2.matcher(str2);
        while (matcher.find()) {
            //System.out.println(matcher.group());
        }

        String str3 = "dada ada adad adsda ad asdda adr3 fas daf fas fdsf 234 adda";
        Pattern pt3 = Pattern.compile("\\b(\\w{3}) *(\\w{4}) *(\\w{5})\\b");
        Matcher matcher3 = pt3.matcher(str3);
        int countAll = matcher3.groupCount();
        while(matcher3.find()) {
            for (int i = 0; i < countAll; i++) {
                //System.out.println(matcher3.group(i+1));
            }
            //System.out.println(matcher3.group());
        }
        List<String> r = camelSplit("getIndexReader");
        /*for(String s:r){
            System.out.println(s);
        }*/

    }

    private static List<String> camelSplit(String e) {
        List<String> r = new ArrayList<>();
        Matcher m = Pattern.compile("^([a-z]+)|([A-Z][a-z]+)|([A-Z]+(?=([A-Z]|$)))").matcher(e);
        if (m.find()) {
            String s = m.group().toLowerCase();
            r.add(s);

            if (s.length() < e.length())
                r.addAll(camelSplit(e.substring(s.length())));
        }
        return r;
    }

}

