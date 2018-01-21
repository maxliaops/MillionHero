package me.lingfengsan.hero;

import org.apdplat.search.JSoupBaiduSearcher;
import org.apdplat.search.SearchResult;
import org.apdplat.search.Searcher;
import org.apdplat.search.Webpage;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by lenovo on 2018/1/21.
 */

public class Search4 implements Callable {
    String keyword;

    public Search4(String keyword) {
        this.keyword = keyword;
    }

    public String search(String keyword) {
        String result = "";
        Searcher searcher = new JSoupBaiduSearcher();
        SearchResult searchResult = searcher.search(keyword,1);
        List<Webpage> webpages = searchResult.getWebpages();
        if (webpages != null) {
            result = result + "\n";
//            int i = 1;
//            LOG.info("搜索结果 当前第 " + searchResult.getPage() + " 页，页面大小为：" + searchResult.getPageSize() + " 共有结果数：" + searchResult.getTotal());
            for (Webpage webpage : webpages) {
                result = result + "标题：" + webpage.getTitle() + "\n";
                result = result + "摘要：" + webpage.getSummary() + "\n\n";

//                LOG.info("搜索结果 " + (i++) + " ：");
//                LOG.info("标题：" + webpage.getTitle());
//                LOG.info("URL：" + webpage.getUrl());
//                LOG.info("摘要：" + webpage.getSummary());
//                LOG.info("正文：" + webpage.getContent());
//                LOG.info("");
            }
        } else {
//            LOG.error("没有搜索到结果");
        }
        return result;
    }

    @Override
    public String call() throws Exception {
        return search(keyword);
    }
}
