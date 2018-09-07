package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangyanjie
 * @create 2018-09-01 - 17:03
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";


    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        Index build = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        try {
            DocumentResult result = jestClient.execute(build);
            System.out.println("result="+result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {

        //先写dsl语句，使用java代码写
        String query=makeQueryStringForSearch(skuLsParams);
        //执行
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;
        try {
            //执行的结果，变成我们封装结果集的对象
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //结果集装换
        SkuLsResult skuLsResult =  makeResultForSearch(skuLsParams,searchResult);
        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //定义一个常量，意思是当商品详情刷新10次时，更新一次es
        int timesToEs = 10;
        //对redis中商品次数进行累加，自定义步长是1，即每调用一次incrHotScore方法，累计加一次
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);
        //热度次数累加到10，更新一次es中的数据
        if(hotScore%timesToEs==0){
            //更新方法  Math.round()在参数上加上0.5然后进行下取整 eg：Math.round(11.5)==12  Math.round(-11.5)==-11
            updateHotScore(skuId, Math.round(hotScore));
        }
    }

    /**
     * 根据热度排名，更新es的数据
     * @param skuId
     * @param hotScore
     */
    private void updateHotScore(String skuId, long hotScore) {
        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";
        Update build = new Update.Builder(updateJson).index(ES_INDEX).type(ES_TYPE).id(skuId).build();
        try {
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 结果集转换，变成封装结果集的对象
     * @param skuLsParams
     * @param searchResult
     * @return
     */
    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {
//        SkuLsResult类中的属性
//        List<SkuLsInfo> skuLsInfoList;
//        long total;
//        long totalPages;
//        List<String> attrValueIdList;
        SkuLsResult skuLsResult = new SkuLsResult();

        //声明一个集合来存储SkuLsInfo
        ArrayList<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        //获取，循环查出的结果集
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            //获取对象，取完对象之后的skuName不是高亮的
            SkuLsInfo skuLsInfo = hit.source;
            //取出高亮字段将原来的值进行覆盖
            if(hit.highlight!=null && hit.highlight.size()>0){
                List<String> list = hit.highlight.get("skuName");
                String skuNameHl = list.get(0);
                skuLsInfo.setSkuName(skuNameHl);
            }

            skuLsInfoArrayList.add(skuLsInfo);
        }

        //skuLsInfo对象
        skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);
        //总数
        skuLsResult.setTotal(searchResult.getTotal());

        //总页数
        long totalPage = (searchResult.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPage);

        //平台属性值，从聚合总获取
        List<String> attrValueIdList = new ArrayList<>();
        //获得数据
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        for (TermsAggregation.Entry bucket : buckets) {
            String key = bucket.getKey();
            attrValueIdList.add(key);
        }
        //给平台属性赋值
        skuLsResult.setAttrValueIdList(attrValueIdList);
        return skuLsResult;
    }

    /**
     * 编写dsl语句
     * @param skuLsParams
     * @return
     */
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {

        //query,bool,filter
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //skuName作为keyword
        if(skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            //如果用户输入了关键字(must: match keyword)
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);

            //设置高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            //设置高亮字段
            highlightBuilder.field("skuName");
            //设置高亮样式，前缀<span style='color:red'>
            highlightBuilder.preTags("<span style='color:red'>");
            //设置高亮样式，后缀</span>         <span style='color:red'></span>
            highlightBuilder.postTags("</span>");
            //将高亮字段放入查询结果中
            searchSourceBuilder.highlight(highlightBuilder);
        }

        //设置三级分类catalog3Id
        if(skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            //如果用户根据三级分类id查询(filter,term)
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //设置属性值skuAttrValueList.valueId
        if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                 String valueId = skuLsParams.getValueId()[i];
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);

        //设置分页
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //设置按照热度"sort":{"hotScore":{"order":"desc"}},
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //设置聚合(aggs:groupby_attr---terms---field)
        TermsBuilder termsBuilder = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(termsBuilder);

        String query = searchSourceBuilder.toString();

        System.out.println("query="+query);
        return query;
    }
}
