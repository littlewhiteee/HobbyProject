package cn.ismiss.utils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.ismiss.bean.JsoupImageVO;

public class JsoupBaiduPic {

    public static List<JsoupImageVO> findImage(String hotelName, int page) {
        int number=100;
        String url = "http://image.baidu.com/search/avatarjson?tn=resultjsonavatarnew&ie=utf-8&word=" + hotelName + "&cg=star&pn=" + page * 30 + "&rn="+number+"&itg=0&z=0&fr=&width=&height=&lm=-1&ic=0&s=0&st=-1&gsm=" + Integer.toHexString(page * 30);

        return findImageNoURl(url);
    }

    private static List<JsoupImageVO> findImageNoURl(String url) {
        List<JsoupImageVO> result = new ArrayList<JsoupImageVO>();
        result.clear();
        Document document = null;
        try {
            document = Jsoup.connect(url).data("query", "Java")//请求参数
                    .userAgent("Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)")//设置urer-agent  get();
                    .timeout(5000)
                    .get();
            String xmlSource = document.toString();
            result = dealResult(xmlSource);
        } catch (Exception e) {
            String defaultURL = "http://qnimg.zowoyoo.com/img/15463/1509533934407.jpg";
            result = dealResult(defaultURL);
        }
        return result;
    }

    private static List<JsoupImageVO> dealResult(String xmlSource) {
        List<JsoupImageVO> result = new ArrayList<JsoupImageVO>();
        xmlSource = StringEscapeUtils.unescapeHtml3(xmlSource);
        String reg = "objURL\":\"http://.+?\\.(gif|jpeg|png|jpg|bmp)";
        Pattern pattern = Pattern.compile(reg);
        Matcher m = pattern.matcher(xmlSource);
        while (m.find()) {
            JsoupImageVO jsoupImageVO = new JsoupImageVO();
            String imageURL = m.group().substring(9);
            if (imageURL == null || "".equals(imageURL)) {
                String defaultURL = "http://qnimg.zowoyoo.com/img/15463/1509533934407.jpg";
                jsoupImageVO.setUrl(defaultURL);
                jsoupImageVO.setName("null_"+System.currentTimeMillis());
            } else {
                jsoupImageVO.setUrl(imageURL);
                jsoupImageVO.setName("pic_"+System.currentTimeMillis());
            }
            result.add(jsoupImageVO);
        }
        return result;
    }
}